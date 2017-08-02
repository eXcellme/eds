package com.coderjerry.eds.dispatch.util;

import org.apache.camel.Exchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PerformanceLogHelper {
  
  private final Logger logger ;
  private static final String EXCHANGE_PERF_PROPERTY = "_perft_";

  private RequestIdManager requestIdManager;

  public PerformanceLogHelper(RequestIdManager requestIdManager, String loggerName){
    this.requestIdManager = requestIdManager;
    this.logger = LogManager.getLogger(loggerName);
  }

  public void before(Exchange exchange){
    // mdc 
    requestIdManager.generate(exchange);
    // log perf
    long start = System.currentTimeMillis();
    exchange.setProperty(EXCHANGE_PERF_PROPERTY, start);
  }
  
  public void success(Exchange exchange){
    Object obj = exchange.getProperty(EXCHANGE_PERF_PROPERTY);
    long cost = -1;
    if(obj != null){
      cost = System.currentTimeMillis() - (long) obj;
      exchange.removeProperty(EXCHANGE_PERF_PROPERTY);
    }
    logger.debug("success|{}|{}|{}",cost,exchange.getIn().getHeaders(),exchange.getIn().getBody());
    requestIdManager.clean(exchange);
  }
  
  public void error(Exchange exchange){
    Object obj = exchange.getProperty(EXCHANGE_PERF_PROPERTY);
    long cost = -1;
    if(obj != null){
      cost = System.currentTimeMillis() - (long) obj;
      exchange.removeProperty(EXCHANGE_PERF_PROPERTY);
    }
    String ex = "";
    if(exchange.getException() != null){
      ex = exchange.getException().getMessage();
      exchange.removeProperty(EXCHANGE_PERF_PROPERTY);
    }
    logger.debug("error{}|{}|{}|{}",ex,cost,exchange.getIn().getHeaders(),exchange.getIn().getBody());
    requestIdManager.clean(exchange);
  }
}
