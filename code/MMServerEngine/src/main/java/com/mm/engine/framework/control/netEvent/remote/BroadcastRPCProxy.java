package com.mm.engine.framework.control.netEvent.remote;

import com.mm.engine.framework.control.aop.AspectProxy;
import com.mm.engine.framework.control.aop.annotation.Aspect;
import com.mm.engine.framework.data.sysPara.SysPara;
import com.mm.engine.framework.tool.helper.BeanHelper;

import java.lang.reflect.Method;

/**
 * Created by apple on 16-10-4.
 */
@Aspect(
        annotation = {BroadcastRPC.class}
)
public class BroadcastRPCProxy extends AspectProxy {
    private BroadcastRPCService broadcastRPCService ;
    @Override
    public void before(Object object, Class<?> cls, Method method, Object[] params) {
        if(broadcastRPCService == null){
            broadcastRPCService = BeanHelper.getServiceBean(BroadcastRPCService.class);
        }
    }

    @Override
    public void after(Object object, Class<?> cls, Method method, Object[] params, Object result) {
        broadcastRPCService.afterMethod(object,cls,method,params,result);
    }
}
