package com.coderjerry.eds.dispatch.logic;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.gearman.GearmanConstants;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.alibaba.fastjson.JSON;
import com.coderjerry.eds.core.Event;
import com.coderjerry.eds.core.exception.EdsException;
import com.coderjerry.eds.dispatch.config.LoggerNames;
import com.coderjerry.eds.dispatch.util.RequestIdManager;

/**
 * Business logic consumer template class
 *
 * @author baofan.li
 */
@ManagedResource
public abstract class AbstractLogicConsumer<T> implements Processor {

  // for framework
  private static final Logger CONSUME_LOG = LogManager.getLogger(LoggerNames.CONSUME_LOGGER);
  // default to debug log
  private final Logger LOG = LogManager.getLogger(getClass());

  @Override
  public void process(Exchange exchange) throws Exception {
    try {
      String requestId = exchange.getProperty(RequestIdManager.REQUEST_ID, String.class);
      ThreadContext.put(RequestIdManager.REQUEST_ID, requestId);
      doProcess(exchange);
    } finally {
      ThreadContext.remove(RequestIdManager.REQUEST_ID);
    }
  }

  private void doProcess(Exchange exchange) throws Exception {
    Object body = exchange.getIn().getBody();
    if (body != null) {
      Event event = null;
      if (body instanceof Event) { // jms
        event = (Event) body;
      } else if (exchange.getIn().getHeader(KafkaConstants.TOPIC) != null) { // from kafka
        if (body instanceof byte[]) {
          try {
            String s = new String((byte[]) body);
            consume((T) s);
            return;
          } catch (Exception e) {
            CONSUME_LOG.error("eds consumer - kafka message conversion exception, only support String ", e);
            throw e;
          }
        }
      } else if (exchange.getIn().getHeader(GearmanConstants.FUNCTION_NAME)
          != null) { // from gearman
        try {
          consume((T) String.valueOf(body));
        } catch (Exception e) {
          CONSUME_LOG.error(
              "eds consumer - gearman conversion exception, Currently only support String generic "
                  + exchange);
          throw e;
        }
        return;
      } else {
        throw new EdsException("eds consumer - event conversion exception " + exchange);
      }
      if(event == null){
        return ;
      }
      Object arg = event.getData();
      if (arg != null) {
        try {
          if (arg instanceof String && ((String) arg).indexOf("type") > 0) {
            String str = String.valueOf(arg);
            // Messages forwarded by camel will become strings, special symbols such as "(double quotes) will be escaped.
            if (str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
              str = str.substring(1, str.length() - 1).replace("\\", "");
            }
            CONSUME_LOG.debug("eds consumer - received message(before process)：{}", str);
            @SuppressWarnings("unchecked")
            T t = (T) JSON.parse(str);
            consume(t);
          } else {
            @SuppressWarnings("unchecked")
            T t = (T) arg;
            consume(t);
          }
        } catch (ClassCastException e) {
          CONSUME_LOG.error("eds consumer - parameter type conversion exception", e);
        } catch (RuntimeException e) {
          LOG.error("eds consumer - consume exception ", e);
        }
      }
    }
  }

  protected abstract void consume(T obj) throws Exception;

}
