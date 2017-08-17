# EDS简介
EventDrivenService 事件驱动服务
基于[Spring Boot](https://github.com/spring-projects/spring-boot)，[Apache Camel](https://github.com/apache/camel)等开源技术的消息发布和消费路由服务。
# 主要特性
1 数据(带事务)发布、转发及消费。可以对数据进行一对一及一对多转发和消费。
2 优雅启停。由于Apache Camel设计的优雅和jolokia的可靠，可通过jolokia客户端(如富客户端[hawt-io](http://hawt.io/))针对所有消费者和单个消费者都可以随时启停
3 异常处理机制，客户端发送失败重试机制；消费端失败请求默认处理6次将进入死信队列。
4 JMX监控。
# 主要模块
eds
	|-eds-core 核心接口类
	|-eds-dispatch 框架核心功能实现模块，基于camel进行数据的路由
	|-eds-client 事件发布客户端。使用[LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor)作数据缓冲，使用[mapdb](https://github.com/jankotek/mapdb)实现数据发送失败的重试机制
	|-eds-consumer 基于Spring BOOT实现的微服务，包含业务逻辑实现，并可以对外提供http接口服务
	|-eds-client-all 方便打fat包
# EDS开发说明：
## 1.队列名称
a) eds使用的队列用前缀"_eds_"标识，默认自动添加；
b) 队列名称使用"队列类型:模块名:功能名"，如"_eds_l:auto_coupon:order"，l是logic缩写，表示业务逻辑处理，d是dispatch缩写，表示分发，一般不涉及到具体业务处理。
## 2.日志
日志在log4j2.xml中配置 
## 3.依赖
修改pom.xml增加依赖时，要尤其小心由于增加依赖导致的版本冲突。
## 4.开发代码
### a) eds-client的使用
在应用中嵌入eds-client的使用（需要deploy到私库中）
增加maven依赖：	
```xml
	<dependency>
    <groupId>com.coderjerry</groupId>
    <artifactId>eds-client-all</artifactId>
    <version>1.0.0-SNAPSHPT</version>
  </dependency>
```
在classpath中增加eds-client.properties文件，用于配置发送的中间件（目前只支持activemq）
```properties
# 指定发布到的中间件地址
activemq.brokerUrl=tcp://192.168.3.103:61618
# 中间件发布并行度，由依赖的中间件设置
activemq.publisher.concurrent=5
# 是否默认，如有多个应选择一个为默认；如果没有选择将使用第一个为默认中间件
activemq.publisher.isDefault=true
# 发布器的线程数，等于Disruptor中workpool的线程数
publishManager.processors=10
# Disruptor的环形缓冲大小，必须是2的平方
publishManager.ringBufferSize=131072
# 用于发送重试机制的mapdb文件位置
all.publisher.redoFileName=E:/data/www/logs/eds/client/redo/redo.db
```

发布消息：
```java
String edsQueue = "some_queue";
Eds.publish(edsQueue, data);
```
### b) 消费者	
eds-consumer项目可通过jar包或war包进行单独部署，在使用中按需修改下列文件：
- eds-consumer/pom.xml 修改profile对应的变量，如mq的地址等
- eds-consumer/resources/application.properties spring-boot配置文件
- eds-consumer/resources/dispatch.yml 消息路由转发配置，如
```
dispatchers:
  list:
    - name: demoRouter 
      from: d.demo # 来源队列名称
      fromProtocol: activemq # 数据来源协议
      concurrencyMin: 1 # 并发性设置
      concurrencyMax: 100
      to: # 转发目的地
        - l.demo1
        - l.demo2
      toProtocol: activemq # 转发数据协议 
```
- eds-consumer/resources/consumer.yml 消费者配置，如
```
consumers:
  list:
   - name: demo1Consumer
     from: l.demo1 # 来源队列名称
     fromProtocol: activemq
     concurrencyMin: 2
     concurrencyMax: 20 
     type: internal # internal 表示消息来源是eds-client发送，将会为from添加_eds_前缀，否则指定为custom将不添加前缀
     processor: demo1Consumer # 消费者在spring容器中注册的名称
     options: concurrentConsumers=1&maxConcurrentConsumers=20 # 额外选项，会组合到from中
   - name: demo2Consumer
     from: l.demo2
     fromProtocol: activemq
     concurrencyMin: 2
     concurrencyMax: 20
     type: internal
     processor: demo2Consumer
```
- eds-consumer/resources/dynamic_conf.properties 使用apache commons-configuration实现的动态配置文件加载
- eds-consumer/resources/jolokia-access.xml jolokia配置，可根据IP配置访问限制
- eds-consumer/resources/META-INF/dubbo/ 如使用了dubbo，可通过配置Filter进行代码增强、日志打印等
- eds-consumer/resources/com.coderjerry.eds.consumer.mybatis.mapper.user 集成mybatis的测试类

请在eds-consumer/com.coderjerry.eds.consumer.logic中编写逻辑代码，代码示例可参照
eds-consumer/com.coderjerry.eds.consumer.logic.Demo1Consumer
eds-consumer/com.coderjerry.eds.consumer.logic.Demo2Consumer


### c) 部署运行：
打包`mvn clean install -Pdev`或`mvn clean package -Pdev`，可增加-Dmaven.test.skip=true跳过单元测试

运行`java -jar eds-consumer/target/eds-consumer-1.0.0-SNAPSHOT.jar `或修改eds-consumer/pom.xml中的`<packaging>war</packaging>`，将打成war包

# 使用的一些开源技术：
- [Spring Boot](https://github.com/spring-projects/spring-boot)
- [Apache Camel](https://github.com/apache/camel)
- [LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor)
- [Jolokia](https://jolokia.org/)
- [Log4j2](http://logging.apache.org/log4j/2.x/)
- [ActiveMQ](http://activemq.apache.org/)
- [Kafka](http://kafka.apache.org/)
- [Gearman](http://gearman.org/)




**闲聊**

eds作为一个项目的原始需求是，在业务项目之外，需要一套松耦合的实时计算服务，通过业务事件触发计算服务，实现一些如“连续登录送金币”、“消费满100元送代金券”等等的需求。

那时候我对[Dianping Cat](https://github.com/dianping/cat)挺着迷，就想着能站在巨人肩膀上写几行代码试试。所以，就参考Cat的结构，构建了eds-client作为发送客户端，eds-consumer作为消费者的主要架构。

虽然代码已在生产环境运行了较长一段时间，但因水平有限，还是有很多不足。通过这个项目，让自己也成长了不少，尤其对并发、通信的了解有了一些长进。对于Disruptor的研究也让我从另一个视野见识到了并发的坏味道。


Change Log :
- 2015-8-26 初始化项目
- 2015-9-1  增加mapdb用于client异常处理和重试
- 2015-9-23 consumer增加支持kafka
- 2015-12-1 增加日志兼容，自动适配通用日志系统，顺序为：log4j2->log4j->slf4j，或者通过eds.logger系统属性指定日志系统（slf4j,log4j,jdk,log4j2)
- 2015-12 上线使用
- 2016-02-05 增加按照system properties指定的consumer、dispatch启动应用
- 2017-08 基于线上版本，重构eds并开源