package com.mm.engine.framework.exception;

/**
 * Created by Administrator on 2015/11/16.
 */
public final class ExceptionHelper {
    public static void handle(ExceptionLevel level,String describe,java.lang.Exception e){
        String log = "ExceptionLevel:"+level+",describe:"+describe;
        if(e != null){
            log+=e.getLocalizedMessage();
        }

        throw  new RuntimeException(log);
    }
}
