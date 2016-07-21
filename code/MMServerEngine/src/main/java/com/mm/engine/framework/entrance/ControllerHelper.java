package com.mm.engine.framework.entrance;

import com.mm.engine.framework.entrance.code.protocol.ProtocolDecode;
import com.mm.engine.framework.entrance.code.protocol.ProtocolEncode;
import com.mm.engine.framework.tool.helper.ClassHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/19.
 */
public final class ControllerHelper {
    private static final Logger log = LoggerFactory.getLogger(ControllerHelper.class);
    private static Map<String,EntranceControllerClass> entranceControllerClassMap =new HashMap<>();
    static {
        // 获取所有request的入口
        List<Class<?>> classList= ClassHelper.getClassListBySuper(EntranceController.class);
        for(Class<?> cls : classList){
            if(cls.isAnnotationPresent(Controller.class)){
                EntranceControllerClass entranceClass=new EntranceControllerClass();
                entranceClass.entranceClass=(Class<? extends EntranceController>)cls;
                Controller controller =cls.getAnnotation(Controller.class);
                entranceClass.protocolDecodeClass = controller.protocolDecode();
                entranceClass.protocolEncodeClass= controller.protocolEncode();
                if(StringUtils.isEmpty(controller.name())){
                    log.error("controller name is Invalid by EntranceClass "+cls.getName());
                    throw new IllegalStateException("controller name is Invalid by EntranceClass "+cls.getName());
                }
                entranceControllerClassMap.put(controller.name(),entranceClass);
            }
        }
    }
    public static Map<String,EntranceControllerClass> getEntranceControllerClassMap(){
        return entranceControllerClassMap;
    }

    public static final class EntranceControllerClass {
        private Class<? extends EntranceController> entranceClass;
        private Class<? extends ProtocolEncode> protocolEncodeClass;
        private Class<? extends ProtocolDecode> protocolDecodeClass;

        public Class<? extends EntranceController> getEntranceClass(){
            return entranceClass;
        }
        public Class<? extends ProtocolEncode> getProtocolEncodeClass(){
            return protocolEncodeClass;
        }
        public Class<? extends ProtocolDecode> getProtocolDecodeClass(){
            return protocolDecodeClass;
        }
    }
}
