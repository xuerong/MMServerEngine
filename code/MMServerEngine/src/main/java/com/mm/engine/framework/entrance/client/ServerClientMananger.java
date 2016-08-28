package com.mm.engine.framework.entrance.client;

import com.mm.engine.framework.server.Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 16-8-28.
 */
public class ServerClientMananger {
    private static List<ServerClient> nodeServerClientList; // 所有的serverClient
    private static ServerClient mainServerClient; // 主服务器
    private static ServerClient asyncServerClient; // 异步服务器

    static{

    }

    public static List<ServerClient> getNodeServerClientList(){
        return nodeServerClientList;
    }

    public static ServerClient getMainServerClient(){
        return mainServerClient;
    }
    public static ServerClient getAsyncServerClient(){
        return asyncServerClient;
    }
}
