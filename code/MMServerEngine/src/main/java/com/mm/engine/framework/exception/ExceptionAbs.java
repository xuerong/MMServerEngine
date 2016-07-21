package com.mm.engine.framework.exception;

/**
 * Created by Administrator on 2015/11/13.
 */
public abstract class ExceptionAbs implements Exception{
    private ExceptionLevel level;
    private String describe;
    private java.lang.Exception e;
    public ExceptionAbs(ExceptionLevel level,java.lang.Exception e){
        this(level,e.getMessage(),e);
    }
    public ExceptionAbs(ExceptionLevel level,String describe ,java.lang.Exception e){
        this.level=level;
        this.describe=describe;
        this.e=e;
    }

    @Override
    public ExceptionLevel getExceptionLevel() {
        return level;
    }

    @Override
    public String getDescribe() {
        return describe;
    }

    @Override
    public java.lang.Exception getException() {
        return e;
    }

    @Override
    public String toString() {
        return new StringBuilder("exception---level:").append(level).append(",describe:")
                .append(describe).append(",StackTrace:").append(e.getStackTrace()).toString();
    }
}
