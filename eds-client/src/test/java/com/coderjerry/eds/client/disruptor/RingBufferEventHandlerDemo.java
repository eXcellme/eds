package com.coderjerry.eds.client.disruptor;

import com.coderjerry.eds.core.Event;
import com.coderjerry.eds.core.EventPublisher;
import com.coderjerry.eds.core.exception.EdsException;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;
import com.lmax.disruptor.EventHandler;

public class RingBufferEventHandlerDemo implements EventHandler<RingBufferEventDemo>{

	private static final Logger LOG = LoggerFactory.getLogger(RingBufferEventHandlerDemo.class);
	@Override
	public void onEvent(RingBufferEventDemo event, long sequence, boolean endOfBatch) throws Exception {
		if(event == null || event.getName() == null || event.getPublisher() == null){
			LOG.error("不能传输event为空的信息! event:"+event+",sequence:"+sequence+",endOfbatch:"+endOfBatch);
			throw new EdsException("不能传输event为空的信息!");
		}
		LOG.trace("eds pub 2 onevent -["+event.getName()+","+event.getData()+"]");
		EventPublisher pub = event.getPublisher();
		try{
			// 实际发布
			pub.publish(new Event(event.getName(),event.getData()));
		}catch(Exception e){
			// 由实际发送者重试发送
			LOG.error("disruptor事件处理者调用消息发布者实际发布消息失败："+event,e);
		}finally{
			event.clear();
		}
	}

}
