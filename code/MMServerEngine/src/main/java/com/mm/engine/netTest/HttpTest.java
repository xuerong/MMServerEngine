package com.mm.engine.netTest;

import com.mm.engine.framework.net.client.http.HttpClient;
import com.mm.engine.framework.net.client.http.PBPacket;
import com.protocol.OpCode;
import com.protocol.PBMessage;

/**
 * Created by a on 2016/8/9.
 */
public class HttpTest {
    public static void main(String[] args){
        PBMessage.CSLogin.Builder builder = PBMessage.CSLogin.newBuilder();
        builder.setMid("123");
        builder.setVersion("123");
        builder.setChannelId(10);
        PBPacket pbPacket = new PBPacket(OpCode.CSLogin,builder);
        PBPacket retPacket = HttpClient.getInstance().send(pbPacket,null);
        System.out.println(retPacket.getResult()+","+retPacket.getOpcode()+" success,"+retPacket.getSession());
    }
}
