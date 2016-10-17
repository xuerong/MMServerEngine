package com.mm.engine.framework.control;

import com.mm.engine.framework.control.gm.Gm;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.control.annotation.*;
import com.mm.engine.framework.control.annotation.EventListener;
import com.mm.engine.framework.control.event.EventListenerHandler;
import com.mm.engine.framework.control.netEvent.NetEventListenerHandler;
import com.mm.engine.framework.control.request.RequestHandler;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.tool.helper.ClassHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.MethodInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * Created by Administrator on 2015/11/18.
 * ServiceHelper
 * 在系统启动的时候，找到所有的Service并用对应的Handler封装，
 * 然后传递给对应的管理器,分别为：
 * Request-RequestHandler-RequestService
 * EventListener-EventListenerHandler-EventService
 * NetEventListener-NetEventListenerHandler-NetEventService
 * Updatable-
 */
public final class ServiceHelper {
    private static TIntObjectHashMap<Class<?>> requestHandlerClassMap=new TIntObjectHashMap<>();
    private static TShortObjectHashMap<Set<Class<?>>> eventListenerHandlerClassMap=new TShortObjectHashMap<>();
    private static TIntObjectHashMap<Class<?>> netEventListenerHandlerClassMap=new TIntObjectHashMap<>();
    private static Map<Class<?>,List<Method>> updatableClassMap=new HashMap<>();

    /**
     * BeanHelp获取该Map对Service进行实例化，
     * 这样确保了同一个Service只实例化一次
     * */
    private static Map<Class<?>,Class<?>> serviceClassMap=new HashMap<>();
    private final static Map<Class<?>,Class<?>> serviceOriginClass = new HashMap<>();

    // 各个service的初始化方法和销毁方法,threeMap默认是根据键之排序的
    private static Map<Integer,Map<Class<?>,Method>> initMethodMap = new TreeMap<>();
    private static Map<Class<?>,Method> destroyMethodMap = new HashMap<>();
    private static Map<String,Method> gmMethod = new HashMap<>();

