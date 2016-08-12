package com.mm.engine.framework.entrance.http;

import com.mm.engine.framework.entrance.Entrance;
import com.mm.engine.framework.entrance.NetFlowFire;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by a on 2016/8/8.
 */
public class EntranceJetty extends Entrance{

    private Server server;

    public EntranceJetty(String name,int port){
        super(name,port);
    }

    @Override
    public void start() throws Exception{
        Handler entranceHandler = new AbstractHandler(){
            @Override
            public void handle(String target, Request baseRequest,
                               HttpServletRequest request, HttpServletResponse response) throws IOException{
                NetFlowFire.fireHttp(request,response,"EntranceJetty");
            }
        };

        server = new Server(this.port);
        server.setHandler(entranceHandler);
        server.start();
    }
    @Override
    public void stop() throws Exception{
        if(server != null){
            server.stop();
        }
    }
}
