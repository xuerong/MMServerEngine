package com.mm.engine.framework.entrance.http;

import com.mm.engine.framework.entrance.NetFlowFire;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.http.HttpDecoder;
import com.mm.engine.framework.entrance.code.net.http.HttpEncoder;
import com.mm.engine.framework.entrance.code.net.http.HttpPacket;
import com.mm.engine.framework.entrance.ControllerDispatcher;
import com.mm.engine.framework.entrance.NetType;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/13.
 *
 * 引擎的http入口类
 */
@WebServlet(name = "EntranceServlet",urlPatterns={"/http/*"} ,loadOnStartup=1)
public class EntranceServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(EntranceServlet.class);
    static{
        Server.init();
    }
    @Override
    public void init() throws ServletException{
        super.init();
        System.out.println("start------------------------");
        Server.start();

    }
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        NetFlowFire.fireHttp(request,response,"EntranceServlet");
    }
    @Override
    public void destroy() {
        System.out.println("stop-------------------");
        super.destroy();
        Server.stop();
    }
}
