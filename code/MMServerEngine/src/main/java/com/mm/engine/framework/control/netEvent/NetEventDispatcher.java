package com.mm.engine.framework.control.netEvent;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.tool.helper.BeanHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/12/30.
 */
public class NetEventDispatcher {
    private static Map<Integer,NetEventListenerHandler> handlerMap=new HashMap<Integer,NetEventListenerHandler>();
    static {
        TIntObjectHashMap<Class<?>> netEventHandlerClassMap = ServiceHelper.getNetEventListenerHandlerClassMap();
        netEventHandlerClassMap.forEachEntry(new TIntObjectProcedure<Class<?>>(){
            @Override
            public boolean execute(int i, Class<?> aClass) {
                handlerMap.put(i, BeanHelper.getServiceBean(aClass));
                return true;
            }
        });
    }
    public static NetEventListenerHandler getHandler(int netEvent){
        return  handlerMap.get(netEvent);
    }
}
