package com.mm.engine.framework.server;

import com.mm.engine.framework.control.job.JobStorage;
import com.mm.engine.framework.data.cache.CacheCenter;
import com.mm.engine.framework.entrance.Entrance;
import com.mm.engine.framework.entrance.code.net.http.HttpDecoder;
import com.mm.engine.framework.entrance.code.net.http.HttpEncoder;
import com.mm.engine.framework.data.persistence.dao.DataAccessor;
import com.mm.engine.framework.data.persistence.ds.DataSourceFactory;
import com.mm.engine.framework.entrance.http.EntranceJetty;
import com.mm.engine.framework.entrance.socket.NetEventNettyEntrance;
import com.mm.engine.framework.exception.MMException;
import com.mm.engine.framework.tool.helper.ConfigHelper;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private int netEventPort = 8001;

    // 系统开启的网络入口
    private final List<Entrance> entranceList = new ArrayList<Entrance>();
    public EngineConfigure(){
        this(null);
    }
    public EngineConfigure(String serverTypeStr){
        this(serverTypeStr,8000);
    }
    public EngineConfigure(String serverTypeStr,int netEventPort){
        if(serverTypeStr!=null){
            ServerType.setServerType(serverTypeStr);
        }else{
            // 从配置文件中取server类型，如果没有，就是默认类型nodeServer
        }
        // 初始化配置：从配置文件中读取
        configureBeans.put(HttpEncoder.class,getBeanFromConfigure("httpEncoder"));
        configureBeans.put(HttpDecoder.class,getBeanFromConfigure("httpDecoder"));
        configureBeans.put(DataSourceFactory.class,getBeanFromConfigure("dataSourceFactory"));
        configureBeans.put(DataAccessor.class,getBeanFromConfigure("dataAccessor"));
        configureBeans.put(CacheCenter.class,getBeanFromConfigure("cacheCenter"));
        configureBeans.put(JobStorage.class,getBeanFromConfigure("jobStorage"));

        defaultRequestController="DefaultRequestController";

        entranceList.add(new EntranceJetty("first",8080));

        this.netEventPort = netEventPort;
        entranceList.add(new NetEventNettyEntrance("NetEventNettyEntrance",netEventPort));
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
    public List<Entrance> getEntranceList() {
        return entranceList;
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
