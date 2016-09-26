package com.mm.engine.framework.data.table;

import com.mm.engine.framework.security.exception.MMException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by a on 2016/9/26.
 */
public class Record {

    private Map<String,Object> valueMap = new HashMap<>();

    public Object get(String key){
        Object value = valueMap.get(key);
        if(value == null){
            throw new MMException("key is not exist, key = "+key);
        }
        return value;
    }

    public int getInt(String key){
        return (Integer)get(key);
    }
    public float getFloat(String key){
        return 0f;
    }
}
