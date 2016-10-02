package com.mm.engine.framework.data.sysPara;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.gm.Gm;
import com.mm.engine.framework.control.netEvent.RemoteCallService;
import com.mm.engine.framework.data.tx.LockerService;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.sys.SysPara;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by a on 2016/9/27.
 * 获取系统参数
 * 修改系统参数
 * 重置系统参数
 *
 *
 * 策划配数、程序修改、程序添加删除
 *
 * 系统变量其实是一个通用变量容器，只不过策划可以配置一些数据，不过这些数据是可以被修改和冲掉的
 *
 * mainServer接收gm指令
 */
@Service(init = "init",initPriority = 4)
public class SysParaService {

    private Map<String,String> paraMap = new HashMap<>();
    private Map<String,String> storageParaMap = null;
    private ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors()+1, 30,
            20, TimeUnit.SECONDS, new LinkedBlockingQueue(), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new MMException("rejectedExecution");
        }
    });

    private SysParaStorage sysParaStorage;
    private LockerService lockerService;
    private RemoteCallService remoteCallService;
    public void init(){
        /**
         * 加载策划配数，
         * 加载store数据，如果与策划配数冲突，替换掉策划配数
         * 启动一个线程池用于广播修改的系统变量
         */
        sysParaStorage = BeanHelper.getFrameBean(SysParaStorage.class);

        for(Map.Entry<String,String> entry : SysPara.paras.entrySet()){
            paraMap.put(entry.getKey(),entry.getValue());
        }
        storageParaMap = sysParaStorage.getAllSysPara();
        if(storageParaMap != null) {
            for (Map.Entry<String, String> entry : storageParaMap.entrySet()) {
                paraMap.put(entry.getKey(), entry.getValue()); // 存在则替换
            }
        }
    }

    /**
     * 获取系统参数
     * @param key
     * @return
     */
    public String get(String key){
        return paraMap.get(key);
    }

    /**
     * 修改参数，返回put之前的参数，并异步更新其它服务器的对应参数
     * @param key
     * @param value
     * @return
     */
    public String put(final String key,final String value){
        // 修改本地
        final String old = _doChange(key,value);
        // 广播并修改存储
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if(!lockerService.lockKeys(key)){
                    throw new MMException("lock fail , key = "+key);
                }
                try {
                    // 广播
                    remoteCallService.broadcastRemoteCallSyn(SysParaService.class,"_doChange",key,value);
                    // 修改存储
                    if (old == null) {
                        sysParaStorage.insertSysPara(key, value);
                    } else {
                        sysParaStorage.update(key, value);
                    }
                }finally {
                    lockerService.unlockKeys(key);
                }
            }
        });
        return old;
    }

    /**
     * 重置系统参数：
     * 如果该参数有策划配数，则重置为策划配数
     * 否则，删除该参数
     * @param key
     * @return 之前的数
     */
    public String reset(final String key){
        final String old = _doReset(key);
        // 广播并修改存储
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if(!lockerService.lockKeys(key)){
                    throw new MMException("lock fail , key = "+key);
                }
                try {
                    // 广播
                    remoteCallService.broadcastRemoteCallSyn(SysParaService.class,"_doReset",key);
                    // 删除存储
                    sysParaStorage.delete(old);
                }finally {
                    lockerService.unlockKeys(key);
                }
            }
        });
        return old;
    }
    @Gm(id="SysPara_gmUpdate")
    public String gmUpdate(String key,String value){
        String old = put(key,value);
        return "success , Previous is "+old;
    }


    public String _doReset(final String key){
        String value = SysPara.paras.get(key);
        String old;
        if(value != null){
            old = paraMap.put(key, value);
        }else{
            old = paraMap.remove(key);
        }
        storageParaMap.remove(key);
        return old;
    }
    public String _doChange(final String key,final String value){
        String old = paraMap.put(key, value);
        storageParaMap.put(key, value);
        return old;
    }


}
