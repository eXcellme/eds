package com.coderjerry.eds.client;

import com.lmax.disruptor.EventTranslatorOneArg;

class EdsRingBufferEventTranslator implements EventTranslatorOneArg<EdsRingBufferEvent,EdsRingBufferEvent>{

	public static final EdsRingBufferEventTranslator instance = new EdsRingBufferEventTranslator();
	private EdsRingBufferEventTranslator(){
	}
	
	@Override
	public void translateTo(EdsRingBufferEvent event, long sequence,EdsRingBufferEvent originData) {
		event.setPublisher(originData.getPublisher());
		event.setData(originData.getData());
		event.setName(originData.getName());
	}
	
}
