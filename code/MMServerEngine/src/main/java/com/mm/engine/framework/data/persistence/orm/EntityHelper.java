package com.mm.engine.framework.data.persistence.orm;

import com.mm.engine.framework.data.entity.account.sendMessage.SendMessageGroup;
import com.mm.engine.framework.data.persistence.dao.ColumnDesc;
import com.mm.engine.framework.data.persistence.orm.annotation.Column;
import com.mm.engine.framework.data.persistence.orm.annotation.DBEntity;
import com.mm.engine.framework.data.sysPara.SysPara;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.ClassHelper;
import com.mm.engine.framework.tool.util.ObjectUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 初始化 DBEntity 结构
 *
 * @author huangyong
 * @since 1.0
 */
public class EntityHelper {
    private static final Logger log = LoggerFactory.getLogger(EntityHelper.class);
    /**
     * 实体类 => 表名
     */
    private static final Map<Class<?>, String> entityClassTableNameMap = new HashMap<Class<?>, String>();

    /**
     * 实体类 => (字段名 => 列名)
     * TODO 这里面只需要存储entity中对应表中有的字段
     */
    private static final Map<Class<?>, Map<String, String>> entityClassFieldMapMap = new HashMap<Class<?>, Map<String, String>>();

    /**
     * DBEntity类与所有的get方法
     */
    private static final Map<Class<?>,Map<String,Method>> getMethodMap = new HashMap<>();

    /**
     * DBEntity类与所有的主键的get方法
     * fieldName-method
     */
    private static final Map<Class<?>,Map<String,Method>> getPkMethodMap = new HashMap<>();

    public static Map<String,Method> getGetMethodMap(Class<?> entityClass){
        return getMethodMap.get(entityClass);
    }
    public static Map<String,Method> getPkGetMethodMap(Class<?> entityClass){
        return getPkMethodMap.get(entityClass);
    }

