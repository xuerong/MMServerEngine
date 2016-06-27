package com.mm.test;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Cache;

/**
 * Created by Administrator on 2015/11/10.
 */
public class TestEhcache {
    public static void main(String[] args){

        System.out.println();

        //初始化
        CacheManager manager = new CacheManager("src/main/resources/ehcache.xml");
        //获取指定Cache对象
        Cache configCache = manager.getCache("configCache");

        //创建节点对象
        Element element = new Element("key1","value1");
        //保存节点到configCache
        configCache.put(element);
        //从configCache获取节点
        Element element2 = configCache.get("key1");
        Object  value = element2.getValue();
        //更新节点
        configCache.put(new Element("key1","value2"));
        //删除节点
        configCache.remove("key1");
        System.out.println("--"+value);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //卸载缓存管理器
        manager.shutdown();
    }
}
