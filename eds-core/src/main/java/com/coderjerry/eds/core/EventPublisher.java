package com.coderjerry.eds.core;

/**    
 * interface of event publisher
 */
public interface EventPublisher extends LifeCycle{
	
	String DEST_PREFIX = EdsConstants.QUEUE_PREFIX;
	
	void publish(Event event) ;
	
}
