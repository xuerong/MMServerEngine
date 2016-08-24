package com.mm.engine.framework.server;

/**
 * Created by a on 2016/8/12.
 */
public final class SysConstantDefine {
    // -------------------------------------------NetEvent------------------------
    public static final int CACHEUPDATE = 1000; // 缓存更新
    public static final int LOCKKEYSANDCHECK = 1001; // 加锁并校验版本等
    public static final int UNLOCKKEYS = 1002; //解锁
    public static final int ASYNCDATA = 1003; // 异步更新数据
    public static final int LOCKKEYS = 1004; // 加锁
    public static final int GETASYNCDATABELONGLISTKEY = 1004; // 从异步数据更新从DB获取的list
    // ------------------------------------------返回客户端特殊数据包的operCode
    public static final int NULLOBJCE = 1100; // 数据处理函数返回null
}
