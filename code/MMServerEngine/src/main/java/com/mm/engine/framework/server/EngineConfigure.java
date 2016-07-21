package com.mm.engine.framework.server;

import com.mm.engine.framework.data.cache.CacheCenter;
import com.mm.engine.framework.entrance.code.net.http.HttpDecoder;
import com.mm.engine.framework.entrance.code.net.http.HttpEncoder;
import com.mm.engine.framework.data.persistence.dao.DataAccessor;
import com.mm.engine.framework.data.persistence.ds.DataSourceFactory;
import com.mm.engine.framework.tool.helper.ConfigHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/16.
 */
public final class EngineConfigure {
    private Map<Class<?>,Class<?>> configureBeans=new HashMap<Class<?>,Class<?>>();
    private String defaultRequestController;
    private int updateCycle=1000;
    private int sessionCycle = 1000;
    private int sessionSurvivalTime = 1000*60*10;

    public EngineConfigure(){
        // 初始化配置：从配置文件中读取
        configureBeans.put(HttpEncoder.class,getBeanFromConfigure("httpEncoder"));
        configureBeans.put(HttpDecoder.class,getBeanFromConfigure("httpDecoder"));
        configureBeans.put(DataSourceFactory.class,getBeanFromConfigure("dataSourceFactory"));
        configureBeans.put(DataAccessor.class,getBeanFromConfigure("dataAccessor"));
        configureBeans.put(CacheCenter.class,getBeanFromConfigure("cacheCenter"));

        defaultRequestController="DefaultRequestController";
    }
    private Class<?> getBeanFromConfigure(String beanType){
        String classPath= ConfigHelper.getString(beanType);
        if(StringUtils.isEmpty(classPath)){
            throw new RuntimeException("class bean set is Invalid ,value is "+classPath);
        }
        Class<?> result;
        try {
            result=Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("class bean set is Invalid ,value is "+classPath);
        }
        return result;
    }

    public EngineConfigure setHttpEncode(Class<HttpEncoder> httpEncodeClass){
        configureBeans.put(HttpEncoder.class,httpEncodeClass);
        return this;
    }
    public EngineConfigure setHttpDecode(Class<HttpDecoder> httpDecodeClass){
        configureBeans.put(HttpDecoder.class,httpDecodeClass);
        return this;
    }
    public  Map<Class<?>,Class<?>> getConfigureBeans(){
        return configureBeans;
    }
    public String getDefaultRequestController(){
        return defaultRequestController;
    }

    public int getSessionSurvivalTime(){
        return sessionSurvivalTime;
    }
    public int getSessionCycle(){return sessionCycle;}
    public int getUpdateCycle() {
        return updateCycle;
    }
    public String getString(String key){
        return ConfigHelper.getString(key);
    }
}
