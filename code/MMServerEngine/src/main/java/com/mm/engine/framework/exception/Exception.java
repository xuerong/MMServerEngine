package com.mm.engine.framework.exception;

/**
 * Created by Administrator on 2015/11/13.
 */
public interface Exception {
    public String getDescribe();
    public ExceptionLevel getExceptionLevel();
    public java.lang.Exception getException();
}
