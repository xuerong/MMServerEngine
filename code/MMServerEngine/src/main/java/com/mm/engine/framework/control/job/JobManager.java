package com.mm.engine.framework.control.job;

import com.google.code.yanf4j.util.ConcurrentHashSet;
import com.mm.engine.framework.control.annotation.NetEventListener;
import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.NetEventManager;
import com.mm.engine.framework.entrance.client.ServerClient;
import com.mm.engine.framework.exception.MMException;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by apple on 16-9-4.
 * job分为两种:
 * 一种是重复执行:用cronExpression表示
 * 一种是指执行一次:用时间段表示
 *
 * job实际运行在各个服务器上,mainServer负责调度job的id,防止重复
 *
 */
public class JobManager {
    // 执行job的调度器,这个线程数不用处理器的个数,因为有些job会有数据库操作
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(100);
    private static ConcurrentHashMap<String,JobExecutor> jobExecutorMap = new ConcurrentHashMap<>();
    // 这里面存储的是所有的job的key,用来确保不能有重复的key,这个只在mainServer上面有效
    private static ConcurrentHashMap<String,String> jobIds = new ConcurrentHashMap<>();

    public static void startJob(Job job){
        JobExecutor jobExecutor = createJobExecutor(job);
        long delay = job.getStartTime().getTime()-System.currentTimeMillis();
        RunnableScheduledFuture<?> future = (RunnableScheduledFuture)executor.schedule(jobExecutor,
                delay, TimeUnit.MILLISECONDS); // 这里delay<0是处理的了
        // TODO 如果这里立刻就执行了,下面添加到jobExecutorMap显然就不应该了,暂时使用CountDownLatch解决,
        jobExecutor.future = future;
        JobExecutor oldJ = jobExecutorMap.putIfAbsent(job.getId(),jobExecutor);
        if(oldJ != null){ // 已经存在了
            executor.remove(future);
            throw new MMException("job has exist! id = "+job.getId());
        }
        // 向远端注册
        JobNetEventData data = new JobNetEventData();
        data.id = job.getId();
        data.type = 1;
        data.serverAdd = Util.getHostAddress()+":"+ Server.getEngineConfigure().getNetEventPort();
        NetEventData result = NetEventManager.fireMainServerNetEventSyn(
                new NetEventData(SysConstantDefine.checkJobId,data));
        if(!(boolean)(result.getParam())){ // 添加失败
            // 在本地删除
            executor.remove(future);
            jobExecutorMap.remove(job.getId());
            throw new MMException("job has exist! id = "+job.getId());
        }
        jobExecutor.latch.countDown();
    }
    @NetEventListener(netEvent = SysConstantDefine.checkJobId)
    public NetEventData checkJobId(NetEventData eventData){
        JobNetEventData data = (JobNetEventData)eventData.getParam();
        if(data.type == 1){ // 1 添加,2 移除
            String oldAdd = jobIds.putIfAbsent(data.id,data.serverAdd);
            eventData.setParam(oldAdd == null);
            return eventData;
        }else if(data.type == 2){ //移除
            String oldAdd = jobIds.remove(data.id);
            if(oldAdd == null ){
                return eventData;
            }
            if(!data.serverAdd.equals(oldAdd)){// 需要删除实际的job
                NetEventManager.fireServerNetEvent(oldAdd,new NetEventData(SysConstantDefine.removeJobOnServer,data.id));
            }
        }
        throw new MMException("netEvent error : "+eventData.getNetEvent());
    }
    @NetEventListener(netEvent =  SysConstantDefine.removeJobOnServer)
    public NetEventData removeJobOnServer(NetEventData data){
        String id = (String)data.getParam();
        JobExecutor jobExecutor = jobExecutorMap.remove(id);
        if(jobExecutor != null){
            executor.remove(jobExecutor.future);
        }
        data.setParam(Boolean.TRUE);
        return data;
    }
    public static void main(String[] args) throws Throwable{
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("do it");
            }
        };
         executor.getQueue();
        RunnableScheduledFuture<?> future = (RunnableScheduledFuture)executor.schedule(runnable,2,TimeUnit.SECONDS);
//        future.cancel(false);
        boolean s = executor.remove(future);
        System.out.println(s);
        future = (RunnableScheduledFuture)executor.schedule(runnable,2,TimeUnit.SECONDS);
        Thread.sleep(3000);
        s = executor.remove(future);
        System.out.println(s);

