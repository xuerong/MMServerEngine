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
        annotation = {Request.class,
                NetEventListener.class,
                EventListener.class,
                Updatable.class
        }
)
public class TxProxy extends AspectProxy {
    @Override
    public void before(Class<?> cls, Method method, Object[] params) {
        Tx tx = method.getAnnotation(Tx.class);
        if(!tx.tx()){
            ThreadLocalTxCache.setTXState(TxState.Absent);
            return;
        }
        ThreadLocalTxCache.setTXState(TxState.In);
    }

    @Override
    public void after(Class<?> cls, Method method, Object[] params, Object result) {
        if(!ThreadLocalTxCache.isInTx()){
            return;
        }
        ThreadLocalTxCache.setTXState(TxState.Committing);
        boolean success = ThreadLocalTxCache.commit();

    }
}
