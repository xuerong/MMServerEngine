package com.mm.engine.netTest;

        import com.mm.engine.framework.server.configure.EngineConfigure;
        import com.mm.engine.framework.server.Server;

/**
 * Created by apple on 16-8-27.
 */
public class NettyTest {

    public static void main(String[] args) throws Throwable{
        Server.init(new EngineConfigure(null,8003));
        Server.start();
//        NettyServerClient nettyServerClient = new NettyServerClient(ServerType.MAIN_SERVER,"localhost",8000);
//        nettyServerClient.start();
    }
}
