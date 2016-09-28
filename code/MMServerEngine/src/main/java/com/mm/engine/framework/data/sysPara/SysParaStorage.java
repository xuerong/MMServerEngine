package com.mm.engine.framework.data.sysPara;

import java.util.Map;

/**
 * Created by a on 2016/9/27.
 */
public interface SysParaStorage {
    public Map<String,String> getAllSysPara();
    public void insertSysPara(String key,String value);
    public void update(String key,String value);
    public void delete(String key);
}
