package com.coderjerry.eds.client.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;

public class RingBufferEventTranslatorDemo implements EventTranslatorOneArg<RingBufferEventDemo,RingBufferEventDemo>{

	public static final RingBufferEventTranslatorDemo instance = new RingBufferEventTranslatorDemo(); 
	public RingBufferEventTranslatorDemo(){
	}
//	private EventPublisher publisher ; 
//	private String name ;
//	private Object data ; 
	

  @Override
  public void translateTo(RingBufferEventDemo event, long sequence, RingBufferEventDemo data) {
    event.setPublisher(data.getPublisher());
    event.setData(data.getData());
    event.setName(data.getName());
    
  }
	
//	private void clear(){
//		setValues(null, null, null);
//	}
	
//	public void setValues(EventPublisher publisher,String name ,Object data){
////		if(publisher == null || name == null || data == null){
////		}else{
////			System.out.println("setValue："+publisher + ",name:"+name+",data:"+data);
////		}
//		this.publisher = publisher ;
//		this.name = name ;
//		this.data = data ;
//	}
	
}
