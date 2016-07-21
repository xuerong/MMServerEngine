package com.mm.engine.sysBean;

import com.mm.engine.framework.control.annotation.EventListener;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.aop.annotation.AspectMark;

/**
 * Created by Administrator on 2015/11/17.
 */
@Service
public class MyProxyTarget {
    @AspectMark(mark = {"aa"})
    public void p1(){
        System.out.println("test proxy-aa");
    }
    @AspectMark(mark = {"bb"})
    public void p2(){
        System.out.println("test proxy-bb");
    }

    @AspectMark(mark = {"aa","bb"})
    public void p3(){
        System.out.println("test proxy-aa,bb");
    }
    @AspectMark(mark = {"cc"})
    public void p4(){
        System.out.println("test proxy-cc");
    }
}
