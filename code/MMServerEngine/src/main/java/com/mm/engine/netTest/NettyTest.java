package com.mm.engine.netTest;

        import com.mm.engine.framework.net.client.socket.NettyClient;
        import com.mm.engine.framework.server.configure.EngineConfigure;
        import com.mm.engine.framework.server.Server;
        import com.protocol.AccountOpcode;
        import com.protocol.AccountPB;

/**
 * Created by apple on 16-8-27.
 */
public class NettyTest {

    public static void main(String[] args) throws Throwable{
        NettyClient nettyClient = new NettyClient("192.168.1.240",8003);
        nettyClient.start();
        AccountPB.CSLoginNode.Builder builder = AccountPB.CSLoginNode.newBuilder();
        builder.setAccountId("accountId_1241");
        builder.setSessionId("Session_f20eb697-d6d9-45e8-9626-51b515d51b35");

        byte[] reData = nettyClient.send(AccountOpcode.CSLoginNode,builder.build().toByteArray());

        AccountPB.SCLoginNode scLoginNode = AccountPB.SCLoginNode.parseFrom(reData);
        System.out.println(scLoginNode);
    }
}
