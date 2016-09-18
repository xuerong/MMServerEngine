package com.mm.engine.framework.data.entity.account;

import com.mm.engine.framework.security.exception.MMException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by a on 2016/9/18.
 * nodeServer的状态
 */
public class NodeServerState {

    private String host;
    // 这里的port是指request的port，不是netEvent的port
    private int port;

    private int workload; // 负载
    private int accountCount; // 账户数量

    private Set<String> accountIdSet = new HashSet<>();

    public String getKey(){
        return host+":"+port;
    }

    public synchronized void addAccount(String accountId){
        boolean newOne = accountIdSet.add(accountId);
        if(newOne){
            accountCount ++;
            workload++;
        }
    }
    public synchronized void removeAccount(String accountId){
        if(accountCount<=0 || workload<=0 ){
            throw new MMException("accountCount < 0");
        }
        boolean has = accountIdSet.remove(accountId);
        if(has){
            accountCount --;
            workload --;
        }
    }

    public Set<String> getAccountIdSet() {
        return accountIdSet;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }

    public int getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(int accountCount) {
        this.accountCount = accountCount;
    }
}
