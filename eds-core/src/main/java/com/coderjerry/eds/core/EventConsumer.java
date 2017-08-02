package com.coderjerry.eds.core;

public interface EventConsumer {
	
	String listenTo();
	
	void consume(Event event);
	
	void ack();
	
	void nack();
	
	void register();

	boolean canConsume();
}
