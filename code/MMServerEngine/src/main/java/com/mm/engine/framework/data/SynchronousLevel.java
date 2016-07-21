package com.mm.engine.framework.data;

/**
 * Created by Administrator on 2015/11/24.
 * 缓存同步级别，只有在分布式中才有意义，包括四个级别
 * NoSync:不进行同步：作用于不需要同时被多个线程、进程访问的
 * nottimely（public）:非及时同步，及时向公用缓存或数据库同步，不需要及时向其它访问者同步
 * Expired ：过期同步，需要向其它访问这发送过期信息，其它访问者可重新获取新的数据
 * real time：实时同步，需要向其他访问者发送改变的数据
 *
 *
 * 由于在ehcache中的缓存数据缓存的是引用，如果运行一个应用程序实例，除了NoSync，其它将将没有特殊功能
 *
 */
public enum SynchronousLevel {
    NoSync,// 默认
    Public,
    Expired,
    RealTime
}
