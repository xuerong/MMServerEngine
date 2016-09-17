package com.mm.engine.framework.control.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2015/11/16.
 * Service用来提供服务，包括：
 * 用户请求服务@Request
 * 监听事件服务@EventListener
 * 更新服务@Updatable
 * 监听远程调用服务@NetEventListener
 *
 * TODO 添加runOnEveryServer
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * 初始化方法
     * @return
     */
    String init() default "";

    /**
     * 销毁方法
     * @return
     */
    String destroy() default "";
    /**
     *
     */
    boolean runOnEveryServer() default true;
}
