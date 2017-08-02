EDS简介
	EventDrivenService 事件驱动服务（目前实现的协议为jms和kafka，中间件为ActiveMQ、Kafka）
主要特性
	1 可以对事件进行一对多转发
	2 优雅启停,针对所有消费者和单个消费者都可以随时启停
	3 异常处理机制，客户端发送失败重试机制；消费端失败请求默认处理6次将进入死信队列
	4 JMX监控
主要模块
	eds
	|-eds-core 核心接口
	|-eds-dispatch 框架核心功能实现模块，基于camel进行数据的路由
	|-eds-client 基于LMAX Disruptor实现的事件发送客户端，使用mapdb实现数据发送失败的重试机制
	|-eds-consumer 基于Spring BOOT实现的微服务，包含业务逻辑实现，并可以对外提供http接口服务
EDS开发规范：
  1.队列名称
	a) eds使用的队列用前缀"_eds_"标识，默认自动添加；
	b) 队列名称使用"队列类型:模块名:功能名"，如"_eds_l:auto_coupon:order"，l是logic缩写，表示业务逻辑处理，d是dispatch缩写，表示分发，一般不涉及到具体业务处理。
  2.日志
	日志在log4j2.xml中配置，为了方便debug和后期数据核对、分析，增加、修改日志必须谨慎，开发人员请与框架作者联系。
  3.依赖
	请勿在不熟悉依赖关系的情况下，修改pom.xml私自增加依赖。尤其小心由于增加依赖导致的版本冲突、日志漏打等。
  4.代码规范
	开发人员请在eds-consumer/com.coderjerry.eds.consumer.logic中编写逻辑代码

Change Log :
2015-8-26 初始化项目
2015-9-1  增加mapdb用于client异常处理和重试
2015-9-23 consumer增加支持kafka
2015-12-1 增加日志兼容，自动适配通用日志系统，顺序为：log4j2->log4j->slf4j，或者通过eds.logger系统属性指定日志系统（slf4j,log4j,jdk,log4j2)
版本号说明：
   1.0.0 线上版本（应改为1.0.0-RELEASE)
   1.1.0-RELEASE 灾备版本
   1.0.0-SNAPSHOT 测试环境版本
统一修改版本号：
   mvn versions:set -Pdev   
2016-02-05 增加按照system properties指定的consumer、dispatch启动应用
   如



运行：
```  mvn clean install ```



