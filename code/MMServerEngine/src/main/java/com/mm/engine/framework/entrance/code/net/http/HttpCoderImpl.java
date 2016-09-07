package com.mm.engine.framework.entrance.code.net.http;

import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.NetPacketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/13.
 */
public class HttpCoderImpl implements HttpEncoder, HttpDecoder {
    private static final Logger log = LoggerFactory.getLogger(HttpCoderImpl.class);

    @Override
    public HttpPacket encode(NetPacket packet) {
        Map<String,String> httpHeader=new HashMap<>();
        Map<String,Object> packetHeader=packet.getHeaders();
        for(Map.Entry<String,Object> entry : packetHeader.entrySet()){
            httpHeader.put(entry.getKey(),entry.getValue().toString());
        }
        HttpPacket httpPacket=new HttpPacketImpl(httpHeader,packet.getData());
        return httpPacket;
    }

    @Override
    public byte[] decode(HttpServletRequest request) throws IOException {
        //data
        String ignoredata = request.getHeader("IGNORE_DATA");// 当是空包的时候，如，logout,unity发不出来，故设此参数
        byte[] buffer = null;
        if(ignoredata==null || ignoredata.length()<=0){
            InputStream is = request.getInputStream();
            int bufSize = request.getContentLength();
            if(bufSize < 0){
                buffer = new byte[0];
//                log.error("request getContentLength={}",bufSize);
//                return null;
            }else{
                buffer = new byte[bufSize];
                int size = is.read(buffer);
                int readedSize = size;
                if (size != bufSize) {
                    while (size > -1) {
                        size = is.read(buffer, readedSize, bufSize - readedSize);
                        readedSize += size;
                    }
                }
            }
        }else {
            buffer = new byte[0];
        }
        return buffer;
    }
}
