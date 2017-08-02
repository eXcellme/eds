package com.coderjerry.eds.client;

import com.coderjerry.eds.core.Event;
import com.coderjerry.eds.core.EventPublisher;
import com.coderjerry.eds.core.exception.EdsException;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;
import com.lmax.disruptor.EventHandler;

class EdsRingBufferEventHandler implements EventHandler<EdsRingBufferEvent>{

	private static final Logger LOG = LoggerFactory.getLogger(EdsRingBufferEventHandler.class);
	@Override
	public void onEvent(EdsRingBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
		System.out.println("onEvent==============="+sequence+", "+event);
		if(event == null || event.getName() == null || event.getPublisher() == null){
			LOG.error("must be not null! event:"+event+", publisher:"+event.getPublisher());
			throw new EdsException("event or real publisher must be not null!");
		}
		LOG.debug("eds pub 2 onevent -["+event.getName()+","+event.getData()+"]");
		EventPublisher pub = event.getPublisher();
		// TODO add a thread pool to do this in case of current thread BLOCKING all ?
		try{
			// real publish
			pub.publish(new Event(event.getName(),event.getData()));
		}catch(Exception e){
			// handle error in real publisher
			LOG.error("disruptor event handler call real publisher to publish event , error: " + event,e);
		}finally{
			event.clear();
		}
	}

}
