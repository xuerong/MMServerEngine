package com.mm.engine.framework.data.persistence.orm;

import com.mm.engine.framework.data.persistence.dao.ColumnDesc;
import com.mm.engine.framework.data.persistence.dao.DatabaseHelper;
import com.mm.engine.framework.data.persistence.dao.SqlHelper;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.util.ObjectUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供与实体相关的数据库操作
 *
 * 持久层也要做成可插件性质的吗?
 *
 * @author huangyong
 * @since 1.0
 */
public class DataSet {

    private static String PK_NAME="id";

    /**
     * 查询单条数据，并转为相应类型的实体
     */
    public static <T> T select(Class<T> entityClass, String condition, Object... params) {
        String sql = SqlHelper.generateSelectSql(entityClass, condition, "");
        return DatabaseHelper.queryEntity(entityClass, sql, params);
    }

    /**
     * 查询多条数据，并转为相应类型的实体列表
     */
    public static <T> List<T> selectList(Class<T> entityClass) {
        return selectListWithConditionAndSort(entityClass, "", "");
    }

    /**
     * 查询多条数据，并转为相应类型的实体列表（带有查询条件与查询参数）
     */
    public static <T> List<T> selectListWithCondition(Class<T> entityClass, String condition, Object... params) {
        return selectListWithConditionAndSort(entityClass, condition, "", params);
    }

    /**
     * 查询多条数据，并转为相应类型的实体列表（带有排序方式）
     */
    public static <T> List<T> selectListWithSort(Class<T> entityClass, String sort) {
        return selectListWithConditionAndSort(entityClass, "", sort);
    }

    /**
     * 查询多条数据，并转为相应类型的实体列表（带有查询条件、排序方式与查询参数）
     */
    public static <T> List<T> selectListWithConditionAndSort(Class<T> entityClass, String condition, String sort, Object... params) {
        String sql = SqlHelper.generateSelectSql(entityClass, condition, sort);
        return DatabaseHelper.queryEntityList(entityClass, sql, params);
    }

    /**
     * 查询数据条数
     */
    public static long selectCount(Class<?> entityClass, String condition, Object... params) {
        String sql = SqlHelper.generateSelectSqlForCount(entityClass, condition);
        return DatabaseHelper.queryCount(sql, params);
    }

    /**
     * 查询多条数据，并转为列表（分页方式）
     */
    public static <T> List<T> selectListForPager(int pageNumber, int pageSize, Class<T> entityClass, String condition, String sort, Object... params) {
        String sql = SqlHelper.generateSelectSqlForPager(pageNumber, pageSize, entityClass, condition, sort);
        return DatabaseHelper.queryEntityList(entityClass, sql, params);
    }

    /**
     * 查询多条数据，并转为映射
     */
    public static <T> Map<Long, T> selectMap(Class<T> entityClass) {
        return selectMapWithPK(entityClass, PK_NAME, "", "");
    }

    /**
     * 查询多条数据，并转为映射（带有查询条件与查询参数）
     */
    public static <T> Map<Long, T> selectMapWithCondition(Class<T> entityClass, String condition, Object... params) {
        return selectMapWithPK(entityClass, PK_NAME, condition, "", params);
    }

    /**
     * 查询多条数据，并转为映射（带有排序方式与查询参数）
     *
     * @since 2.3.3
     */
    public static <T> Map<Long, T> selectMapWithSort(Class<T> entityClass, String sort) {
        return selectMapWithPK(entityClass, PK_NAME, "", sort);
    }

    /**
     * 查询多条数据，并转为映射（带有查询条件、排序方式与查询参数）
     */
    public static <T> Map<Long, T> selectMapWithConditionAndSort(Class<T> entityClass, String condition, String sort, Object... params) {
        return selectMapWithPK(entityClass, PK_NAME, condition, sort, params);
    }

    /**
     * 查询多条数据，并转为映射（带有主键名）
     */
    @SuppressWarnings("unchecked")
    public static <PK, T> Map<PK, T> selectMapWithPK(Class<T> entityClass, String pkName, String condition, String sort, Object... params) {
        Map<PK, T> map = new LinkedHashMap<PK, T>();
        List<T> list = selectListWithConditionAndSort(entityClass, condition, sort, params);
        for (T obj : list) {
            PK pk = (PK) ObjectUtil.getFieldValue(obj, pkName);
            map.put(pk, obj);
        }
        return map;
    }

    /**
     * 根据列名查询单条数据，并转为相应类型的实体
     */
    public static <T> T selectColumn(Class<?> entityClass, String columnName, String condition, Object... params) {
        String sql = SqlHelper.generateSelectSql(entityClass, condition, "");
        sql = sql.replace("*", columnName);
        return DatabaseHelper.queryColumn(sql, params);
    }

