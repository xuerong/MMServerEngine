package com.mm.engine.framework.security.exception;

/**
 * Created by apple on 16-10-2.
 * 发往前端的异常,一般可用于弹框提示错误
 */
public class ToClientException extends RuntimeException {
    private int errCode = -10001;// TODO 异常代号:注意，这里不是访问号啊，后面要加一个
    private int opcode;
    private String errMsg = null;

    public ToClientException(){
        super();
    }
    public ToClientException(String msg) {
        super(msg);
        this.errMsg = msg;
    }

    public ToClientException(Throwable cause) {
        super(null, cause);
    }

    public ToClientException(int errCode, String msg) {
        super(msg);
        this.errCode = errCode;
        this.errMsg = msg;
    }

    public ToClientException(int errCode, Throwable cause) {
        super(null, cause);
        this.errCode = errCode;
    }

    public void setMessage(String message){
        this.errMsg = message;
    }
    public int getErrCode() {
        return errCode;
    }

    public String getMessage(){
        String tmp = this.errMsg;
        if (tmp==null && this.getCause()!=null){
            tmp = this.getCause().getMessage();
        }
        return tmp;
    }
}
