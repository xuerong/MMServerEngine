package com.mm.engine.framework.control.netEvent.remote;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.control.annotation.NetEventListener;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.NetEventService;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.ReflectionUtil;

import java.lang.reflect.Method;

/**
 * Created by apple on 16-10-4.
 */
@Service(init = "init")
public class BroadcastRPCService {
    private NetEventService netEventService;

    private ThreadLocal<Boolean> isRpc = new ThreadLocal<Boolean>(){
        @Override
        protected Boolean initialValue() {
            return true;
        }
    };

    public void init(){

    }
    public void afterMethod(Object object, Class<?> cls, Method method, Object[] params, Object result){
        if(isRpc.get()){
            BroadcastRPC broadcastRPC = method.getAnnotation(BroadcastRPC.class);
            NetEventData netEventData = new NetEventData(SysConstantDefine.broadcastRPC);
            RemoteCallData remoteCallData = new RemoteCallData();
            Class<?> originClass = ServiceHelper.getOriginServiceClass(cls);
            remoteCallData.setCls(originClass);
            remoteCallData.setMethodName(method.getName());
            remoteCallData.setParams(params);
            netEventData.setParam(remoteCallData);
            if(broadcastRPC.async()){
                netEventService.broadcastNetEvent(netEventData,false);
            }else {
                netEventService.broadcastNetEventSyn(netEventData,false);
            }
        }
    }

    @NetEventListener(netEvent = SysConstantDefine.broadcastRPC)
    public NetEventData receiveBroadcastRPC(NetEventData netEventData){
        isRpc.set(false);
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
        }finally {
            isRpc.set(true);
        }
    }
}
