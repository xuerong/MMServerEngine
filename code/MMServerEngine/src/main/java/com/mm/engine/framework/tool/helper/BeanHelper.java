package com.mm.engine.framework.tool.helper;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.aop.AopHelper;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.configure.EngineConfigure;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.server.configure.EntranceConfigure;
import com.mm.engine.sysBean.MyProxyTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private final static Map<String,Entrance> entranceBeans = new HashMap<>();

    public static Map<Class<?>, Object> getServiceBeans() {
        return serviceBeans;
    }

    public static Map<String, Entrance> getEntranceBeans() {
        return entranceBeans;
    }

    /**
     *
     * 添加实例化的时候别忘了验证是否已经存在
     * */
    private static Map<Class<?>, Class<?>> configureBeans;
    static{
        try {
            // service
            Map<Class<?>, Class<?>> serviceClassMap = ServiceHelper.getServiceClassMap();
            for (Map.Entry<Class<?>, Class<?>> entry : serviceClassMap.entrySet()) {
                serviceBeans.put(entry.getKey(), newAopInstance(entry.getKey(), entry.getValue()));
            }

            // 框架Bean
            EngineConfigure configure = Server.getEngineConfigure();
            configureBeans = configure.getConfigureBeans();
            for (Map.Entry<Class<?>, Class<?>> entry : configureBeans.entrySet()) {
                // 由于在实例化一个的时候，可能用到另外一个，就在get的时候实例化了
                // 首先看是否是service，如果是，从service中获取，否则，创建
                if(!frameBeans.containsKey(entry.getKey())) {
                    if(entry.getValue().getAnnotation(Service.class)!=null){
                        Object object = serviceBeans.get(entry.getValue());// 注意：这里是value
                        if(object == null){
                            throw new MMException("service frameBean fail:"+entry.getValue());
                        }
                        frameBeans.put(entry.getKey(), object);
                    }else{
                        Object object = newAopInstance(entry.getValue());
                        frameBeans.put(entry.getKey(), object);
                    }
                }
            }
            // Ioc: 如果bean中有声明的serviceBeans中存在的变量，则赋值
            iocSetService();
            // aop
//            frameBeans.put(MyProxyTarget.class, newAopInstance(MyProxyTarget.class));
            // net
            Map<String,EntranceConfigure> entranceClassMap = configure.getEntranceClassMap();
            for (EntranceConfigure entranceConfigure:entranceClassMap.values()) {

                Entrance entrance = (Entrance)serviceBeans.get(entranceConfigure.getCls()) ; // 入口也可能声明为service
                if(entrance == null){
                    entrance = (Entrance) newAopInstance(entranceConfigure.getCls());
                }
                Method nameMethod = Entrance.class.getMethod("setName",String.class);
                nameMethod.invoke(entrance,entranceConfigure.getName());
                Method portMethod = Entrance.class.getMethod("setPort",int.class);
                portMethod.invoke(entrance,entranceConfigure.getPort());

                entranceBeans.put(entranceConfigure.getName(),entrance);
                //-----ioc
                iocSetObject(entranceConfigure.getCls(),entrance);
            }

        }catch (Throwable e){
            e.printStackTrace();
            log.error("init BeanHelper fail");
        }
    }
    // 给beans中的对象的service变量赋值
    private static void iocSetService() throws Throwable{
        // service
        for(Map.Entry<Class<?>,Object> entry : serviceBeans.entrySet()){
            Class<?> cls = entry.getKey();
            iocSetObject(cls,entry.getValue());
        }
        // frame
        // ioc
        for (Map.Entry<Class<?>, Class<?>> entry : configureBeans.entrySet()) {
            Object object = frameBeans.get(entry.getKey());
            iocSetObject(entry.getValue(),object);
        }
    }
    private static void iocSetObject(Class sourceClass,Object object) throws Throwable{
        Field[] fields = sourceClass.getDeclaredFields();
        for(Field field : fields){
            Object service = serviceBeans.get(field.getType());
            if(service == null){
                service = frameBeans.get(field.getType());
            }
            if(service != null){
                field.setAccessible(true); // 可访问私有变量。
                field.set(object,service);
            }
        }
    }

    // Bean类的实例化，之前需要先加aop : 目标类等于代理类
    public static <T> T newAopInstance(Class<T> cls){
        T reCls = AopHelper.getProxyObject(cls);
        return reCls;
    }
    //  : 目标类不等于代理类
    public static <T> T newAopInstance(Class<?> keyCls, Class<T> newCls){
        T reCls = AopHelper.getProxyObject(keyCls,newCls);
        return reCls;
    }

    /**
     * 获取框架所用的bean，这部分bean从配置文件中读取其实现类，或者在引擎初始化的时候设定它，使用者可以自定义之
     *
     * 由于在实例化一个的时候，可能用到另外一个，就在这里实例化了
     * **/
    public static <T> T getFrameBean(Class<T> cls){
        Object t = frameBeans.get(cls);
        if(t == null){
            Class<?> c = configureBeans.get(cls);
            if(c != null){
                t = newAopInstance(c);
                frameBeans.put(cls, t);
            }else{
                log.error("con't get frame bean by class" + cls);
            throw new RuntimeException("con't get frame bean by class" + cls);
        }
        }
        return (T)t;
    }
    /**
     * 获取ServiceBean
     * **/
    public static <T> T getServiceBean(Class<T> cls){
        Object t = serviceBeans.get(cls);
        if(t == null){
            log.error("con't get service bean by class"+cls);
            throw new RuntimeException("con't get service bean by class"+cls);
        }
        return (T)t;
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
