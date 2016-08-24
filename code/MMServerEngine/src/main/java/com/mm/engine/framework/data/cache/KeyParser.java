package com.mm.engine.framework.data.cache;

import com.mm.engine.framework.data.persistence.orm.EntityHelper;
import com.mm.engine.framework.exception.ExceptionHelper;
import com.mm.engine.framework.exception.ExceptionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a on 2016/8/10.
 * 完成对cache的key的生成等操作
 */
public class KeyParser {
    private static final Logger log = LoggerFactory.getLogger(KeyParser.class);
    private static final String LISTSEPARATOR ="#";
    private static final String SEPARATOR ="_";

//    private static Map<Class<?>,List<String>> pkMap = new HashMap<>();
    static {
        // 要校验所有的DBEntity，确保Class.getName()不能有一样的
    }

    public static String parseKey(Object entity){
        return null;
    }

    /**
     * 传进来的条件必须是主键
     * @return
     */
    public static String parseKeyForObject(Class<?> entityClass, String condition, Object... params){
        // 判断条件中的主键
        Map<String,Method> pkMethodMap = EntityHelper.getPkGetMethodMap(entityClass);
        Set<String> pks = pkMethodMap.keySet(); // 注意这里面的排序
        if(pks == null){
            ExceptionHelper.handle(ExceptionLevel.Serious,"没找到主键方法"+entityClass.getName(),null);
        }
        String resultCondition = parseParamsToString(condition,params);
        String[] conditions = resultCondition.split("and");
        Map<String,String> pksInConditions = new HashMap<>();
        for (String conditionStr:conditions) {
            conditionStr = conditionStr.trim();
            if(conditionStr.length() > 0){
                String[] pk = conditionStr.split("=");
                if(pk.length!=2){
                    ExceptionHelper.handle(ExceptionLevel.Serious,"condition 参数错误,resultCondition = "+resultCondition,null);
                }
                pksInConditions.put(pk[0],pk[1]);
            }
        }
        if(pks.size() > pksInConditions.size()){
            ExceptionHelper.handle(ExceptionLevel.Serious,"condition 参数错误,主键数量，resultCondition = "+resultCondition,null);
        }
        //拼接key
        StringBuilder sb = new StringBuilder(entityClass.getName());
        for (String pk:pks) {
            if(!pksInConditions.containsKey(pk)){
                ExceptionHelper.handle(ExceptionLevel.Serious,"condition 参数错误,缺少主键["+pk+"]+，resultCondition = "+resultCondition,null);
            }
            sb.append("_"+pksInConditions.get(pk));
        }

        return sb.toString();
    }
    // 从listKey中获取对应的class的名字
    public static String getClassNameFromListKey(String listKey){
        return listKey.split("#")[0];
    }
    // 判断一个对象是否属于一个list
    public static <T> boolean isObjectBelongToList(Object object,String listKey){
        if(!listKey.startsWith(object.getClass().getName())){ // 是否是同一中类
            return false;
        }
        String[] listKeyStrs = listKey.split(LISTSEPARATOR);
        if(listKeyStrs.length<4){
            log.warn("listKey is Illegal : listKey = "+listKey);
            return false;
        }
        String[] fieldNames = listKeyStrs[2].split(SEPARATOR);
        String[] fieldValues = listKeyStrs[3].split(SEPARATOR);
        if(fieldNames.length !=fieldValues.length){
            log.warn("listKey is Illegal : listKey = "+listKey);
            return false;
        }
        Map<String,Method> getMethodMap = EntityHelper.getGetMethodMap(object.getClass());
        try {
            int i=0;
            for(String fieldName : fieldNames){
                Method method = getMethodMap.get(fieldName);
                if(method == null){
                    log.warn("listKey is Illegal : fieldName is not exist in getMethodMap , fieldName = "+fieldName);
                    return false;
                }
                Object ret = method.invoke(object);
                if(!parseParamToString(ret).equals(fieldValues[i])){
                    return false;
                }
                i++;
            }
        } catch (IllegalAccessException |InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * list的key如何实际：
     * entityClass.getName()#list#[条件列名_...]#[条件值_...]
     * LISTSEPARATOR
     * @param entityClass
     * @param condition
     * @param params
     * @return
     */
    public static String parseKeyForList(Class<?> entityClass, String condition, Object... params){
        return null;
    }
    ///////---------------------------工具----------------------
    private static String parseParamsToString(String condition, Object... params){
        if(params.length == 0 || condition.indexOf('?') == -1){
            return condition;
        }
        if(params.length>=1 && condition.indexOf('?') == condition.lastIndexOf('?')){
            return condition.replace("?",parseParamToString(params[0]));
        }
        StringBuilder sb = new StringBuilder(condition);
        int count = 0;
        int paramsCount = 0;
        String paramsStr = null;
        for (char c: condition.toCharArray()){
            if(c == '?'){
                if(params.length <= paramsCount){
                    ExceptionHelper.handle(ExceptionLevel.Serious,"params 参数错误，参数太少",null);
                }
                paramsStr = parseParamToString(params[paramsCount]);
                sb.replace(count,count+1,paramsStr);
                paramsCount++;
                count+=(paramsStr.length()-1);
            }
            count++;
        }
        return sb.toString();
    }
    private static String parseParamToString(Object param){
        if(param instanceof Timestamp){
            return ((Timestamp)param).getTime()+"";
        }
        return param.toString();
    }
    public static void main(String[] args){
        List aaa=null;
        for (Object a:aaa
             ) {

        }
//        String out = parseParamsToString("11 ? 33 ? 55 ?","haha","hei",new Timestamp(System.currentTimeMillis()));
//        System.out.println(out);
    }
}
