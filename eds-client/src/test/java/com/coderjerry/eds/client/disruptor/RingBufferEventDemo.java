package com.coderjerry.eds.client.disruptor;

import com.coderjerry.eds.core.EventPublisher;
import com.lmax.disruptor.EventFactory;

public class RingBufferEventDemo {
	private String name ;
	private Object data ;
	private EventPublisher publisher ; 
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

	public static final EventFactory<RingBufferEventDemo> FACTORY = new EventFactory<RingBufferEventDemo>() {
		@Override
		public RingBufferEventDemo newInstance() {
			return new RingBufferEventDemo();
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
