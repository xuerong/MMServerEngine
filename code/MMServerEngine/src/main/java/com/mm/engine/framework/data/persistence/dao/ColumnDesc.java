package com.mm.engine.framework.data.persistence.dao;

/**
 * Created by a on 2016/9/21.
 */
public class ColumnDesc {
    private String field;
    private String type;
    private boolean key;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }
}