    static{
        try {
            //j2ee下需要设置:且后面跟的类视乎没有什么关系，有待研究
            ClassPool.getDefault().insertClassPath(new ClassClassPath(Service.class));

            Map<Class<?>,List<Method>> requestMap=new HashMap<>();
            Map<Class<?>,List<Method>> eventListenerMap=new HashMap<>();
            Map<Class<?>,List<Method>> netEventListenerMap=new HashMap<>();
            Map<Class<?>,List<Method>> updatableMap=new HashMap<>();
            List<Class<?>> serviceClasses= ClassHelper.getClassListByAnnotation(Service.class);
//            for(Class<?> serviceClass : serviceClasses){
//                ClassPool pool = ClassPool.getDefault();
//                CtClass oldClass =  pool.get(serviceClass.getName());
//                System.out.println(serviceClass.getName()+" before:"+serviceClass.getDeclaredMethods().length);
//                CtMethod r = oldClass.getDeclaredMethods()[0];
//                oldClass.removeMethod(r);
//                System.out.println(serviceClass.getName()+" after:"+serviceClass.getDeclaredMethods().length);
////                oldClass.addMethod(r);
////                System.out.println(serviceClass.getName()+" end:"+serviceClass.getDeclaredMethods().length);
////                CtClass cls = pool.makeClass(oldClass.getName()+"$Aaaaa",oldClass);
////
//                System.out.println(oldClass.toClass() == serviceClass);
//            }
            for(Class<?> serviceClass : serviceClasses){
                Service service = serviceClass.getAnnotation(Service.class);
                String init = service.init();
                int initPriority = service.initPriority();
                String destroy = service.destroy();
                Method[] methods=serviceClass.getMethods();
                for (Method method : methods){
                    // 判断是否存在Request
                    if(method.isAnnotationPresent(Request.class)){
                        addMethodToMap(requestMap,serviceClass,method);
                    }
                    //
                    if(!service.runOnEveryServer() && !ServerType.isMainServer()){
                        continue;
                    }
                    if(method.isAnnotationPresent(EventListener.class)){
                        addMethodToMap(eventListenerMap,serviceClass,method);
                    }
                    if(method.isAnnotationPresent(NetEventListener.class)){
                        addMethodToMap(netEventListenerMap,serviceClass,method);
                    }
                    if(method.isAnnotationPresent(Updatable.class)){
                        addMethodToMap(updatableMap,serviceClass,method);
                    }
                    // 判断是否是初始化方法和销毁方法
                    if(method.getName().equals(init)){
                        Map<Class<?>,Method> methodMap = initMethodMap.get(initPriority);
                        if(methodMap == null){
                            methodMap = new HashMap<>();
                            initMethodMap.put(initPriority,methodMap);
                        }
                        methodMap.put(serviceClass,method);
                    }
                    if(method.getName().equals(destroy)){
                        destroyMethodMap.put(serviceClass,method);
                    }
                    Gm gm = method.getAnnotation(Gm.class);
                    if(gm != null){
                        Method old = gmMethod.put(gm.id(),method);
                        if(old != null){
                            throw new MMException("gm id duplicate,id="+gm.id()+" at "+method.getDeclaringClass().getName()+"."+method.getName()
                                    +" and "+old.getDeclaringClass().getName()+"."+old.getName());
                        }
                    }
                }
            }

            /**
             * 获取每个Service被引用的情况，并生成新的Class
             *
             * 这一步生成需要修改BeanHelper中的对应的类和实例化对象
             * */
            for(Class<?> serviceClass : serviceClasses){
                // 对于request，用opcode导航
                List<Short> opcodeList=null;
                List<Short> eventList=null;
                List<Integer> netEventList=null;

                Class<?> newServiceClass=serviceClass;

                Service service = serviceClass.getAnnotation(Service.class);
                // TODO 这里用的isMainServer,可考虑修改
                if(!service.runOnEveryServer() && !ServerType.isMainServer()){
                    // 改写所有的public方法,使用远程调用
                    ClassPool pool = ClassPool.getDefault();
                    CtClass oldClass =  pool.get(serviceClass.getName());
                    CtClass cls = pool.makeClass(oldClass.getName()+"$Proxy",oldClass);

                    for(CtMethod ctMethod : oldClass.getDeclaredMethods()){
                        MethodInfo methodInfo = ctMethod.getMethodInfo();
                        if(methodInfo.getAccessFlags() == 1){ // public 的
                            // 重写该方法
                            StringBuilder sb = new StringBuilder(ctMethod.getReturnType().getName()+" "+ctMethod.getName()+"(" );
                            CtClass[] paramClasses = ctMethod.getParameterTypes();
                            int i=0;
                            StringBuilder paramsStr = new StringBuilder();
                            for(CtClass paramClass : paramClasses){
                                if(i > 0){
                                    sb.append(",");
                                    paramsStr.append(",");
                                }
                                sb.append(paramClass.getName()+" p"+i);
                                // 这个地方需要进行一步强制转换,基本类型不能编译成Object类型
                                paramsStr.append(praseBaseTypeStrToObjectTypeStr(paramClass.getName(),"p"+i));
                                i++;
                            }
                            sb.append(") {");
                            String paramStr = "null";
                            if(i >0){
                                sb.append("Object[] param = new Object[]{"+paramsStr+"};");
                                paramStr = "param";
                            }
                            // --------------这个地方要进行强制转换
                            sb.append("com.mm.engine.framework.control.netEvent.remote.RemoteCallService remoteCallService = (com.mm.engine.framework.control.netEvent.remote.RemoteCallService)com.mm.engine.framework.tool.helper.BeanHelper.getServiceBean(com.mm.engine.framework.control.netEvent.remote.RemoteCallService.class);");
                            String invokeStr = "remoteCallService.remoteCallMainServerSyn("+serviceClass.getName()+".class,\""+ctMethod.getName()+"\","+paramStr+");";
                            if(ctMethod.getReturnType().getName().toLowerCase().equals("void")){
                                sb.append(invokeStr);
                            }else{
                                sb.append("Object object = "+invokeStr);
                                sb.append("return "+parseBaseTypeStrToObjectTypeStr(ctMethod.getReturnType().getName()));
                            }
                            sb.append("}");
//                            System.out.println(sb.toString());
                            CtMethod method = CtMethod.make(sb.toString(), cls);
                            cls.addMethod(method);
                        }
                    }
                    newServiceClass = cls.toClass();
                }


                if(requestMap.containsKey(serviceClass)){
                    newServiceClass=generateRequestHandlerClass(newServiceClass,serviceClass);
                    opcodeList=new ArrayList<>();
                    List<Method> methodList=requestMap.get(serviceClass);
                    for(Method method :methodList){
                        Request request = method.getAnnotation(Request.class);
                        opcodeList.add(request.opcode());
                    }
                }
                if(eventListenerMap.containsKey(serviceClass)){
                    newServiceClass=generateEventListenerHandlerClass(newServiceClass,serviceClass);
                    eventList=new ArrayList<>();
                    List<Method> methodList=eventListenerMap.get(serviceClass);
                    for(Method method :methodList){
                        EventListener request = method.getAnnotation(EventListener.class);
                        eventList.add(request.event());
                    }
                }
                if(netEventListenerMap.containsKey(serviceClass)){
                    newServiceClass=generateNetEventListenerHandlerClass(newServiceClass,serviceClass);
                    netEventList=new ArrayList<>();
                    List<Method> methodList = netEventListenerMap.get((serviceClass));
                    for (Method method :methodList) {
                        NetEventListener netEventListener = method.getAnnotation(NetEventListener.class);
                        netEventList.add(netEventListener.netEvent());
                    }
                }
                if(updatableMap.containsKey(serviceClass)){
                    newServiceClass=generateUpdatableHandlerClass(newServiceClass,serviceClass);
                }
                serviceClassMap.put(serviceClass,newServiceClass);
                serviceOriginClass.put(newServiceClass,serviceClass);
                // request
                if(opcodeList!=null){
                    for(short opcode : opcodeList){
                        requestHandlerClassMap.put(opcode,serviceClass);
                    }
                }
                // event
                if(eventList!=null){
                    // 一个event可能对应多个类
                    for(short event : eventList){
                        if(eventListenerHandlerClassMap.containsKey(event)){
                            Set<Class<?>> classes=eventListenerHandlerClassMap.get(event);
                            classes.add(serviceClass);
                        }else{
                            Set<Class<?>> classes=new HashSet<>();
                            classes.add(serviceClass);
                            eventListenerHandlerClassMap.put(event,classes);
                        }
                    }
                }
                // netEvent
                if(netEventList!=null){
                    // 一个netevent可能对应多个类
                    for(int netEvent : netEventList){
                        netEventListenerHandlerClassMap.put(netEvent,serviceClass);
                    }
                }
                // update
                if(updatableMap.containsKey(serviceClass)){
                    updatableClassMap.put(serviceClass,updatableMap.get(serviceClass));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("ServiceHelper init Exception");
        }

    }
    private static String praseBaseTypeStrToObjectTypeStr(String typeStr,String paramStr){
        if(typeStr.equals("byte")){
            return "new Byte("+paramStr+")";
        }else if(typeStr.equals("short")){
            return "new Short("+paramStr+")";
        }else if(typeStr.equals("long")){
            return "new Long("+paramStr+")";
        }else if(typeStr.equals("int")){
            return "new Integer("+paramStr+")";
        }else if(typeStr.equals("float")){
            return "new Float("+paramStr+")";
        }else if(typeStr.equals("double")){
            return "new Double("+paramStr+")";
        }else if(typeStr.equals("char")){
            return "new Character("+paramStr+")";
        }else if(typeStr.equals("boolean")){
            return "new Boolean("+paramStr+")";
        }
        return paramStr;
    }
    private static String parseBaseTypeStrToObjectTypeStr(String typeStr){
        if(typeStr.equals("byte")){
            return "((Byte)object).byteValue();";
        }else if(typeStr.equals("short")){
            return "((Short)object).shortValue();";//"Short";
        }else if(typeStr.equals("long")){
            return "((Long)object).longValue();";//"Long";
        }else if(typeStr.equals("int")){
            return "((Integer)object).intValue();";//"Integer";
        }else if(typeStr.equals("float")){
            return "((Float)object).floatValue();";//"Float";
        }else if(typeStr.equals("double")){
            return "((Double)object).doubleValue();";//"Double";
        }else if(typeStr.equals("char")){
            return "((Character)object).charValue();";//"Character";
        }else if(typeStr.equals("boolean")){
            return "((Boolean)object).booleanValue();";//"Boolean";
        }
        return "("+typeStr+")object";
    }

    //get set
    public static Map<Class,List<Method>> getMethodsByAnnotation(Class<? extends Annotation> cls){
        List<Class<?>> serviceClasses= ClassHelper.getClassListByAnnotation(Service.class);
        Map<Class,List<Method>> result = new HashMap<>();
        for(Class<?> serviceClass : serviceClasses) {
            Service service = serviceClass.getAnnotation(Service.class);
            Method[] methods = serviceClass.getMethods();
            for (Method method : methods) {
                Annotation annotation = method.getAnnotation(cls);
                if(annotation != null){
                    List<Method> list = result.get(serviceClass);
                    if(list == null){
                        list = new ArrayList<>();
                        result.put(serviceClass,list);
                    }
                    list.add(method);
                }
            }
        }
        return result;
    }
    public static TIntObjectHashMap<Class<?>> getRequestHandlerMap(){
        return requestHandlerClassMap;
    }
    public static TShortObjectHashMap<Set<Class<?>>> getEventListenerHandlerClassMap(){
        return eventListenerHandlerClassMap;
    }
    public static TIntObjectHashMap<Class<?>> getNetEventListenerHandlerClassMap(){
        return netEventListenerHandlerClassMap;
    }

    public static Map<Class<?>, List<Method>> getUpdatableClassMap() {
        return updatableClassMap;
    }

    public static Map<Class<?>,Class<?>> getServiceClassMap(){
        return serviceClassMap;
    }
    public static Class<?> getOriginServiceClass(Class<?> cls){
        return serviceOriginClass.get(cls);
    }
    public static Map<Integer,Map<Class<?>,Method>>  getInitMethodMap() {
        return initMethodMap;
    }

    public static Map<Class<?>, Method> getDestroyMethodMap() {
        return destroyMethodMap;
    }

    public static Map<String, Method> getGmMethod() {
        return gmMethod;
    }

    //
    private static void addMethodToMap(Map<Class<?>,List<Method>> map,Class<?> cls,Method method){
        if(map.containsKey(cls)){
            List<Method> list=map.get(cls);
            list.add(method);
        }else{
            List<Method> list=new ArrayList<>();
            list.add(method);
            map.put(cls,list);
        }
    }
    // 生成request的处理类
    private static Class generateRequestHandlerClass(Class clazz, Class<?> oriClass) throws Exception{
        Map<Short,String> opMethods = new TreeMap<Short,String>();
        Method[] methods = oriClass.getDeclaredMethods();
        for(Method method:methods){ //遍历所有方法，将其中标注了是包处理方法的方法名加入到opMethods中
            if(method.isAnnotationPresent(Request.class)){
                Request op = method.getAnnotation(Request.class);
                Class[] parameterTypes= method.getParameterTypes();
                //检查方法的合法性
                // 检查参数
                if(parameterTypes.length != 2){
                    throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
                }
                if(parameterTypes[0] != Object.class || parameterTypes[1] != Session.class){
                    throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
                }
                // 检查返回值
                if(method.getReturnType()!=RetPacket.class){
                    throw new IllegalStateException("Method "+method.getName()+" ReturnType Error");
                }
                opMethods.put(op.opcode(), method.getName());
            }
        }
        if(opMethods.size() > 0){
            ClassPool pool = ClassPool.getDefault();

            CtClass oldClass =  pool.get(clazz.getName());
//			log.info("oldClass: " + oldClass);
            CtClass ct = pool.makeClass(oldClass.getName()+"$Proxy", oldClass); //这里需要生成一个新类，并且继承自原来的类
            CtClass superCt = pool.get(RequestHandler.class.getName());  //需要实现RequestHandler接口
            ct.addInterface(superCt);
            //添加handler方法，在其中添上switch...case段
            StringBuilder sb = new StringBuilder("public com.mm.engine.framework.net.code.RetPacket handle(" +
                    "int opcode,Object clientData,com.mm.engine.framework.data.entity.session.Session session) throws Exception{");
            sb.append("com.mm.engine.framework.net.code.RetPacket rePacket=null;");
            sb.append("short opCode = opcode;");//$1.getOpcode();");
            sb.append("switch (opCode) {");
            Iterator<Map.Entry<Short,String>> ite = opMethods.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry<Short, String> entry = ite.next();
                sb.append("case ").append(entry.getKey()).append(":");
                sb.append("rePacket=").append(entry.getValue()).append("($2,$3);"); //注意，这里所有的方法都必须是protected或者是public的，否则此部生成会出错
                sb.append("break;");
                //opcodes.add(entry.getKey());
            }
            sb.append("}");
            sb.append("return rePacket;");
            sb.append("}");
            CtMethod method = CtMethod.make(sb.toString(), ct);
            ct.addMethod(method);

            return ct.toClass();
        }else{
            return clazz;
        }
    }
    // 生成event的处理类
    private static Class generateEventListenerHandlerClass(Class clazz , Class<?> oriClass) throws Exception{
        Map<Short,List<String>> opMethods = new TreeMap<Short,List<String>>();
        Method[] methods = oriClass.getDeclaredMethods();
//        Method[] methods = clazz.getMethods();
        for(Method method:methods){ //遍历所有方法，将其中标注了是包处理方法的方法名加入到opMethods中
            if(method.isAnnotationPresent(EventListener.class)){
                EventListener op = method.getAnnotation(EventListener.class);
                Class[] parameterTypes= method.getParameterTypes();
                //检查方法的合法性
                // 检查参数
                if(parameterTypes.length != 1){
                throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
            }
            if(parameterTypes[0] != EventData.class){
                throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
            }
            // 一个类中可能存在多个对该事件的监听
                if(opMethods.containsKey(op.event())){
                    opMethods.get(op.event()).add(method.getName());
                }else{
                    List<String> methodNames=new ArrayList<>();
                    methodNames.add(method.getName());
                    opMethods.put(op.event(), methodNames);
                }
            }
        }
        if(opMethods.size() > 0){
            ClassPool pool = ClassPool.getDefault();
            CtClass oldClass =  pool.get(clazz.getName());
            CtClass ct = pool.makeClass(oldClass.getName()+"$Proxy", oldClass); //这里需要生成一个新类，并且继承自原来的类
            CtClass superCt = pool.get(EventListenerHandler.class.getName());  //需要实现RequestHandler接口
            ct.addInterface(superCt);
            //添加handler方法，在其中添上switch...case段
            StringBuilder sb = new StringBuilder("public void handle(" +
                    "com.mm.engine.framework.control.event.EventData eventData) throws Exception{");
            sb.append("short event = $1.getEvent();");//$1.getOpcode();");
            sb.append("switch (event) {");
            Iterator<Map.Entry<Short,List<String>>> ite = opMethods.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry<Short, List<String>> entry = ite.next();
                sb.append("case ").append(entry.getKey()).append(":");
                for(String meName : entry.getValue()){
                    sb.append(meName).append("($1);"); //注意，这里所有的方法都必须是protected或者是public的，否则此部生成会出错
                }
                sb.append("break;");
                //opcodes.add(entry.getKey());
            }
            sb.append("}");
            sb.append("}");
            CtMethod method = CtMethod.make(sb.toString(), ct);
            ct.addMethod(method);
            return ct.toClass();
        }else{
            return clazz;
        }
    }
    private static Class generateNetEventListenerHandlerClass(Class clazz, Class<?> oriClass) throws Exception{
        Map<Integer,String> opMethods = new TreeMap<Integer,String>();
        Method[] methods = oriClass.getDeclaredMethods();

        for(Method method:methods){ //遍历所有方法，将其中标注了是包处理方法的方法名加入到opMethods中
            if(method.isAnnotationPresent(NetEventListener.class)){
                NetEventListener op = method.getAnnotation(NetEventListener.class);
                Class[] parameterTypes= method.getParameterTypes();
                //检查方法的合法性
                // 检查参数
                if(parameterTypes.length != 1){
                    throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
                }
                if(parameterTypes[0] != NetEventData.class){
                    throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
                }
                // 检查返回值
                if(method.getReturnType()!=NetEventData.class){
                    throw new IllegalStateException("Method "+method.getName()+" ReturnType Error");
                }
                opMethods.put(op.netEvent(), method.getName());
            }
        }
        if(opMethods.size() > 0){
            ClassPool pool = ClassPool.getDefault();
            CtClass oldClass =  pool.get(clazz.getName());
            CtClass ct = pool.makeClass(oldClass.getName()+"$Proxy", oldClass); //这里需要生成一个新类，并且继承自原来的类
            CtClass superCt = pool.get(NetEventListenerHandler.class.getName());  //需要实现RequestHandler接口
            ct.addInterface(superCt);
            //添加handler方法，在其中添上switch...case段
            StringBuilder sb = new StringBuilder("public com.mm.engine.framework.control.netEvent.NetEventData handle(" +
                    "com.mm.engine.framework.control.netEvent.NetEventData netEventData) throws Exception{");
            sb.append("com.mm.engine.framework.control.netEvent.NetEventData rePacket=null;");
            sb.append("int event = $1.getNetEvent();");//$1.getOpcode();");
            sb.append("switch (event) {");
            Iterator<Map.Entry<Integer,String>> ite = opMethods.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry<Integer, String> entry = ite.next();
                sb.append("case ").append(entry.getKey()).append(":");
                sb.append("rePacket=").append(entry.getValue()).append("($1);"); //注意，这里所有的方法都必须是protected或者是public的，否则此部生成会出错
                sb.append("break;");
            }
            sb.append("}");
            sb.append("return rePacket;");
            sb.append("}");
            CtMethod method = CtMethod.make(sb.toString(), ct);
            ct.addMethod(method);
            return ct.toClass();
        }else{
            return clazz;
        }
    }
    // 校验参数
    private static Class generateUpdatableHandlerClass(Class clazz, Class<?> oriClass) throws Exception{
        Method[] methods = oriClass.getDeclaredMethods();
        for(Method method:methods){ //遍历所有方法，将其中标注了是包处理方法的方法名加入到opMethods中
            if(method.isAnnotationPresent(Updatable.class)){
                Updatable op = method.getAnnotation(Updatable.class);
                Class[] parameterTypes= method.getParameterTypes();
                //检查方法的合法性
                // 检查参数
                if(parameterTypes.length != 1){
                    throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
                }
                if(parameterTypes[0] != Integer.class && parameterTypes[0] != int.class ){
                    throw new IllegalStateException("Method "+method.getName()+" Parameter Error");
                }
                // 检查注解
                if(op.isAsynchronous() && op.cycle()==-1 && op.cronExpression().length()==0){
                    throw new IllegalStateException("Method "+method.getName()+" annotation Error,cycle can't be default while update is asynchronous");
                }
            }
        }
        return clazz;
    }
}
