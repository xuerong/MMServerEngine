package com.mm.engine.framework.control.job;

import java.util.Date;

/**
 * Created by apple on 16-9-4.
 * job分为两种:
 * 一种是重复执行:用cronExpression表示,可以用注解
 * 一种是指执行一次:用时间段表示
 *
 */
public class Job {
    private String id;

    //
    private Date startTime; // 第一次执行时间,
    private String cronExpression; // 执行时间表达式,如果存在,会多次执行

    private boolean db; // 是否持久化

    private String method;
    private Class serviceClass;
    private Object[] para;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isDb() {
        return db;
    }

    public void setDb(boolean db) {
        this.db = db;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Object[] getPara() {
        return para;
    }

    public void setPara(Object... para) {
        this.para = para;
    }

}
