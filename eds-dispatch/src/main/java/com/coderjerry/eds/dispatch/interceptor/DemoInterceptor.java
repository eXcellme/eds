package com.coderjerry.eds.dispatch.interceptor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;

import com.coderjerry.eds.dispatch.config.EdsCamelConfig;


public class DemoInterceptor implements InterceptStrategy{

	private EdsCamelConfig edsCamelConfig;
	
	public EdsCamelConfig getEdsCamelConfig() {
		return edsCamelConfig;
	}

	public void setEdsCamelConfig(EdsCamelConfig edsCamelConfig) {
		this.edsCamelConfig = edsCamelConfig;
	}

	@Override
	public Processor wrapProcessorInInterceptors(CamelContext context,
			final ProcessorDefinition<?> definition, final Processor target,
			final Processor nextTarget) throws Exception {
		return new DelegateAsyncProcessor(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
//				if(!camelConfig.isRunning()){
//					System.err.println("系统将关闭，不在处理任务");
//					return ;
//				}
				System.out.println("defainition :"+definition);
				System.out.println("nextTarget :"+nextTarget);
				target.process(exchange);
			}
		});
	}

}
