package com.mm.engine.netTest;

        import com.mm.engine.framework.entrance.client.socket.NettyServerClient;
        import com.mm.engine.framework.server.EngineConfigure;
        import com.mm.engine.framework.server.Server;
        import com.mm.engine.framework.server.ServerType;

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
