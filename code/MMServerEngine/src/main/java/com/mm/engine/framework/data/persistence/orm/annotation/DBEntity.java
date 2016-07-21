package com.mm.engine.framework.data.persistence.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义 DBEntity 类
 *
 * 所有的数据库对象都要添加该注解，如果需要缓存则，继承自CacheEntity接口
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBEntity {
    String tableName();
}
