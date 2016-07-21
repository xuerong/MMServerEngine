package com.mm.engine.framework.data.persistence.orm;

import com.mm.engine.framework.data.persistence.orm.annotation.Column;
import com.mm.engine.framework.data.persistence.orm.annotation.DBEntity;
import com.mm.engine.framework.tool.helper.ClassHelper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 初始化 DBEntity 结构
 *
 * @author huangyong
 * @since 1.0
 */
public class EntityHelper {

    /**
     * 实体类 => 表名
     */
    private static final Map<Class<?>, String> entityClassTableNameMap = new HashMap<Class<?>, String>();

    /**
     * 实体类 => (字段名 => 列名)
     */
    private static final Map<Class<?>, Map<String, String>> entityClassFieldMapMap = new HashMap<Class<?>, Map<String, String>>();

    static {
        // 获取并遍历所有实体类
        List<Class<?>> entityClassList = ClassHelper.getClassListByAnnotation(DBEntity.class);
        for (Class<?> entityClass : entityClassList) {
            initEntityNameMap(entityClass);
            initEntityFieldMapMap(entityClass);
        }
    }

    private static void initEntityNameMap(Class<?> entityClass) {
        DBEntity dbEntity=entityClass.getAnnotation(DBEntity.class);
        String tableName=dbEntity.tableName();
        entityClassTableNameMap.put(entityClass, tableName);
    }

    private static void initEntityFieldMapMap(Class<?> entityClass) {
        // 获取并遍历该实体类中所有的字段（不包括父类中的方法）
        Field[] fields = entityClass.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
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
                    // 若不存在，则将字段名转换为下划线风格的列名
                    columnName = camelhumpToUnderline(fieldName);
                }
                fieldMap.put(fieldName, columnName);
            }
            entityClassFieldMapMap.put(entityClass, fieldMap);
        }
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
