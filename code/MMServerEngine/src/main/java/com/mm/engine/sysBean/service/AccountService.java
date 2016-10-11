package com.mm.engine.sysBean.service;

import com.mm.engine.framework.control.annotation.Request;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.data.entity.account.Account;
import com.mm.engine.framework.data.entity.account.AccountSysService;
import com.mm.engine.framework.data.entity.account.LoginSegment;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.entity.session.SessionService;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.RetPacketImpl;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.protocol.AccountOpcode;
import com.protocol.AccountPB;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2016/9/20.
 */
@Service(init = "init")
public class AccountService {
    // account-session
    private ConcurrentHashMap<String,Session> sessionMap;

    public AccountSysService accountSysService;

    public void init(){
        sessionMap = new ConcurrentHashMap<>();
    }

    @Request(opcode = AccountOpcode.CSLoginMain)
    public RetPacket loginMain(Object data, Session session) throws Throwable{
        if(!ServerType.isMainServer()){
            throw new MMException("login fail,this is not mainServer");
        }
        AccountPB.CSLoginMain csLoginMain = AccountPB.CSLoginMain.parseFrom((byte[])data);
        String accountId = csLoginMain.getAccountId();

        LoginSegment loginSegment = accountSysService.loginMain(accountId,session.getUrl(),session.getIp());
        Account account = loginSegment.getAccount();

        {
            // 一些对account的设置，并保存
        }

        AccountPB.SCLoginMain.Builder builder = AccountPB.SCLoginMain.newBuilder();
        builder.setSessionId(loginSegment.getSessionId());
        builder.setHost(loginSegment.getHost());
        builder.setPort(loginSegment.getPort());
        RetPacket retPacket = new RetPacketImpl(AccountOpcode.SCLoginMain,false,builder.build().toByteArray());
        return retPacket;
    }
    @Request(opcode = AccountOpcode.CSLogoutMain)
    public RetPacket logoutMain(Object data,Session session) throws Throwable{
        if(!ServerType.isMainServer()){
            throw new MMException("logout fail,this is not mainServer");
        }
        AccountPB.CSLogoutMain csLogoutMain = AccountPB.CSLogoutMain.parseFrom((byte[])data);
        String accountId = csLogoutMain.getAccountId();
        accountSysService.logoutMain(accountId);

        AccountPB.SCLogoutMain.Builder builder = AccountPB.SCLogoutMain.newBuilder();
        RetPacket retPacket = new RetPacketImpl(AccountOpcode.SCLogoutMain,false,builder.build().toByteArray());
        return retPacket;
    }
    @Request(opcode = AccountOpcode.CSLoginNode)
    public RetPacket loginNode(Object data,Session session) throws Throwable{

        System.out.println("doLogin,sessionId = "+session.getSessionId());

        AccountPB.CSLoginNode csLoginNode = AccountPB.CSLoginNode.parseFrom((byte[])data);

        accountSysService.loginNodeServer(csLoginNode.getAccountId(),csLoginNode.getSessionId());

        AccountPB.SCLoginNode.Builder builder = AccountPB.SCLoginNode.newBuilder();
        RetPacket retPacket = new RetPacketImpl(AccountOpcode.SCLoginNode,false,builder.build().toByteArray());
        return retPacket;
    }

}
