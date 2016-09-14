package com.mm.engine.framework.server.configure;

import com.mm.engine.framework.control.job.JobStorage;
import com.mm.engine.framework.data.cache.CacheCenter;
import com.mm.engine.framework.data.persistence.dao.DataAccessor;
import com.mm.engine.framework.data.persistence.ds.DataSourceFactory;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.tool.helper.ConfigHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by Administrator on 2015/11/16.
 */
public final class EngineConfigure {
    private Map<Class<?>,Class<?>> configureBeans=new HashMap<Class<?>,Class<?>>();
    private String defaultRequestController;
    private int syncUpdateCycle = 1000;
    private int netEventPort = 8001;

    // 系统开启的网络入口
    private final Map<String,EntranceConfigure> entranceClassMap = new HashMap<>();
    public EntranceConfigure netEventEntrance;
    public EntranceConfigure requestEntrance;
    // session update:貌似不能定义系统参数

    //
    public EngineConfigure(){
        this(null);
    }
    public EngineConfigure(String serverTypeStr){
        if(serverTypeStr!=null){
            ServerType.setServerType(serverTypeStr);
        }else{
            // 从配置文件中取server类型，如果没有，就是默认类型nodeServer
        }
        // 初始化配置：从配置文件中读取
        configureBeans.put(DataSourceFactory.class,getBeanFromConfigure("dataSourceFactory"));
        configureBeans.put(DataAccessor.class,getBeanFromConfigure("dataAccessor"));
        configureBeans.put(CacheCenter.class,getBeanFromConfigure("cacheCenter"));
        configureBeans.put(JobStorage.class,getBeanFromConfigure("jobStorage"));

        defaultRequestController="DefaultRequestController";
        // 初始化入口
        initEntrance();
        // session.cycle
//        sessionUpdateCycle = Integer.parseInt(getString("session.cycle"));
        // syncUpdate.cycle
        syncUpdateCycle = Integer.parseInt(getString("syncUpdate.cycle"));
    }

    private void initEntrance(){
        // entrance
        Map<String, Object> entranceMap = ConfigHelper.getMap("entrance");
        Set<String> nameSet = new HashSet<>();
        for(Map.Entry<String,Object> entry : entranceMap.entrySet()){
            String name = entry.getKey().replace("entrance.","").replace(".port","").replace(".class","");
            if(nameSet.contains(name)){
                continue;
            }
            nameSet.add(name);
            String portStr = ConfigHelper.getString("entrance."+name+".port");
            String clsStr = ConfigHelper.getString("entrance."+name+".class");

            int port = Integer.parseInt(portStr);
            Class cls;
            try {
                cls = Class.forName(clsStr);
            } catch (ClassNotFoundException e) {
                throw new MMException(e);
            }
            EntranceConfigure configure = new EntranceConfigure();
            configure.setName(name);
            configure.setPort(port);
            configure.setCls(cls);
            entranceClassMap.put(name,configure);
            if(name.equals("netEvent")){
                netEventEntrance = configure;
            }
            if(name.equals("request")){
                requestEntrance = configure;
            }
        }
        if(netEventEntrance == null){
            throw new MMException("configure has no netEventEntrance");
        }
        if(requestEntrance == null){
            throw new MMException("configure has no requestEntrance");
        }
    }

    public EntranceConfigure getNetEventEntrance() {
        return netEventEntrance;
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

    public  Map<Class<?>,Class<?>> getConfigureBeans(){
        return configureBeans;
    }
    public String getDefaultRequestController(){
        return defaultRequestController;
    }

    public int getSyncUpdateCycle() {
        return syncUpdateCycle;
    }
    public String getString(String key){
        return ConfigHelper.getString(key);
    }

    public Map<String, EntranceConfigure> getEntranceClassMap() {
        return entranceClassMap;
    }

    public boolean isAsyncServer(){ // 是否是异步服务器
        return true;
    }
    public int getNetEventPort(){
        return netEventPort;
    }

    public String getMainServerNetEventAdd(){
        return "localhost:8000";
    }
}
