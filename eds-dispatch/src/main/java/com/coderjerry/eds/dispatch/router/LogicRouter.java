package com.coderjerry.eds.dispatch.router;

import com.coderjerry.eds.core.EdsConstants;
import com.coderjerry.eds.dispatch.config.LoggerNames;
import com.coderjerry.eds.dispatch.config.LogicConsumerConfig;
import com.coderjerry.eds.dispatch.config.LogicConsumerConfig.Element;
import com.coderjerry.eds.dispatch.config.LogicConsumerConfig.Element.Type;
import java.util.Set;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.util.CamelLogger;
import org.springframework.beans.factory.annotation.Autowired;

public class LogicRouter extends RouteBuilder{

	private LogicConsumerConfig logicConsumerConfig;

	private ThreadPoolProfile threadPoolProfile ;

	@Autowired
	public LogicRouter(LogicConsumerConfig logicConsumerConfig, ThreadPoolProfile threadPoolProfile) {
		this.logicConsumerConfig = logicConsumerConfig;
		this.threadPoolProfile = threadPoolProfile;
	}


	@Override
	public void configure() throws Exception {
		// context error handler
	  onException(Exception.class)
  	  .handled(false)
  	  .bean("consumerHelper","error");
		errorHandler(
				defaultErrorHandler()
//				.exceptionPolicyStrategy(exceptionPolicyStrategy)
//				.onPrepareFailure(processor)
				.maximumRedeliveries(10) // x 次后将进入activemq 默认的死信队列，-1为不进入
				.logger(new CamelLogger(LoggerNames.CONSUME_ERROR_LOGGER, LoggingLevel.ERROR))
				.retryAttemptedLogLevel(LoggingLevel.WARN)
				.allowRedeliveryWhileStopping(false) // 关闭时无需重发 业务逻辑无需重试，因为依赖的服务如dubbo可能已经关闭
				.backOffMultiplier(1.5)  // 指数退避
				.useExponentialBackOff()
				) ;
		
		Set<String> enabledGroupSet = logicConsumerConfig.getEnabledConsumerGroupSet();
		Set<String> enabledNameSet = logicConsumerConfig.getEnabledConsumerNameSet();
		for(Element ele : logicConsumerConfig.getList()){
		  String name = ele.getName();
      if(enabledNameSet.size() > 0 && !enabledNameSet.contains(name)){
        continue;
      }
      String group = ele.getGroup();
      if(enabledGroupSet.size() > 0 && !enabledGroupSet.contains(group)){
        continue;
      }
		  String prefix = ele.getType().compareTo(Type.internal) == 0 ? EdsConstants.QUEUE_PREFIX : "";
			String from = ele.getFromProtocol() + ':' + prefix + ele.getFrom();
			String proc = ele.getProcessor();
			int poolSize = ele.getConcurrencyMin() ;
			int maxPoolSize = ele.getConcurrencyMax();
			String options = ele.getOptions();
			if(options != null){
			  if(options.startsWith("?")){
			    from += options;
			  }else{
			    from += "?" + options;
			  }
			}
			from(from)
			  .id(ele.getName())
			  .routeId("logic_"+ele.getName())
			  .transacted()
			  .threads(poolSize, maxPoolSize,threadPoolProfile.getId())
  			  .bean("consumerHelper", "before")
  			  .log(LoggingLevel.INFO,LoggerNames.CONSUME_LOGGER,"${headers}|${body}")
				// 这里设置了线程池，上面的requestId要重新设置
//				.doTry()  // 使用doTry doCatch doFinaly 将不能使用默认的error handler
				  .process(proc)
//				  .bean("consumerHelper","error")
//				.doCatch(Exception.class)
				  .bean("consumerHelper","success")
//				.end()  
				;
		}
	}

}
