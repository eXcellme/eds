package com.coderjerry.eds.dispatch.config;

import com.coderjerry.eds.dispatch.messageconvertor.EventMessageConvertor;
import com.coderjerry.eds.dispatch.router.DispatcherRouter;
import com.coderjerry.eds.dispatch.router.LogicRouter;
import com.coderjerry.eds.dispatch.util.PerformanceLogHelper;
import com.coderjerry.eds.dispatch.util.RequestIdManager;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.camel.component.ActiveMQConfiguration;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.ThreadPoolRejectedPolicy;
import org.apache.camel.component.log.LogComponent;
import org.apache.camel.spi.ThreadPoolProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jmx.export.annotation.ManagedResource;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ManagedResource 
public class EdsCamelConfig {
	
//	@Autowired
//	CamelContext camelContext ;
	// jms start 
	@Value(value="${jms.userName}")
	private String jmsUserName ;
	@Value(value="${jms.password}")
	private String jmsPassword ;
	@Value(value="${jms.brokerUrl}")
	private String jmsBrokerUrl ;
	@Value(value="${jms.maxPooledConnections}")
	private int jmsMaxPooledConnections;
	@Value(value="${jms.minConsumers}")
	private int jmsMinConsumers;
	@Value(value="${jms.maxConsumers}")
	private int jmsMaxConsumers;
	// jms end 
	@Value(value="${thread.poolSize}")
	private int threadPoolSize;
	@Value(value="${thread.maxPoolSize}")
	private int threadMaxPoolSize;
	@Value(value="${thread.maxQueueSize}")
	private int threadMaxQueueSize;

	@Autowired
	private ConfigurableEnvironment env;

	@Value(value="${serviceId}")
	private String serviceId ;
//	transactionManger database有一个txman，这里会重复
//	@Bean(name="txManager")
//	JmsTransactionManager transactionManger(){
//		return new JmsTransactionManager(connectionFactory());
//	}

  @Bean(name="connectionFactory")
	public ConnectionFactory connectionFactory(){
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
//		activeMQConnectionFactory.setUseAsyncSend(true);
		activeMQConnectionFactory.setUserName(jmsUserName);
		activeMQConnectionFactory.setPassword(jmsPassword);
		activeMQConnectionFactory.setBrokerURL(jmsBrokerUrl);

		// 默认重复投递6次将转发到死信队列,改为无限次数
//		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
//		redeliveryPolicy.setMaximumRedeliveries(-1);
//		
//		activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
		
		PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
		pooledConnectionFactory.setMaxConnections(jmsMaxPooledConnections);
//		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(pooledConnectionFactory);
//		cachingConnectionFactory.setSessionCacheSize(10);
		return pooledConnectionFactory;
	}
	
//	@Bean(name="jms")
//	JmsComponent jmsComponent(){
//		// 修改为activemq component ，activemq做了调优
//		JmsConfiguration config = new JmsConfiguration();
//		config.setConnectionFactory(connectionFactory());
//		JmsComponent component = new JmsComponent(config);
//		component.setMessageConverter(new EventMessageConvertor());
//		// session 客户端确认/无事务
//		component.setAcknowledgementMode(Session.CLIENT_ACKNOWLEDGE); 
//		// 针对本地事务（非XA事务的调优）
//		component.setCacheLevelName("CACHE_CONSUMER");
//		return component;
//	}
	
