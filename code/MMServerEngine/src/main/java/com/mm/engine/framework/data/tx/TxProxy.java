package com.mm.engine.framework.data.tx;

import com.mm.engine.framework.control.aop.AspectProxy;
import com.mm.engine.framework.control.aop.annotation.Aspect;
import com.mm.engine.framework.tool.helper.BeanHelper;

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
    private TxCacheService txCacheService;
    // 在构造函数中添加恐怕有问题
//    public TxProxy(){
//        txCacheService = BeanHelper.getServiceBean(TxCacheService.class);
//    }
    @Override
    public void before(Object object,Class<?> cls, Method method, Object[] params) {
        Tx tx = method.getAnnotation(Tx.class);
        if(txCacheService == null){
            // 这个地方不用加锁，因为多个线程获取的都是同一个
            txCacheService = BeanHelper.getServiceBean(TxCacheService.class);
        }
        txCacheService.begin(tx.tx(),tx.lock(),tx.lockClass());
    }

    @Override
    public void after(Object object,Class<?> cls, Method method, Object[] params, Object result) {
        boolean success = txCacheService.after();
        //
        if(success){ // 没有事务或事务提交成功

        }
    }
}
