package com.mm.engine.netTest;

import com.mm.engine.framework.net.client.http.HttpClient;
import com.mm.engine.framework.net.client.http.HttpPBPacket;
import com.mm.engine.framework.net.client.socket.NettyClient;
import com.protocol.AccountOpcode;
import com.protocol.AccountPB;
import com.protocol.Test;
import com.protocol.TestOpcode;

/**
 * Created by a on 2016/11/3.
 */
public class AccountTest {
    public static void main(String[] args) throws Throwable{
        // 账号
        String accountId = "accountId_1241";
        // 登录mainServer 获取sessionId和nodeServer地址
        AccountPB.CSLoginMain.Builder loginMainBuilder = AccountPB.CSLoginMain.newBuilder();
        loginMainBuilder.setAccountId(accountId);
        HttpPBPacket httpPbPacket = new HttpPBPacket(AccountOpcode.CSLoginMain,loginMainBuilder);
        HttpPBPacket retPacket = HttpClient.getInstance().send(httpPbPacket,null);
        AccountPB.SCLoginMain scLoginMain = AccountPB.SCLoginMain.parseFrom(retPacket.getData());

        System.out.println(retPacket.getResult()+","+retPacket.getOpcode()+" success,host:"+
                scLoginMain.getHost()+",port:"+scLoginMain.getPort()+",session:"+scLoginMain.getSessionId());
        // 连接nodeServer
        NettyClient nettyClient = new NettyClient("192.168.1.240",8003);
        nettyClient.start();
        // 登录nodeServer
        AccountPB.CSLoginNode.Builder loginNodeBuilder = AccountPB.CSLoginNode.newBuilder();
        loginNodeBuilder.setAccountId(accountId);
        loginNodeBuilder.setSessionId(scLoginMain.getSessionId());
        byte[] reData = nettyClient.send(AccountOpcode.CSLoginNode,loginNodeBuilder.build().toByteArray());
        AccountPB.SCLoginNode scLoginNode = AccountPB.SCLoginNode.parseFrom(reData);
        System.out.println("scLoginNode"+scLoginNode);
        // 登出mainServer，同时也登出了nodeServer
        AccountPB.CSLogoutMain.Builder logoutMainBuilder = AccountPB.CSLogoutMain.newBuilder();
        logoutMainBuilder.setAccountId(accountId);
        httpPbPacket = new HttpPBPacket(AccountOpcode.CSLogoutMain,logoutMainBuilder);
        retPacket = HttpClient.getInstance().send(httpPbPacket,null);
        AccountPB.SCLogoutMain scLogoutMain = AccountPB.SCLogoutMain.parseFrom(retPacket.getData());
        System.out.println("scLogoutMain"+scLogoutMain);
    }
}
