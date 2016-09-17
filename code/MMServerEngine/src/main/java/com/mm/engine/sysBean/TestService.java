package com.mm.engine.sysBean;

import com.mm.engine.framework.control.annotation.*;
import com.mm.engine.framework.control.event.EventService;
import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.RemoteCallService;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.RetPacketImpl;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.server.IdService;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import com.protocol.OpCode;
import com.protocol.PBMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/19.
 */
@Service(init = "init")
public class TestService {
    private EventService eventService;
    private IdService idService;
    public void init(){
//        System.out.println("TestService init");
        eventService = BeanHelper.getServiceBean(EventService.class);
        idService = BeanHelper.getServiceBean(IdService.class);
//        RemoteCallService remoteCallService = BeanHelper.getServiceBean(RemoteCallService.class);
//        remoteCallService.aaa();
    }
    @Request(opcode = 20002)
    public RetPacket handlerLogin(Object clientData, Session session){
        PBMessage.SCLoginRet.Builder builder= PBMessage.SCLoginRet.newBuilder();
//        session.setSessionClient(new Play);
        builder.setHasNewVersion(1);
        builder.setNewVersionUrl("123456");
        System.out.println("---------------------------------------------------------------------------------------");
        return new RetPacketImpl(OpCode.SCLoginRet,builder);
    }
    @Request(opcode = OpCode.CSPayRequest)
    public RetPacket handlerPay(Object clientData,Session session) throws Exception{
        PBMessage.CSPayRequest msg= PBMessage.CSPayRequest.parseFrom((byte[])clientData);
        Map<String, String> response=new HashMap<String,String>();
        PBMessage.SCPayRequestRet.Builder builder= PBMessage.SCPayRequestRet.newBuilder();
        for (Map.Entry<String, String> responseItem : response.entrySet()) {
            PBMessage.PBPayRequestItem.Builder itemBuilder= PBMessage.PBPayRequestItem.newBuilder();
            itemBuilder.setKey(responseItem.getKey());
            itemBuilder.setValue(responseItem.getValue());
            builder.addInfo(itemBuilder);
        }
        return new RetPacketImpl(OpCode.SCPayRequestRet, builder);
    }
    @Request(opcode = OpCode.CSAskServerTime)
    public RetPacket askServerTime(Object clientData,Session session) throws Exception{
        PBMessage.SCAskServerTimeRet.Builder reBuilder= PBMessage.SCAskServerTimeRet.newBuilder();
        reBuilder.setTime(Util.getSystemUtcTime()/1000);// 返回的是s
        return new RetPacketImpl(OpCode.SCAskServerTimeRet, reBuilder);
    }
    @Request(opcode = OpCode.CSLogout)
    public RetPacket handlerLogout(Object clientData,Session session) throws Exception{
        session.setExpired();
        PBMessage.SCLoginRet.Builder builder= PBMessage.SCLoginRet.newBuilder();
        eventService.fireEvent(new EventData((short)100));
        return new RetPacketImpl(OpCode.SCLogoutRet, builder);
    }
    @EventListener(event = 100)
    public void eventListener1(EventData eventData){
        System.out.println("eventListener1 eventData:"+eventData.getEvent());
    }
    @EventListener(event = 100)
    public void eventListener2(EventData eventData){
        System.out.println("eventListener2 eventData:"+eventData.getEvent());
    }
    @EventListener(event = 200)
    public void eventListener3(EventData eventData){
        System.out.println("eventListener2 eventData:"+eventData.getEvent());
    }
    @NetEventListener(netEvent = 222)
    public NetEventData testNetEvent1(NetEventData netEventData){
        System.out.println("netEventListener done:"+netEventData.getNetEvent());
        return new NetEventData(netEventData.getNetEvent(),null);
    }
    @Updatable(isAsynchronous = false)
    public void testUpdateSync1(int interval){
//        System.out.println("testUpdateSync1"+interval);
        System.out.println("idService,id:"+idService.acquireInt(TestService.class));
    }
    @Updatable(isAsynchronous = true,cronExpression = "1/10 * * * * ? *")
    public void testUpdateSync2(int interval){
//        System.out.println("testUpdateSync2:"+interval);
//        MyProxyTarget myProxyTarget = BeanHelper.getServiceBean(MyProxyTarget.class);
//        myProxyTarget.p1();
    }
    public void hahaha(){};
    protected void aaa(){}
    private  void bbb(){}

    public static class A{
        public void aaa(int a) {
            System.out.println("one");
        }
    }
    public static class B extends A{
//        @Override
        public void aaa(Integer a){
            System.out.println("two");
        }
    }
    public static void main(String[] args){
        B a = new B();
        a.aaa(new Integer(1));
    }

}
