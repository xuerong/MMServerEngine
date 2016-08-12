package com.mm.engine.sysBean.dbentity;

import com.mm.engine.framework.data.persistence.orm.annotation.DBEntity;

/**
 * Created by Administrator on 2015/11/26.
 */
@DBEntity(tableName = "user",pks = {"id"})
public class User {
    private int id;
    private String name;
    private String pass;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
