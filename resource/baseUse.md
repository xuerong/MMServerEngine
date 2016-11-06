## 基本使用流程
###1.先能启动起来 
本项目使用windows操作系统IntelliJ IDEA 15.0开发，jdk版本为1.8，建议配置为该环境运行系统，如果使用其它环境，请自行解决兼容问题。<br>
数据库。新建mysql数据库，取名“test”，username=root，password=123456。将MMServerEngine/others/database下面的建表的sql文件在数据库中执行。<br>
memcache。下载windows版本的memcached应用，并启动。<br>
配置文件。第一、mainServer = [自己电脑的ip]:8001，第二，确定jdbc中的mysql参数正确，第三，确保配置文件中的配置的端口号均没有被占用（一般不会，除非运行了其它服务器，如tomcat常用的8080）。<br>
启动参数。打开Run/Debug Configurations对话框，配置一个Application，参数如下：<br>
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/serverStartArguments.png)<br>
其中，Program arguments说明，该服务实例为三种服务器。<br>
配置完成后运行server，即可启动服务器。如没有异常和error日志，说明正常启动了。

###2.使用系统提供的登陆登出服务 
系统提供了基于Protocol Buffer协议的登陆登出功能，参见MMServerEngine/others/proto/protos/AccountPB.proto文件，生成的java文件在com.protocol包中，具体生成方式参见“系统与周边-前后端协议”。<br>
使用的入口为com.mm.engine.sysBean.entrance .RequestNettyPBEntrance，使用的AccountService为com.mm.engine.sysBean.service. AccountService。<br>
运行com.mm.engine.netTest.AccountTest，其执行过程为：登陆mainServer、登陆nodeServer、登出账号。正常运行结果如下：<br>
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/accountTest.png)<br>
此时数据库中account表有对应的账号数据。<br>

###3.编写自己的Service 
在MMServerEngine/others/proto/protos下创建Test.proto，内容为<br>
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/testProto.png)<br>
运行父目录下面的proto.bat，可以在com.protocol包中得到Test和TestOpcode两个类。<br>
在com.mm.engine.netTest下面编写TestService.java，内容为：<br>
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/testServiceJava.png)<br>
重新运行服务器，然后，在com.mm.engine.netTest下面创建TestServiceClient.java文件，并将AccountTest的代码copy过来，再在其登陆nodeServer之后添加交互代码：<br>
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/testServiceClientJava.png)<br>
运行之，可进行一次服务器运行的交互：<br>
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/testServiceClientResult.png)<br>
服务器也将打印相应的日志：<br>
![image](https://github.com/xuerong/MMServerEngine/blob/master/resource/testServiceClientServerResult.png)