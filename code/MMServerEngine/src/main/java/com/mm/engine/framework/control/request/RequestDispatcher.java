package com.mm.engine.framework.control.request;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.tool.helper.BeanHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/17.
 */
public final class RequestDispatcher {
    private static Map<Integer,RequestHandler> handlerMap=new HashMap<Integer,RequestHandler>();
    static {
        TIntObjectHashMap<Class<?>> requestHandlerClassMap = ServiceHelper.getRequestHandlerMap();
        requestHandlerClassMap.forEachEntry(new TIntObjectProcedure<Class<?>>(){
            @Override
            public boolean execute(int i, Class<?> aClass) {
                handlerMap.put(i, BeanHelper.getServiceBean(aClass));
                return true;
            }
        });
    }
    public static RequestHandler getHandler(int opcode){
        return  handlerMap.get(opcode);
    }
}