    static {
        try {
            // 获取并遍历所有实体类
            // TODO 校验数据库中对应的表的存在和对应的字段,只需要数据库中存在的列即可
            List<Class<?>> entityClassList = ClassHelper.getClassListByAnnotation(DBEntity.class);
            for (Class<?> entityClass : entityClassList) {
//                initEntityNameMap(entityClass);
//                initEntityFieldMapMap(entityClass);
//                initEntityGetMethods(entityClass);
                initEntity(entityClass);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 构建key组成的sql语句中的condition
     * @param object
     * @return
     */
    public static ConditionItem parsePkCondition(Object object){
        Map<String,Method> map = getPkGetMethodMap(object.getClass());
        if(map == null || map.size() == 0){
            throw new MMException("getPkGetMethodMap is null , class = "+object.getClass());
        }

        Object[] params = new Object[map.size()];
        StringBuilder sb = new StringBuilder();
        int i=0;
        try {
            for(Map.Entry<String,Method> entry : map.entrySet()){
                sb.append(entry.getKey()+"=? and");
                params[i++] = entry.getValue().invoke(object);
            }
        }catch (IllegalAccessException |InvocationTargetException e){
            throw new MMException(e);
        }
        String condition = "";
        if(sb.length()>0){
            condition = sb.substring(0,sb.length()-4);
        }
        ConditionItem result = new ConditionItem();
        result.setParams(params);
        result.setCondition(condition);
        return result;
    }

    public static class ConditionItem{
        private String condition;
        private Object[] params;

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    private static void initEntity(Class<?> entityClass){
        // ----------------initEntityNameMap
        DBEntity dbEntity=entityClass.getAnnotation(DBEntity.class);
        String tableName=dbEntity.tableName();
        // 查看table是否存在，并获取列名
        List<ColumnDesc> columnDescList = DataSet.getTableDesc(tableName);
        if(columnDescList == null){
            log.warn("table is not exist ,tableName = "+tableName+",DBEntity = "+entityClass.getName());
            return;
        }

        Set<String> columnNameSet = new HashSet<>(columnDescList.size());
        for(ColumnDesc columnDesc : columnDescList){
            columnNameSet.add(columnDesc.getField());
        }
        entityClassTableNameMap.put(entityClass, tableName);
        // ---------------initEntityFieldMapMap
        // 获取并遍历该实体类中所有的字段（不包括父类中的方法）
        Field[] fields = entityClass.getDeclaredFields();
//        Field[] fields = entityClass.getFields();
        if (ArrayUtils.isEmpty(fields)) {
            log.warn("fields is null,entityClass = "+entityClass.getName());
            return;
        }
        // 创建一个 fieldMap（用于存放列名与字段名的映射关系）
        Map<String, String> fieldMap = new HashMap<String, String>();
        for (Field field : fields) {
            String fieldName = field.getName();
            String columnName;
            // 判断该字段上是否存在 Column 注解
            if (field.isAnnotationPresent(Column.class)) {
                // 若已存在，则使用该注解中定义的列名
                columnName = field.getAnnotation(Column.class).value();
            } else {
                // 若不存在，则直接用列名
                columnName = fieldName;//camelhumpToUnderline(fieldName);
            }
            if(columnNameSet.contains(columnName)) {
                fieldMap.put(fieldName, columnName);
            }
        }
        entityClassFieldMapMap.put(entityClass, fieldMap);
        // ------------------initEntityGetMethods
        Set<String> set = fieldMap.keySet();
        Map<String,Method> getMethodMap = new HashMap<>(set.size());
        Map<String,Method> getPkMethodMap = new HashMap<>(set.size());
//        DBEntity dbEntity = entityClass.getAnnotation(DBEntity.class);
        String[] pks = dbEntity.pks();
        if(pks == null || pks.length==0){
            pks = set.toArray(new String[set.size()]);
            log.warn("DBEntity has no pk Annotation : use all field as pks");
        }
        List<String> pkList = Arrays.asList(pks);
        for (String fieldName:set) {
            String first = fieldName.substring(0, 1);
            StringBuilder sb = new StringBuilder("get");
            sb.append(first.toUpperCase());
            sb.append(fieldName.substring(1));
            String methodName = sb.toString();
            Method method = null;
            try{
                method = entityClass.getMethod(methodName);
            }catch (NoSuchMethodException e){
                method = null;
            }
            if (method != null) {
                getMethodMap.put(fieldName,method);
                if(pkList.contains(fieldName)){
                    getPkMethodMap.put(fieldName,method);
                }
            } else {
                log.error("DBEntity get method not found: class="
                        + entityClass + ",methodName="
                        + methodName);
            }
        }
        EntityHelper.getMethodMap.put(entityClass,getMethodMap);
        EntityHelper.getPkMethodMap.put(entityClass,getPkMethodMap);
    }
    public static Map<String, Object> getFieldMap(Object obj) {
        Map<String, Object> fieldMap = new LinkedHashMap<String, Object>();
        Class cls = obj.getClass();
        Set<String> fieldNameSet = entityClassFieldMapMap.get(cls).keySet(); // 这些是需要保存到数据库中的列
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if(!fieldNameSet.contains(fieldName)){
                continue;
            }
            Object fieldValue = ObjectUtil.getFieldValue(obj, fieldName);
            fieldMap.put(fieldName, fieldValue);
        }
        return fieldMap;
    }
    /**
     * 将驼峰风格替换为下划线风格
     */
    public static String camelhumpToUnderline(String str) {
        Matcher matcher = Pattern.compile("[A-Z]").matcher(str);
        StringBuilder builder = new StringBuilder(str);
        for (int i = 0; matcher.find(); i++) {
            builder.replace(matcher.start() + i, matcher.end() + i, "_" + matcher.group().toLowerCase());
        }
        if (builder.charAt(0) == '_') {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    public static String getTableName(Class<?> entityClass) {
        return entityClassTableNameMap.get(entityClass);
    }

    public static Map<String, String> getFieldMap(Class<?> entityClass) {
        return entityClassFieldMapMap.get(entityClass);
    }

    public static Map<String, String> getColumnMap(Class<?> entityClass) {
        return invert(getFieldMap(entityClass));
    }

    public static String getColumnName(Class<?> entityClass, String fieldName) {
        String columnName = getFieldMap(entityClass).get(fieldName);
        return StringUtils.isNotEmpty(columnName) ? columnName : fieldName;
    }
    /**
     * 转置 Map
     */
    public static <K, V> Map<V, K> invert(Map<K, V> source) {
        Map<V, K> target = null;
        if (MapUtils.isNotEmpty(source)) {
            target = new LinkedHashMap<V, K>(source.size());
            for (Map.Entry<K, V> entry : source.entrySet()) {
                target.put(entry.getValue(), entry.getKey());
            }
        }
        return target;
    }
}