    /**
     * 根据列名查询多条数据，并转为相应类型的实体列表
     */
    public static <T> List<T> selectColumnList(Class<?> entityClass, String columnName, String condition, String sort, Object... params) {
        String sql = SqlHelper.generateSelectSql(entityClass, condition, sort);
        sql = sql.replace("*", columnName);
        return DatabaseHelper.queryColumnList(sql, params);
    }

    /**
     * 获取表结构
     * @param tableName
     * @return
     */
    public static List<ColumnDesc> getTableDesc(String tableName){
        try {
            List<Map<String,Object>> l = DatabaseHelper.queryMapList("desc " + tableName);
            if(l == null){
                return null;
            }
            List<ColumnDesc> result = new ArrayList<>();
            for(Map<String,Object> map : l){
                ColumnDesc columnDesc = new ColumnDesc();
                columnDesc.setField(map.get("Field").toString());
                columnDesc.setType(map.get("Type").toString());
                columnDesc.setKey(map.get("Key").equals("PRI"));
                result.add(columnDesc);
            }
            return result;
        }catch (Throwable e){
            if(e.getMessage().contains("doesn't exist Query: desc")){
                return null;
            }else{
                throw new MMException(e);
            }
        }
    }

    /**
     * 插入一条数据
     */
    public static boolean insert(Class<?> entityClass, Map<String, Object> fieldMap) {
        if (MapUtils.isEmpty(fieldMap)) {
            return true;
        }
        String sql = SqlHelper.generateInsertSql(entityClass, fieldMap.keySet());
        int rows = DatabaseHelper.update(sql, fieldMap.values().toArray());
        return rows > 0;
    }

    /**
     * 插入一个实体
     */
    public static boolean insert(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException();
        }
        Class<?> entityClass = entity.getClass();
//        Map<String, Object> fieldMap = ObjectUtil.getFieldMap(entity);
        Map<String, Object> fieldMap = EntityHelper.getFieldMap(entity);
        return insert(entityClass, fieldMap);
    }

    /**
     * 更新相关数据
     */
    public static boolean update(Class<?> entityClass, Map<String, Object> fieldMap, String condition, Object... params) {
        if (MapUtils.isEmpty(fieldMap)) {
            return true;
        }
        String sql = SqlHelper.generateUpdateSql(entityClass, fieldMap, condition);
        int rows = DatabaseHelper.update(sql, ArrayUtils.addAll(fieldMap.values().toArray(), params));
        return rows > 0;
    }

    /**
     * 更新一个实体
     */
    public static boolean update(Object entity) {
        return update(entity, PK_NAME);
    }

    /**
     * 更新一个实体（带有主键名）
     */
    public static boolean update(Object entityObject, String pkName) {
        if (entityObject == null) {
            throw new IllegalArgumentException();
        }
        Class<?> entityClass = entityObject.getClass();
//        Map<String, Object> fieldMap = ObjectUtil.getFieldMap(entityObject);
        Map<String, Object> fieldMap = EntityHelper.getFieldMap(entityObject);
        String condition = pkName + " = ?";
        Object[] params = {ObjectUtil.getFieldValue(entityObject, pkName)};
        return update(entityClass, fieldMap, condition, params);
    }
    /**
     * 更新一个实体（根据给定的条件）
     */
    public static boolean update(Object entityObject,String condition, Object... params){
        if (entityObject == null) {
            throw new IllegalArgumentException();
        }
        Class<?> entityClass = entityObject.getClass();
//        Map<String, Object> fieldMap = ObjectUtil.getFieldMap(entityObject);
        Map<String, Object> fieldMap = EntityHelper.getFieldMap(entityObject);
        return update(entityClass, fieldMap, condition, params);
    }
    public static boolean update(Object entityObject, EntityHelper.ConditionItem conditionItem){
        return update(entityObject,conditionItem.getCondition(),conditionItem.getParams());
    }

    /**
     * 删除相关数据
     */
    public static boolean delete(Class<?> entityClass, String condition, Object... params) {
        String sql = SqlHelper.generateDeleteSql(entityClass, condition);
        int rows = DatabaseHelper.update(sql, params);
        return rows > 0;
    }

    /**
     * 删除一个实体
     */
    public static boolean delete(Object entityObject) {
        return delete(entityObject, PK_NAME);
    }

    /**
     * 删除一个实体（可指定主键名）
     */
    public static boolean delete(Object entityObject, String pkName) {
        if (entityObject == null) {
            throw new IllegalArgumentException();
        }
        Class<?> entityClass = entityObject.getClass();
        String condition = pkName + " = ?";
        Object[] params = {ObjectUtil.getFieldValue(entityObject, pkName)};
        return delete(entityClass, condition, params);
    }
    /**
     * 删除一个实体（根据给定的条件）
     */
    public static boolean delete(Object entityObject, EntityHelper.ConditionItem conditionItem) {
        return delete(entityObject.getClass(), conditionItem.getCondition(), conditionItem.getParams());
    }
}
