package com.mm.engine.framework.control.request;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.BeanHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/17.
 */
@Service(init = "init")
public class RequestService {
    private Map<Integer,RequestHandler> handlerMap=new HashMap<Integer,RequestHandler>();
    public void init(){
        TIntObjectHashMap<Class<?>> requestHandlerClassMap = ServiceHelper.getRequestHandlerMap();
        requestHandlerClassMap.forEachEntry(new TIntObjectProcedure<Class<?>>(){
            @Override
            public boolean execute(int i, Class<?> aClass) {
                handlerMap.put(i, (RequestHandler)BeanHelper.getServiceBean(aClass));
                return true;
            }
        });
    }


    public RetPacket handle(int opcode,Object clientData, Session session) throws Exception{
        RequestHandler handler = handlerMap.get(opcode);
        if(handler == null){
            throw new MMException("can't find handler of "+opcode);
        }
        return handler.handle(opcode,clientData,session);
    }
}
