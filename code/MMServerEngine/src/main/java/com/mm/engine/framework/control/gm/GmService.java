package com.mm.engine.framework.control.gm;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.BeanHelper;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by a on 2016/9/28.
 * gm
 */
@Service(init = "init")
public class GmService {
    private Map<String,Method> gmMethods;
    public void init(){
        // 取出gm的所有方法的参数
        gmMethods = ServiceHelper.getGmMethod();
    }

    /**
     * 处理gm，注意这里传过来的参数全是引用数据类型，不是基本类型
     * @param id
     * @param params
     */
    public Object handle(String id,Object... params){
        Method method = gmMethods.get(id);
        if(method == null){
            throw new MMException("gm is not exist , id = "+id);
        }
        System.out.println(method.getDeclaringClass());
        Object service = BeanHelper.getServiceBean(method.getDeclaringClass());
        if(service == null){
            throw new MMException("gm service is not exist , id = "+id+",class = "+method.getDeclaringClass());
        }
        // 校验参数
        Class[] clses = method.getParameterTypes();
        if(clses.length>params.length){
            throw new MMException("gm params error!need "+clses.length+" params"+",but get "+params.length);
        }
        int i=0;
        for(Class cls : clses){
            if(!castPrimitiveClass(cls).isAssignableFrom(params[i].getClass())){
                throw new MMException("gm params error, id = "+id);
            }
            i++;
        }
        // 调用
        try {
            Object result = method.invoke(service, params);
            return result;
        }catch (Throwable e){
            throw new MMException(e);
        }
    }

    /**
     * 如果是原始类型，转换成封装类型
     * @param cls
     */
    private Class castPrimitiveClass(Class cls){
        if(cls.isPrimitive()){
            if(cls == int.class) cls = Integer.class;
            else if(cls == long.class) cls = Long.class;
            else if(cls == float.class) cls = Float.class;
            else if(cls == double.class) cls = Double.class;
            else if(cls == char.class) cls = Character.class;
            else if(cls == byte.class) cls = Byte.class;
            else if(cls == boolean.class) cls = Boolean.class;
            else if(cls == short.class) cls = Short.class;
        }
        return cls;
    }
}
