# MMServerEngine 


[测试连接](https://github.com/xuerong/MMServerEngine/blob/master/resource/test.md "悬停显示") 

MMServerEngine是一个游戏服务器引擎。支持如下几点：
* 集群：可设置多个逻辑处理节点。
* 缓存：支持本地和远程两层缓存
* 异步数据库：对于更新，插入和删除操作支持异步更新到数据库
* 服务层事务：任何一个服务处理可以添加事务。
 
#目标：
MMServerEngine设计的最主要的目标有两点：
* 插件化：即具体使用的技术（如缓存，网络，数据库等）可以通过插件的形式加入到本框架
* 使用高效：使游戏逻辑编写者尽量不用关注和具体游戏逻辑无关的枝节

# 框架 
* 系统框架  
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/sys.png)  
* Server框架  
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/server.png)  
* Service框架  
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/service.png)  

# 使用  
#####1、编写客户端消息入口  
* 要根据使用的协议定义入口，入口继承自Entrance，
* 对进入的消息解析出一个访问标识符（opcode）和一个对象,如果需要session，也要解析出session的id，并通过SessionService来创建获取删除等
* 调用RequestService.handle方法处理  

#####2、编写Service    
* 对服务添加Service注解，其变量init和destroy为初始化和销毁的方法
* 服务包括Request,Event,Update,Job,NetEvent
* 如果需要使用其它Service，可以通过BeanHelper.getServiceBean方法获得。（如果设置成Service的变量，系统会对serviceBean进行依赖注入） 

#####3、编写数据库对应实体类  
* 需要用DBEntity来注解，如果字段名字和表的字段名不同，要添加Column注解
* 继承Serializable接口，因为需要进行缓存
* 通过DataService来对DB数据进行操作

#####4、Request  
* 对于处理入口来的某个opcode的方法添加Request注解，并设置opcode变量

#####5、Event  
* 接受事件的服务方法，要添加EventListener注解，并设置event变量
* 发送事件要先定义EventData对象，设置其event标识，和发送的对象
* 通过EventService发送事件，其中fireEvent异步发送事件，fireEventSyn同步发送事件

#####6、NetEvent（网络事件在编写普通服务的时候极少用到）  
* 接受事件的服务方法，要添加NetEventListener注解，并设置netEvent变量。注意，NetEventListener只能有一个，如果需要多个可通过Event转发
* 发送事件要先定义NetEventData对象，设置其netEvent标识，和发送的对象
* 通过NetEventService来发送事件，有多种发送方式，具体参见NetEventService
* 系统有默认的网络事件入口，如果需要自己的网络事件入口（如特定的协议编解码），可自己编写，并在配置文件中配置

#####7、Update  
* 对相应的方法使用Updatable注解
* update可以设置同步调用和异步调用，其中同步调用将使用系统的调用周期（可在配置文件中配置），不易添加复杂的逻辑
* 通过runEveryServer设置其是否在所有的服务器中运行（鉴负载与集群），若否，则将由系统分配其运行服务器
* 调用时间点可通过cycle设置一个常数，或者通过cronExpression设置，注意，若cronExpression设置了，cycle将不起作用

#####8、Job  
* job是指在某个时间之后做一件事情
* 定义一个Job对象，设置其id（要求唯一，可以通过Service名+方法名+玩家id等）、执行时间、服务类和方法、参数
* 通过设置db来设置该job在系统重启之后是否要加载
* 通过JobService来启动或者删除job

#####9、tx事务  
* 需要添加事务的方法添加Tx注解
* 变量tx确定是否使用事务
* lock使DBEntity的提交加锁，而lockClass可以设置该事务加锁DBEntity的类型，不设置所有flush的DBEntity都将被加锁。
* 加锁必须在事务内，如果方法调用时已经在事务内，则共用已经有的事务。注意：由于事务提交之后才解锁事务中加的锁，所以在同一个事务中不易有太多的加锁操作

#####10、全局锁（较少用到）  
* 如果需要加全局锁，即多个服务器共用锁，用LockerService可以完成，但需要自己解锁

#####11、缓存服务（较少用到）  
* 通过CacheService可以缓存自己想要缓存的对象

#####12、运行状态监控（还未实现）  
* 通过MonitorService可以来监控自己服务的运行状态


# 待实现 
#####1、安全控制  
* 安全监控和异常处理
* 访问和权限
* 加密
* 冗余、备份和回滚

#####2、项目相关  
* gm系统
* 策划配数
* 系统变量
* 过滤和敏感字
* 单元测试
* 工具

#####3、周边  
* 支付 
* 社交
* 系统部署
* 运营支持
