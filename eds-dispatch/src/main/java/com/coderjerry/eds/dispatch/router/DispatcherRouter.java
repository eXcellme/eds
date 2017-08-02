package com.coderjerry.eds.dispatch.router;

import com.coderjerry.eds.core.EdsConstants;
import com.coderjerry.eds.dispatch.config.DispatcherConfig;
import com.coderjerry.eds.dispatch.config.DispatcherConfig.Element;
import com.coderjerry.eds.dispatch.config.LoggerNames;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.util.CamelLogger;

public class DispatcherRouter extends RouteBuilder{

//	private static final Logger LOG = LoggerFactory.getLogger(DispatcherRouter.class);

	private DispatcherConfig dispatcherConfig ;

	private ThreadPoolProfile threadPoolProfile;

	public DispatcherRouter(DispatcherConfig dispatcherConfig, ThreadPoolProfile threadPoolProfile) {
		this.dispatcherConfig = dispatcherConfig;
		this.threadPoolProfile = threadPoolProfile;
	}

	@Override
	public void configure() throws Exception {
//		camelContext.setTracing(true);
	  onException(Exception.class)
  	  .handled(false)
  	  .bean("dispatcherHelper","error");
		errorHandler(
				defaultErrorHandler()
				.maximumRedeliveries(6) // 消费6次不成功进入死信队列 -1 永远不进入死信队列
				.logger(new CamelLogger(LoggerNames.DISPATCH_ERROR_LOGGER, LoggingLevel.ERROR))
				.retryAttemptedLogLevel(LoggingLevel.WARN)
				.backOffMultiplier(1.5)  // 指数退避
				.useExponentialBackOff()
				);
		Set<String> enabledGroupSet = dispatcherConfig.getEnabledDispatchGroupSet();
		Set<String> enabledNameSet = dispatcherConfig.getEnabledDispatchNameSet();
		// 控制开关 shutdown hook
		for(final Element ele : dispatcherConfig.getList()){
		  String name = ele.getName();
		  if(enabledNameSet.size() > 0 && !enabledNameSet.contains(name)){
		    continue;
		  }
		  String group = ele.getGroup();
		  if(enabledGroupSet.size() > 0 && !enabledGroupSet.contains(group)){
		    continue;
		  }
			String from = ele.getFromProtocol() + ':' + EdsConstants.QUEUE_PREFIX + ele.getFrom();
			List<String> to = ele.getTo();
			to = Lists.transform(to,new Function<String,String>() {
				@Override
				public String apply(String input) {
					return ele.getToProtocol() + ':' +EdsConstants.QUEUE_PREFIX + input;
				}
			});
			int poolSize = ele.getConcurrencyMin();
			int maxPoolSize = ele.getConcurrencyMax();
			String[] tos = new String[to.size()];
			tos = to.toArray(tos);
			// 定义dispatch routes
//			RouteDefinition rd = 
			from(from)
			  .routeId("dispath_"+name)
			  .id(name)
			  .transacted()
			  .threads(poolSize, maxPoolSize, threadPoolProfile.getId())
			  .bean("dispatcherHelper","before")
  			  .log(LoggingLevel.INFO, LoggerNames.DISPATCH_LOGGER, "${headers}|${body}")
  			  .multicast()
   			  .to(tos)
 			  .bean("dispatcherHelper","success");
		}
	}

}
