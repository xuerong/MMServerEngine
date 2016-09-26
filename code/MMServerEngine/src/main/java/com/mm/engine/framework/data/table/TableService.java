package com.mm.engine.framework.data.table;

import com.mm.engine.framework.security.exception.MMException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by a on 2016/9/26.
 */
public class TableService {

    private Map<String,Table> tableMap = new HashMap<>();

    public Table getTable(String tableName){
        Table table = tableMap.get(tableName);
        if(table == null){
            throw new MMException("table is not exist ,tableName = "+tableName);
        }
        return table;
    }

    public <T> List<T> getTable(Class<T> cls){
        return null;
    }
}
