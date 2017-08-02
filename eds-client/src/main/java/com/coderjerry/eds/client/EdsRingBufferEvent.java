package com.coderjerry.eds.client;

import com.coderjerry.eds.core.EventPublisher;
import com.lmax.disruptor.EventFactory;

class EdsRingBufferEvent {
	private String name ;
	private Object data ;
	private EventPublisher publisher ; 
	
	public EdsRingBufferEvent(){
	}
	public EdsRingBufferEvent(String name,Object data,EventPublisher realPublisher){
	  this.name = name;
	  this.data = data;
	  this.publisher = realPublisher;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public EventPublisher getPublisher() {
		return publisher;
	}
	public void setPublisher(EventPublisher publisher) {
		this.publisher = publisher;
	}

	public static final EventFactory<EdsRingBufferEvent> FACTORY = new EventFactory<EdsRingBufferEvent>() {
		@Override
		public EdsRingBufferEvent newInstance() {
			return new EdsRingBufferEvent();
		}
	};
	
	public void clear(){
		this.name = null;
		this.data = null;
		this.publisher = null;
	}
	
	@Override
	public String toString() {
		return "[name:"+name+",data:"+data+",publisher:"+publisher+"]";
	}
}
