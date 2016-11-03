package com.mm.engine.netTest;

import com.mm.engine.framework.control.annotation.Request;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.RetPacketImpl;
import com.protocol.Test;
import com.protocol.TestOpcode;

/**
 * Created by a on 2016/11/3.
 */
@Service
public class TestService {
    @Request(opcode = TestOpcode.CSTest)
    public RetPacket testService(Object clientData,Session session) throws Exception{
        Test.CSTest csTest = Test.CSTest.parseFrom((byte[]) clientData);
        System.out.println("receive client data:"+csTest.getCsStr());
        Test.SCTest.Builder builder = Test.SCTest.newBuilder();
        builder.setScStr("back client data...");
        RetPacket ret = new RetPacketImpl(TestOpcode.SCTest,builder.build().toByteArray());
        return ret;
    }
}