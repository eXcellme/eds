package com.coderjerry.eds.dispatch.messageconvertor;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.coderjerry.eds.core.Event;
import com.coderjerry.eds.core.EventConverter;
import com.coderjerry.eds.core.exception.EdsException;

public class EventMessageConvertor implements MessageConverter{

	@Override
	public Object fromMessage(Message message) throws JMSException,
			MessageConversionException {
		if(message instanceof MapMessage){
			return EventConverter.convertToEvent((MapMessage) message );
		}
		throw new EdsException("eds message to event conversion exception : " + message);
	}

	@Override
	public Message toMessage(Object object, Session session) throws JMSException,
			MessageConversionException {
		if(object instanceof Event){
			MapMessage mapMessage = session.createMapMessage();
//			Event ev = new Event();
			return EventConverter.convertToMessage(mapMessage, (Event)object);
		}
		throw new EdsException("eds event to message conversion exception : " + ToStringBuilder.reflectionToString(object));
	}

}
