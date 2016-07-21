package com.mm.engine.framework.control.annotation;

import com.mm.engine.framework.server.Server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2015/11/16.
 * 更新器，用于做定时更新
 * 添加此注解的方法将在每隔一段时间被调用一次
 * Updatable分为同步和异步
 *
 * 对应的方法存在一个参数。实际的更新时间间隔
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Updatable {
    /**
     * 同步还是异步
     * 同步：将按照系统设置的更新周期更新，并且同步更新的最好不要有较多花费时间的操作
     * 异步：异步将用下面interval设置的更新频率更新，每个更新都是异步执行，不相关系链
     *
     * 默认是异步的，只需设置interval即可，如果不设置，将按照系统更新周期更新
     * 如果需要同步的，只需要设置isAsynchronous为false即可
     * */
    boolean isAsynchronous() default true;

    /**
     * interval,更新周期，毫秒计
     * 异步更新时有效
     * **/
    int cycle() default -1;
}
