package com.mm.engine.framework.data.entity.session;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.annotation.Updatable;
import com.mm.engine.framework.data.cache.CacheCenter;
import com.mm.engine.framework.entrance.NetType;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import gnu.trove.procedure.TLongLongProcedure;
import gnu.trove.procedure.TLongProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/11/16.
 * SessionManager：session的管理器，用来：
 * 获取session
 * 创建session
 * 定期更新session
 * 删除session
 * 保存session
 */
@Service
public final class SessionManager {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private static final ConcurrentHashMap<String,Long> updateTime;
    private static final int cycle;
    /**
     * session的存活时间，后面考虑一下是否将其改为由SessionClient决定
     *
     * 或者：session有自身的存活时间，而SessionClient自身可以有自己的存活时间，通过是否过期可以让自身的session销毁！！
     * **/
    private static final int survivalTime;
    /**
     * 最多移除数量
     * **/
    private static final int maxOnceRemoveSessionCount = 300;

    private static final CacheCenter cacheCenter;
    static {
        cycle= Server.getEngineConfigure().getSessionCycle();
        survivalTime=Server.getEngineConfigure().getSessionSurvivalTime();
        cacheCenter= BeanHelper.getFrameBean(CacheCenter.class);
        updateTime=new ConcurrentHashMap<>();
    }

    public static Session get(String sessionId){
        Session session = (Session) cacheCenter.get(sessionId);
        if(session!=null) {
            // 更新session时间
            session.setLastUpdateTime(new Date());
            updateTime.put(sessionId, session.getLastUpdateTime().getTime());
        }
        return session;
    }

    // 创建http的session
    public static Session create(HttpServletRequest request){
        Session session=new Session(NetType.Http,request.getContextPath(), createSessionIdPrefix(), Util.getIp(request),new Date());
        updateTime.put(session.getSessionId(),session.getLastUpdateTime().getTime());
        Object older = cacheCenter.putIfAbsent(session.getSessionId(),session);
        if(older != null){
            session = (Session)older;
        }
        return session;
    }
    public static Session create(NetType netType, String url,String ip){
        Session session=new Session(netType,url, createSessionIdPrefix(), ip,new Date());
        updateTime.put(session.getSessionId(),session.getLastUpdateTime().getTime());
        Object older = cacheCenter.putIfAbsent(session.getSessionId(),session);
        if(older != null){
            session = (Session)older;
        }
        return session;
    }

    // 这个地方如何赋值？可以取final值，所以可以从配置文件中取
    @Updatable(isAsynchronous = true,cycle = 200000)
    public void update(int interval){
        final long currentTime=System.currentTimeMillis();
        final List<String> expiredIds = new ArrayList<>();
        // 先找出过期的session，然后更新
        int expiredIdNum = 0;
        for (Map.Entry<String,Long> entry :updateTime.entrySet()) {
            if(currentTime - entry.getValue() >=survivalTime){
                expiredIds.add(entry.getKey());
            }
            if(expiredIdNum++ > maxOnceRemoveSessionCount){
                break;
            }
        }
        for (String key :expiredIds) {
            removeSession((Session) cacheCenter.get(key));
        }
    }

    private void removeSession(Session session){
        session.getSessionClient().destroySession();
        updateTime.remove(session.getSessionId());
        // 是否从当前缓存中剔除：1不踢，2踢本地缓存，3踢本地和远程
        cacheCenter.remove(session.getSessionId());
    }

    private static String createSessionIdPrefix(){
        return System.currentTimeMillis()+"";
    }
}
