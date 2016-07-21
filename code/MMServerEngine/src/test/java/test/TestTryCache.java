package test;

import java.util.concurrent.*;

/**
 * Created by Administrator on 2015/11/23.
 */
public class TestTryCache {
    public static void main(String[] args){
        ScheduledExecutorService asyncExecutor = new ScheduledThreadPoolExecutor(6, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

            }
        });
        asyncExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("132132132");
            }
        },100,100, TimeUnit.MILLISECONDS);

            long t1=System.nanoTime();
            int count=0;
            for (int i = 0; i < 10000000; i++) {
                try {
                    String result = aaa();
                    int a=Integer.parseInt(result);
                    long nono=System.nanoTime();
                    if(nono==(long)10000 && a==1000){
                        System.out.println(a + nono);
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }finally {
                    count++;
                }

            }

            long t2=System.nanoTime();
            System.out.println((t2-t1)/1000000+","+count);
        }
    public static String aaa(){
        int a=100;
        int b=200;
        return ""+(a+b*b+(int)Math.sin(b-a));
    }
}
