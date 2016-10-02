package com.mm.engine.framework.net.entrance.http;

import com.mm.engine.framework.net.entrance.Entrance;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by a on 2016/8/8.
 * TODO 由于未弄明白如何加载jar包给web容器，所以，现在是把需要的jar文件拷贝到了F:\0322JDK8\jre\lib\ext目录下，即实际运行的jre对应的目录下面，
 * TODO 后面部署的时候，要么弄明白原理，要么就拷贝
 */
public class GmEntranceJetty extends Entrance{
    private static final Logger log = LoggerFactory.getLogger(GmEntranceJetty.class);
    /**
     * 这个目录是服务的根目录，即浏览器访问的时候要添加这个目录
     * 而web.xml过滤的目录都是建立在该目录下面的，如：
     * 浏览器访问：http://localhost:8081/gm/index.jsp
     * 如果要过滤，只需要/*,而不是/gm/*
     */
    private String contextPath = "/gm";
//    private String resourceBase = "./src/main/webapp";// TODO 这个目录真是太蛋疼了，部署的时候要注意
//    private String descriptor = "./src/main/webapp/WEB-INF/web.xml";
    private String resourceBase = "./target/mmserverengine";// TODO 这个目录真是太蛋疼了，部署的时候要注意
    private String descriptor = "./target/mmserverengine/WEB-INF/web.xml";




    private Server server;

    public GmEntranceJetty(){}

    @Override
    public void start() throws Exception{
        try {
            // 服务器的监听端口
            server = new Server(port);

            // 关联一个已经存在的上下文
            WebAppContext context = new WebAppContext();

            // 设置描述符位置
            context.setDescriptor(descriptor);

            // 设置Web内容上下文路径
            context.setResourceBase(resourceBase);

            // 设置上下文路径
            context.setContextPath(contextPath);
            context.setParentLoaderPriority(true);

            server.setHandler(context);

            // 启动
            server.start();

            System.out.println("gm模块启动,使用url:http://localhost:"+port+contextPath+" 进行访问");

//            server.join();

        } catch (Throwable e) {
            e.printStackTrace();
        }
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
