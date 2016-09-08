package com.mm.engine.framework.net.entrance;

import com.mm.engine.framework.control.aop.AspectProxy;
import com.mm.engine.framework.control.aop.annotation.Aspect;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.control.event.EventService;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;

import java.lang.reflect.Method;

/**
 * Created by a on 2016/9/6.
 */
@Aspect(
        mark = {"EntranceStart"}
)
public class EntranceProxy extends AspectProxy {

    @Override
    public void before(Object object,Class<?> cls, Method method, Object[] params) {
//        System.out.println(cls.getName()+","+method.getName()+" start before!");
    }

    @Override
    public void after(Object object,Class<?> cls, Method method, Object[] params, Object result) {
//        System.out.println(cls.getName()+","+method.getName()+" start end!");
        EventService eventService = BeanHelper.getServiceBean(EventService.class);
        EventData eventData = new EventData(SysConstantDefine.Event_EntranceStart);
        eventData.setData(object);
        eventService.fireEvent(eventData);
    }
}
