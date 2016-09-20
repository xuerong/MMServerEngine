package com.mm.engine.netTest;

import com.mm.engine.framework.net.client.http.HttpClient;
import com.mm.engine.framework.net.client.http.HttpPBPacket;
import com.protocol.AccountOpcode;
import com.protocol.AccountPB;
import com.protocol.OpCode;

/**
 * Created by a on 2016/8/9.
 */
public class HttpTest {
    public static void main(String[] args){
//        PBMessage.CSLogin.Builder builder = PBMessage.CSLogin.newBuilder();
//        builder.setMid("123");
//        builder.setVersion("123");
//        builder.setChannelId(10);
//        HttpPBPacket httpPbPacket = new HttpPBPacket(OpCode.CSLogin,builder);
//        HttpPBPacket retPacket = HttpClient.getInstance().send(httpPbPacket,null);
//        System.out.println(retPacket.getResult()+","+retPacket.getOpcode()+" success,"+retPacket.getSession());

        AccountPB.CSLoginMain.Builder builder = AccountPB.CSLoginMain.newBuilder();
        builder.setAccountId("accountId_123");
        HttpPBPacket httpPbPacket = new HttpPBPacket(AccountOpcode.CSLoginMain,builder);
        HttpPBPacket retPacket = HttpClient.getInstance().send(httpPbPacket,null);
        System.out.println(retPacket.getResult()+","+retPacket.getOpcode()+" success,"+retPacket.getSession());
    }
}
