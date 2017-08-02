package com.coderjerry.eds.core;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class EventConverter {
	public static MapMessage convertToMessage(MapMessage mapMessage,Event event) throws JMSException{
		String name = event.getName();
		Object data = event.getData();
		// 如果已经是字符串，则不动
		String dataStr = null;
		if(!(data instanceof String)){
		     dataStr = JSON.toJSONString(data,SerializerFeature.WriteClassName);
		}else{
		    dataStr = data.toString();
		}
		mapMessage.setObject("name", name);
		mapMessage.setObject("data", dataStr);
		return mapMessage;
	}
	
	/**  
	* extract message from mapMessage and convert to Event
	* @throws JMSException if jms exception occured
	*/
	public static Event convertToEvent(MapMessage mapMessage) throws JMSException{
		Object data = mapMessage.getObject("data");
		Object name = mapMessage.getObject("name");
		return new Event(String.valueOf(name),data);
	}
	
}
