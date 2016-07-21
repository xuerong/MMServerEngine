package com.mm.engine.framework.data.entity.session;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.annotation.Updatable;
import com.mm.engine.framework.data.cache.CacheCenter;
import com.mm.engine.framework.entrance.NetType;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import gnu.trove.list.TLongList;
import gnu.trove.list.linked.TLongLinkedList;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.procedure.TLongLongProcedure;
import gnu.trove.procedure.TLongProcedure;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

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

    private static final TLongLongMap updateTime;
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
        updateTime=new TLongLongHashMap();
    }

    public static Session get(String sessionId){
        String idStr = sessionId.substring(sessionId.lastIndexOf("_")+1);
        if(StringUtils.isEmpty(idStr) || !StringUtils.isNumeric(idStr)){
            log.warn("sessionId is error , sessionId:"+sessionId);
            throw new RuntimeException("sessionId is error , sessionId:"+sessionId);
        }
        long id = Long.parseLong(idStr);

        Session session = (Session) cacheCenter.get(id,Session.class);
        if(session!=null) {
            // 更新session时间
            session.setLastUpdateTime(new Date());
            updateTime.put(id, session.getLastUpdateTime().getTime());
        }
        return session;
    }

    // 创建http的session
    public static Session create(HttpServletRequest request){
        Session session=new Session(NetType.Http,request.getContextPath(), createSessionIdPrefix(), Util.getIp(request),new Date());
        updateTime.put(session.getCacheId(),session.getLastUpdateTime().getTime());
        cacheCenter.putNew(session);
        return session;
    }
    public static Session create(NetType netType, String url,String ip){
        Session session=new Session(netType,url, createSessionIdPrefix(), ip,new Date());
        updateTime.put(session.getCacheId(),session.getLastUpdateTime().getTime());
        cacheCenter.putNew(session);
        return session;
    }

    // 这个地方如何赋值？
    @Updatable(isAsynchronous = true,cycle = 200000)
    public void update(int interval){
        long currentTime=System.currentTimeMillis();
        TLongList expiredIds = new TLongLinkedList();
        // 先找出过期的session，然后更新
        updateTime.forEachEntry(new TLongLongProcedure() {
            int expiredIdNum = 0;
            @Override
            public boolean execute(long id, long lastUpdateTime) {
                if(currentTime - lastUpdateTime >=survivalTime){
                    expiredIds.add(id);
                }
                if(expiredIdNum++ > maxOnceRemoveSessionCount){
                    return false;
                }
                return true;
            }
        });
        synchronized (updateTime){
            expiredIds.forEach(new TLongProcedure() {
                @Override
                public boolean execute(long id) {
                    removeSession((Session) cacheCenter.get(id,Session.class));
                    return true;
                }
            });
        }
    }

    private void removeSession(Session session){
        session.getSessionClient().destroySession();
        updateTime.remove(session.getCacheId());
        // 是否从当前缓存中剔除：1不踢，2踢本地缓存，3踢本地和远程
        cacheCenter.remove(session);
    }

    private static String createSessionIdPrefix(){
        return System.currentTimeMillis()+"";
    }
}
