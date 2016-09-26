package com.mm.engine.framework.data.table;

import com.mm.engine.framework.security.exception.MMException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by a on 2016/9/26.
 */
public class Table{
    private Map<Integer,Record> recordMap = new HashMap<>();

    public Record getRecord(int id){
        Record record = recordMap.get(id);
        if(record == null){
            throw new MMException("record is not exist,id="+id);
        }
        return record;
    }

    public Map<Integer, Record> getRecordMap() {
        return recordMap;
    }
}
