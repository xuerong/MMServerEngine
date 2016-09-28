package com.mm.engine.framework.data.sysPara;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by a on 2016/9/28.
 */
public class DefaultSysParaStorage implements SysParaStorage {
    @Override
    public Map<String, String> getAllSysPara() {
        return new HashMap<>();
    }

    @Override
    public void insertSysPara(String key, String value) {

    }

    @Override
    public void update(String key, String value) {

    }

    @Override
    public void delete(String key) {

    }
}
