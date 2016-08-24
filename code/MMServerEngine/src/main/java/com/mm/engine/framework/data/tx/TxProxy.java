package com.mm.engine.framework.data.tx;

import com.mm.engine.framework.control.annotation.*;
import com.mm.engine.framework.control.aop.AspectProxy;
import com.mm.engine.framework.control.aop.annotation.Aspect;

import java.lang.reflect.Method;

/**
 * Created by apple on 16-8-21.
 * 对四种服务进行切面,查看对应的函数是否添加了事务
 *
 */
@Aspect(
        annotation = {Tx.class
        }
)
public class TxProxy extends AspectProxy {
    @Override
    public void before(Class<?> cls, Method method, Object[] params) {
        Tx tx = method.getAnnotation(Tx.class);
        ThreadLocalTxCache.begin(tx.tx(),tx.lock(),tx.lockClass());
    }

    @Override
    public void after(Class<?> cls, Method method, Object[] params, Object result) {
        boolean success = ThreadLocalTxCache.after();
        //
        if(success){ // 没有事务或事务提交成功

        }
    }
}
