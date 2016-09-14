package com.mm.engine.framework.net.entrance.http;

import com.mm.engine.framework.net.entrance.Entrance;
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

    public EntranceJetty(){}

    @Override
    public void start() throws Exception{
        Handler entranceHandler = new AbstractHandler(){
            @Override
            public void handle(String target, Request baseRequest,
                               HttpServletRequest request, HttpServletResponse response) throws IOException{

            }
        };

        server = new Server(this.port);
        server.setHandler(entranceHandler);
        server.start();
    }
    private void fire(){

    }
    @Override
    public void stop() throws Exception{
        if(server != null){
            server.stop();
        }
    }
}