	@Bean(name="activemq")
	ActiveMQComponent activeMQComponent(){
		// 修改为activemq component ，activemq做了调优
		ActiveMQConfiguration config = new ActiveMQConfiguration();
//		config.setMapJmsMessage(false); // 自动映射jms消息
		config.setConcurrentConsumers(jmsMinConsumers);
		config.setMaxConcurrentConsumers(jmsMaxConsumers);
		config.setConnectionFactory(connectionFactory());
//		config.setAcknowledgementMode(cc);
//		config.setAcknowledgementMode(Session.CLIENT_ACKNOWLEDGE);
		config.setAcknowledgementMode(Session.SESSION_TRANSACTED);
		// 事务控制
//		config.setTransactionManager(transactionManger());
		config.setTransacted(true);
		ActiveMQComponent component = new ActiveMQComponent(config);
		component.setMessageConverter(new EventMessageConvertor());
		// session 客户端确认/无事务
//				component.setAcknowledgementMode(Session.AUTO_ACKNOWLEDGE); 
//				component.setAcknowledgementMode(Session.CLIENT_ACKNOWLEDGE); 
		// 针对本地事务（非XA事务的调优）
		component.setCacheLevelName("CACHE_CONSUMER");
		return component;
	}
	
	@Bean(name="log")
	LogComponent getLogComponent(){
		return new LogComponent();
	}
	
	@Bean(name="eventMessageConvertor")
	MessageConverter getMessageConvertor(){
		return new EventMessageConvertor();
	}
	
	/**  
	* thread pool of consumer
	*/
	@Bean(name="defaultThreadPoolProfile")
	ThreadPoolProfile threadPoolProfile(){
		ThreadPoolProfile defaultThreadPoolProfile = new ThreadPoolProfile();
		defaultThreadPoolProfile.setDefaultProfile(true);
		defaultThreadPoolProfile.setId("defaultThreadPoolProfile");
		defaultThreadPoolProfile.setPoolSize(threadPoolSize);
		defaultThreadPoolProfile.setMaxPoolSize(threadMaxPoolSize);
		defaultThreadPoolProfile.setMaxQueueSize(threadMaxQueueSize); // 队列最大程度1000万
		defaultThreadPoolProfile.setTimeUnit(TimeUnit.SECONDS);
		defaultThreadPoolProfile.setKeepAliveTime(60 * 5L);
		defaultThreadPoolProfile.setRejectedPolicy(ThreadPoolRejectedPolicy.CallerRuns);
//		camelContext().getExecutorServiceManager().registerThreadPoolProfile(defaultThreadPoolProfile);
//		setDefaultThreadPoolProfile(defaultThreadPoolProfile);
		return defaultThreadPoolProfile;
	}
	
	@Bean(name="requestIdManager")
	RequestIdManager requestIdManager(){
	    return new RequestIdManager(serviceId);
	}
	
	@Bean(name="dispatcherHelper")
	PerformanceLogHelper dispatcherHelper(){
	  return new PerformanceLogHelper(requestIdManager(), "PERF-DISPATCHER");
	}
	
	@Bean(name="consumerHelper")
	PerformanceLogHelper consumerHelper(){
	  return new PerformanceLogHelper(requestIdManager(), "PERF-CONSUMER");
	}

	// spring PropertySource can not load yaml by default
	@Bean
	@Order(-1)
	PropertySource dispacherPropertySource() throws IOException {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		// profile null is default
		PropertySource propertySource =
				loader.load("dispachers", new ClassPathResource("dispatch.yml"),null);
		env.getPropertySources().addLast(propertySource);
		return propertySource;
	}

	@Bean
	@Order(-1)
	PropertySource consumerPropertySource() throws IOException {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		PropertySource propertySource =
				loader.load("consumers", new ClassPathResource("consumer.yml"),null);
		env.getPropertySources().addLast(propertySource);
		return propertySource;
	}

	@Bean
	@Order(0)
	DispatcherConfig dispatcherConfig(){
		return new DispatcherConfig();
	}

	@Bean
	@Order(0)
	LogicConsumerConfig logicConsumerConfig(){
		return new LogicConsumerConfig();
	}

	@Bean
	@Order(1)
	DispatcherRouter dispatcherRouter(){
		return new DispatcherRouter(dispatcherConfig(), threadPoolProfile());
	}

	@Bean
	@Order(1)
	LogicRouter logicRouter(){
		return new LogicRouter(logicConsumerConfig(), threadPoolProfile());
	}
}