package com.mm.engine.sysBean;

import com.mm.engine.framework.control.annotation.Request;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.aop.AspectProxy;
import com.mm.engine.framework.control.aop.annotation.Aspect;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2015/11/17.
 */

@Aspect(
        mark = {"aa"}
//        annotation = {Request.class}
        //pkg = {"com.mm.engine.sysBean.controller"}
)
public class MyProxy extends AspectProxy {
    @Override
    public void before(Object object,Class<?> cls, Method method, Object[] params) {
        System.out.println("before");
    }

    @Override
    public void after(Object object,Class<?> cls, Method method, Object[] params, Object result) {
        System.out.println("after");
    }
}