//        boolean isFather = JobManager.class.isAssignableFrom(JobManager.class);
//        if(isFather){
//            System.out.println("D是B的父类");
//        }else{
//            System.out.println("D不是B的父类");
//        }
//        try {
//            Date now = new Date();
//            CronExpression cronExpression = new CronExpression("1/10 * * * * ? *");
//            Date time = cronExpression.getNextValidTimeAfter(now);
//            while(true){
//                Thread.sleep(time.getTime()-now.getTime());
//                System.out.println(time.getTime()/1000);
//                now = new Date();
//                time = cronExpression.getNextValidTimeAfter(time);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }

    private static JobExecutor createJobExecutor(Job job){
        try {
            JobExecutor jobExecutor = new JobExecutor();
            jobExecutor.id = job.getId();
            if(jobExecutor.id == null || jobExecutor.id.length() == 0){
                throw new MMException("job set error: id is null");
            }
            if(job.getCronExpression() != null){
                jobExecutor.cronExpression = new CronExpression(job.getCronExpression());
            }
            if(job.getStartTime() != null) {
                jobExecutor.startTime = job.getStartTime();
            }else if(jobExecutor.cronExpression != null){
                jobExecutor.startTime = jobExecutor.cronExpression.getNextValidTimeAfter(new Date());
            }else{
                throw new MMException("job set error: can't create startTime");
            }
            jobExecutor.db = job.isDb();
            Object bean = BeanHelper.getServiceBean(job.getServiceClass());
            if(bean == null){
                throw new MMException("job set error: service is not exist:"+job.getServiceClass().getName());
            }
            Method method = null;

            Method[] methods = job.getServiceClass().getMethods();
            for(int i = 0;i<methods.length;i++){
                if(methods[i].getName().equals(job.getMethod())){
                    Class<?>[] classes = methods[i].getParameterTypes(); // 它是可能是父类
                    if((job.getPara() == null || job.getPara().length == 0)
                            &&(classes == null || classes.length == 0)){ // 都没有
                        method = methods[i];
                        break;
                    }
                    if((job.getPara() == null || job.getPara().length == 0)
                            ||(classes == null || classes.length == 0)){ // 其中一个没有
                        continue;
                    }
                    if(classes.length != job.getPara().length){// TODO 这个地方如何考虑Object...参数
                        continue;
                    }
                    // 比较参数
                    Class<?>[] paraClasses = new Class[job.getPara().length];
                    for(int p=0;p<paraClasses.length;p++){
                        paraClasses[p] = job.getPara()[p].getClass();
                    }
                    boolean success = true;
                    for(int k = 0;k<classes.length;k++){
                        if(!classes[k].isAssignableFrom(paraClasses[k])){
                            success = false;
                            break;
                        }
                    }
                    if(success) {
                        method = methods[i];
                        break;
                    }
                }
            }
            if(method == null){
                throw new MMException("can't find method with such para: "+job.getMethod());
            }
            jobExecutor.method = method;
            jobExecutor.para = job.getPara();
            jobExecutor.object = bean;

            return jobExecutor;
        }catch (ParseException e){
            throw new MMException(e);
        }
    }

    public static class JobNetEventData implements Serializable{
        private int type; // 1 添加,2 移除

        private String id;
// 这个不要了,因为在逻辑中如果需要这样做,一定知道原来的job,就可以通过删除解决,否则,不能这样做
//        private boolean replaceIfExist;

        private String serverAdd; // server的地址
    }

    public static class JobExecutor implements Runnable{
        private String id;
        //
        private Date startTime; // 第一次执行时间,
        private CronExpression cronExpression; // 执行时间表达式,如果存在,会多次执行

        private boolean db; // 是否持久化

        private Method method;
        private Object object;
        private Object[] para;

        private CountDownLatch latch = new CountDownLatch(1);// 这个是防止同步的时候出现问题

        private RunnableScheduledFuture<?> future;

        @Override
        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Date now = new Date();
            try {
                method.invoke(object,para);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if(cronExpression != null){
                Date time = cronExpression.getNextValidTimeAfter(now);
                long delay = time.getTime()-now.getTime();
                RunnableScheduledFuture<?> future = (RunnableScheduledFuture)executor.schedule(this,
                        delay, TimeUnit.MILLISECONDS); // 这里delay<0是处理的了
                this.future =future;
            }else{
                JobNetEventData data = new JobNetEventData();
                data.id = id;
                data.type = 2; // 移除
                data.serverAdd = Util.getHostAddress()+":"+ Server.getEngineConfigure().getNetEventPort();
                // 清楚同步id
                NetEventManager.fireMainServerNetEvent(
                        new NetEventData(SysConstantDefine.checkJobId,data));
                // 清除自身的保存
                jobExecutorMap.remove(id);
            }
        }
    }
}
