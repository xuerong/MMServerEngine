package com.mm.engine.framework.control.scene;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.IdService;
import com.mm.engine.framework.tool.helper.BeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2016/9/14.
 * 场景服务
 *
 */
@Service(init = "init")
public class SceneService {
    private static final Logger log = LoggerFactory.getLogger(SceneService.class);
    private ConcurrentHashMap<Integer,Scene> sceneMap;

    private IdService idService;

    public void init(){
        idService = BeanHelper.getServiceBean(IdService.class);
        sceneMap = new ConcurrentHashMap<>();
    }

    public Object handle(int sceneId,int opcode,Object data){
        Scene scene = sceneMap.get(sceneId);
        if(scene == null){
            throw new MMException("scene is not exist");
        }
        Object retData = scene.handle(opcode,data);
        return retData;
    }

    public <T> T createScene(Class<T> cls){
        if(!Scene.class.isAssignableFrom(cls)){
            throw new MMException("create scene error,"+cls.getName()+"is not Scene");
        }
        Scene scene = (Scene)BeanHelper.newAopInstance(cls);
        scene.setId(idService.acquire(Scene.class));
        scene.init();
        return (T)scene;
    }

    public Scene getScene(int id){
        return sceneMap.get(id);
    }

    public Scene removeScene(int id){
        Scene scene = sceneMap.remove(id);
        if(scene == null){
            log.warn("scene is not exist while removeScene,sceneId = "+id);
        }else {
            scene.destroy();
            idService.release(Scene.class,scene.getId());
        }
        return scene;
    }
}
