package com.mm.engine.framework.data.sysPara;

import com.mm.engine.framework.data.persistence.orm.annotation.DBEntity;

/**
 * Created by apple on 16-10-2.
 * 存储系统变量
 */
@DBEntity(tableName = "sysPara",pks = {"id"})
public class SysPara {
    private String id;
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
