package com.coderjerry.eds.client.disruptor;

import com.coderjerry.eds.core.EventPublisher;
import com.lmax.disruptor.EventTranslator;

public class RingBufferEventTranslatorOriDemo implements EventTranslator<RingBufferEventDemo>{

	public static final RingBufferEventTranslatorDemo instance = new RingBufferEventTranslatorDemo(); 
	public RingBufferEventTranslatorOriDemo(){
	}
	private EventPublisher publisher ; 
	private String name ;
	private Object data ; 
	
	@Override
	public void translateTo(RingBufferEventDemo event, long sequence) {
		event.setPublisher(publisher);
		event.setData(data);
		event.setName(name);
	}
	
	private void clear(){
		setValues(null, null, null);
	}

	private void setValues(EventPublisher publisher,String name ,Object data){
//		if(publisher == null || name == null || data == null){
//		}else{
//			System.out.println("setValue："+publisher + ",name:"+name+",data:"+data);
//		}
		this.publisher = publisher ;
		this.name = name ;
		this.data = data ;
	}
	
}
