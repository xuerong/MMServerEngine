package com.mm.engine.framework.tool.util;

import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.Server;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.RemoteEndpoint;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/11/16.
 */
public final class Util {
    // 获取http访问的ip
    public static String getIp(HttpServletRequest request) {
        // We look if the request is forwarded
        // If it is not call the older function.
        String ip = request.getHeader("X-Pounded-For");
        if (ip != null) {
            return ip;
        }
        ip = request.getHeader("x-forwarded-for");

        if (ip == null) {
            return request.getRemoteAddr();
        } else {
            // Process the IP to keep the last IP (real ip of the computer on
            // the net)
            StringTokenizer tokenizer = new StringTokenizer(ip, ",");

            // Ignore all tokens, except the last one
            for (int i = 0; i < tokenizer.countTokens() - 1; i++) {
                tokenizer.nextElement();
            }
            ip = tokenizer.nextToken().trim();
            if (ip.equals("")) {
                ip = null;
            }
        }
        // If the ip is still null, we insert 0.0.0.0 to avoid null values
        if (ip == null) {
            ip = "0.0.0.0";
        }

        return ip;
    }
    // 获取websocket的ip
    //the socket object is hidden in WsSession, so you can use reflection to got the ip address.
    // the execution time of this method is about 1ms. this solution is not prefect but useful.
    public static String getIp(javax.websocket.Session session){
        RemoteEndpoint.Async async = session.getAsyncRemote();
        InetSocketAddress addr = (InetSocketAddress) getFieldInstance(async,
                "base#sos#socketWrapper#socket#sc#remoteAddress");
        if(addr == null){
            return "127.0.0.1";
        }
        return addr.getAddress().getHostAddress();
    }
    private static Object getFieldInstance(Object obj, String fieldPath) {
        String fields[] = fieldPath.split("#");
        for(String field : fields) {
            obj = getField(obj, obj.getClass(), field);
            if(obj == null) {
                return null;
            }
        }
        return obj;
    }

    private static Object getField(Object obj, Class<?> clazz, String fieldName) {
        for(;clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field;
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
            }
        }
        return null;
    }
    public static boolean isLocalHost(String host){
        if(host.equals("localhost") || host.equals("127.0.0.1")){
            return true;
        }
        String localHost = getHostAddress();
        if(host.equals(localHost)){
            return true;
        }
        return false;
    }
    private static String hostAddress = null;
    public static String getHostAddress(){
        if(hostAddress == null){
            try {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            }catch (UnknownHostException e){
                throw new MMException(e);
            }
        }
//        System.out.println(hostAddress);
        return hostAddress;
    }
    public static String getLocalNetEventAdd(){
        return getHostAddress()+""+ Server.getEngineConfigure().getNetEventEntrance();
    }
    public static boolean isIP(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        return ipAddress;
    }

    /** 获取服务器的utc时间的long值  单位ms**/
    public static long getSystemUtcTime(){
        return Calendar.getInstance().getTimeInMillis();
    }
}
