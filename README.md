MMServerEngine
======================================================================================================
## 概述 
### 是什么
MMServerEngine（简称MMSE）是一个游戏服务器引擎。也可用于非游戏的服务端应用，支持如下几点：
* 集群：可设置多个逻辑处理节点
* 缓存：支持本地和远程两层缓存
* 异步数据库：对于更新，插入和删除操作支持异步更新到数据库
* 服务层事务：任何一个服务处理可以添加事务
* MMServerEngine（简称MMSE）是一个游戏服务器引擎。也可用于非游戏的服务端应用
 
### 目标：
MMServerEngine设计的最主要的目标有两点：
* 插件化：即具体使用的技术（如缓存，网络，数据库等）可以通过插件的形式加入到本框架
* 使用高效：使游戏逻辑编写者尽量不用关注和具体游戏逻辑无关的枝节

### 框架 
* 集群框架  
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/sys.png)  
* Server框架  
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/server.png)  
* Service框架  
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/service.png)  

## [系统详述](https://github.com/xuerong/MMServerEngine/blob/master/resource/sysDetail.md) 
## [集群](https://github.com/xuerong/MMServerEngine/blob/master/resource/cluster.md) 
## [系统与周边](https://github.com/xuerong/MMServerEngine/blob/master/resource/around.md) 
## [重要原理和文件详述](https://github.com/xuerong/MMServerEngine/blob/master/resource/mainTheory.md) 
## [基本使用流程](https://github.com/xuerong/MMServerEngine/blob/master/resource/baseUse.md) 
## [一个小游戏案例Live](https://github.com/xuerong/MMServerEngine/blob/master/resource/live.md) 