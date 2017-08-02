package com.coderjerry.eds.consumer.aop;


import com.alibaba.fastjson.JSON;
import com.coderjerry.eds.consumer.model.LogBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LogicConsumerAspect {

  private static final Logger logger = LogManager.getLogger(LogicConsumerAspect.class);

  @Around("execution (* com.coderjerry.eds.dispatch.logic.AbstractLogicConsumer+.consume(..))")
  public Object log(ProceedingJoinPoint jp) throws Throwable {
    Object result = null;
    String callMethod = jp.getSignature().getDeclaringType().getSimpleName() + "." + jp.getSignature().getName();
    LogBean log = new LogBean();
    try {
      long start = System.currentTimeMillis();
      result = jp.proceed();
      long end = System.currentTimeMillis();
      log.ct = (end - start);
      log.k = callMethod;
      log.p = jp.getArgs();
      log.rq = ThreadContext.get("requestId");
      log.t = start;
      log.c = LogBean.CODE_SUCCESS;
      logger.debug(JSON.toJSONString(log));
      return result;
    } catch (Throwable e) {
      logger.error("error in logic consumer aspect ", e);
      log.ex = e.getMessage();
      log.c = LogBean.CODE_FAILURE;
      logger.debug(JSON.toJSONString(log));
      throw e;
    }

  }
}
