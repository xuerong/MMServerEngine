package com.mm.engine.framework.tool.helper;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.control.aop.AopHelper;
import com.mm.engine.framework.server.EngineConfigure;
import com.mm.engine.framework.server.Server;
import com.mm.engine.sysBean.MyProxyTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/16.
 * 存储所有的bean的实例，包括框架所用的bean，引擎服务所用的bean和用户服务所用的bean
 *
 * 注意，不设置注册bean的函数，所有的bean的初始化都在static中完成
 */
public final class BeanHelper {
    private static final Logger log = LoggerFactory.getLogger(BeanHelper.class);
    private final static Map<Class<?>,Object> frameBeans=new HashMap<Class<?>,Object>();
    private final static Map<Class<?>,Object> engineBeans=new HashMap<Class<?>,Object>();
    private final static Map<Class<?>,Object> userBeans=new HashMap<Class<?>,Object>();

    private final static Map<Class<?>,Object> serviceBeans=new HashMap<Class<?>,Object>();

    public static Map<Class<?>, Object> getServiceBeans() {
        return serviceBeans;
    }

    /**
     *
     * 添加实例化的时候别忘了验证是否已经存在
     * */
    private static Map<Class<?>, Class<?>> configureBeans;
    static{
        try {
            // 框架Bean
            EngineConfigure configure = Server.getEngineConfigure();
            configureBeans = configure.getConfigureBeans();
            for (Map.Entry<Class<?>, Class<?>> entry : configureBeans.entrySet()) {
                // 由于在实例化一个的时候，可能用到另外一个，就在get的时候实例化了
                if(!frameBeans.containsKey(entry.getKey())) {
                    frameBeans.put(entry.getKey(), newInstance(entry.getValue()));
                }
            }
            // service
            Map<Class<?>, Class<?>> serviceClassMap = ServiceHelper.getServiceClassList();
            for (Map.Entry<Class<?>, Class<?>> entry : serviceClassMap.entrySet()) {
                serviceBeans.put(entry.getKey(), newInstance(entry.getKey(), entry.getValue()));
            }
            // aop
            frameBeans.put(MyProxyTarget.class, newInstance(MyProxyTarget.class));
        }catch (Throwable e){
            e.printStackTrace();
            log.error("init BeanHelper fail");
        }
    }
    // Bean类的实例化，之前需要先加aop
    private static <T> T newInstance(Class<T> cls){
        T reCls = AopHelper.getProxyObject(cls);
        return reCls;
    }
    private static <T> T newInstance(Class<?> keyCls,Class<T> newCls){
        T reCls = AopHelper.getProxyObject(keyCls,newCls);
        return reCls;
    }

    /**
     * 获取框架所用的bean，这部分bean从配置文件中读取其实现类，或者在引擎初始化的时候设定它，使用者可以自定义之
     *
     * 由于在实例化一个的时候，可能用到另外一个，就在这里实例化了
     * **/
    public static <T> T getFrameBean(Class<T> cls){
        if(!frameBeans.containsKey(cls)){
            if(configureBeans.containsKey(cls)){
                frameBeans.put(cls, newInstance(configureBeans.get(cls)));
            }else {
                log.error("con't get frame bean by class" + cls);
                throw new RuntimeException("con't get frame bean by class" + cls);
            }
        }
        return (T)frameBeans.get(cls);
    }
    /**
     * 获取ServiceBean
     * **/
    public static <T> T getServiceBean(Class<?> cls){
        if(!serviceBeans.containsKey(cls)){
            log.error("con't get service bean by class"+cls);
            throw new RuntimeException("con't get service bean by class"+cls);
        }
        return (T)serviceBeans.get(cls);
    }
    /**
     * 测试aop功能
     *
     * **/
    public static  void main(String[] args){
        MyProxyTarget test =BeanHelper.getFrameBean(MyProxyTarget.class);
        if(test==null){
            System.out.println("sss fail");
        }
        test.p1();
        System.out.println("---------------");
        test.p2();
        System.out.println("---------------");
        test.p3();
        System.out.println("---------------");
        test.p4();
    }
}
