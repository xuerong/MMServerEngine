package com.mm.engine.framework.control.netEvent.remote;

import com.mm.engine.framework.control.annotation.NetEventListener;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.NetEventService;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by apple on 16-9-15.
 * TODO 把所有的方法补全
 */
@Service(init = "init")
public class RemoteCallService {
    private NetEventService netEventService;
    public void init(){
        netEventService = BeanHelper.getServiceBean(NetEventService.class);
    }
    // 缓存下来,提高效率
    public Map<String,RemoteMethodCache> remoteMethodCacheMap = new HashMap<>();

    /**
     * 异步远程调用
     */
    public void remoteCallMainServer(Class cls,String methodName,Object... params){
        NetEventData netEventData = new NetEventData(SysConstantDefine.remoteCall);
//        Method method = null;
//        try {
//            method = cls.getMethod(methodName, ReflectionUtil.getParamsTypes(params));
//        }catch (NoSuchMethodException e){
//            throw new MMException(e);
//        }

        RemoteCallData remoteCallData = new RemoteCallData();
        remoteCallData.setCls(cls);
        remoteCallData.setMethodName(methodName);
        remoteCallData.setParams(params);
        netEventData.setParam(remoteCallData);
        netEventService.fireMainServerNetEvent(netEventData);
    }
    @NetEventListener(netEvent = SysConstantDefine.remoteCall)
    public NetEventData receiveRemoteCall(NetEventData netEventData){
        RemoteCallData remoteCallData = (RemoteCallData)netEventData.getParam();
        try {
            Method method = remoteCallData.getCls().getMethod(remoteCallData.getMethodName(),
                        ReflectionUtil.getParamsTypes(remoteCallData.getParams()));
            Object serviceObject = BeanHelper.getServiceBean(remoteCallData.getCls());
            Object ret = method.invoke(serviceObject, remoteCallData.getParams());
            if(!(ret instanceof Void)) {
                netEventData.setParam(ret);
            }
            return netEventData;
        }catch (Throwable e){
            throw new MMException(e);
        }
    }

    /**
     * 同步远程调用
     */
    public Object remoteCallMainServerSyn(Class cls,String methodName,Object... params){
        NetEventData netEventData = new NetEventData(SysConstantDefine.remoteCall);
        RemoteCallData remoteCallData = new RemoteCallData();
        remoteCallData.setCls(cls);
        remoteCallData.setMethodName(methodName);
        remoteCallData.setParams(params);
        netEventData.setParam(remoteCallData);

        NetEventData retData = netEventService.fireMainServerNetEventSyn(netEventData);
        return retData.getParam();
    }

    public Object remoteCallSyn(String  add,Class cls,String methodName,Object... params){
        NetEventData netEventData = new NetEventData(SysConstantDefine.remoteCall);
        RemoteCallData remoteCallData = new RemoteCallData();
        remoteCallData.setCls(cls);
        remoteCallData.setMethodName(methodName);
        remoteCallData.setParams(params);
        netEventData.setParam(remoteCallData);

        NetEventData retData = netEventService.fireServerNetEventSyn(add,netEventData);
        return retData.getParam();
    }

    public Map<String,Object> broadcastRemoteCallSyn(Class cls, String methodName, Object... params){
        NetEventData netEventData = new NetEventData(SysConstantDefine.remoteCall);
        RemoteCallData remoteCallData = new RemoteCallData();
        remoteCallData.setCls(cls);
        remoteCallData.setMethodName(methodName);
        remoteCallData.setParams(params);
        netEventData.setParam(remoteCallData);

        Map<String,NetEventData> retData = netEventService.broadcastNetEventSyn(netEventData,false);
        Map result = new HashMap(retData.size());
        for(Map.Entry<String,NetEventData> re : retData.entrySet()){
            result.put(re.getKey(),re.getValue().getParam());
        }
        return result;
    }

    class RemoteMethodCache{
        private Method method;
        private Object serviceObject;

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getServiceObject() {
            return serviceObject;
        }

        public void setServiceObject(Object serviceObject) {
            this.serviceObject = serviceObject;
        }
    }
}
