package com.coderjerry.eds.core;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**    
 * event entity
 * @author baofan.li
 *
 */
public class Event implements Serializable{
	
	private static final long serialVersionUID = -1477867390436795402L;
	
	public Event(){
	}
	
	public Event(String name ,Object data){
		this.name = name ;
		this.data = data ;
	}
	
	private String name ;
	
	private Object data ;
	
	private String queue ;
	
	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
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
	
	@Override
	public String toString() {
	    try{
	        return "[name:"+this.name+",data:"+this.data+"]";
	    }catch(Exception e){
	        return JSON.toJSONString(this);
	    }
	}
}
