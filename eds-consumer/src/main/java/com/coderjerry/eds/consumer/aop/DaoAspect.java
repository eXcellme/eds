package com.coderjerry.eds.consumer.aop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**    
 * dao performance aspect
 */
@Aspect
public class DaoAspect {

  private static final Logger logger = LogManager.getLogger(DaoAspect.class);

  @Around("execution (public * com.coderjerry.eds.consumer.mybatis.mapper..*Mapper.*(..))")
  public Object logCache(ProceedingJoinPoint jp) throws Throwable {
    Object result = null;
    String callMethod = jp.getSignature().getDeclaringType().getSimpleName() + "." + jp.getSignature().getName();
    long start = System.currentTimeMillis();
    try {
      result = jp.proceed();
      logger.debug("SQL execute {} ,cost {}ms", callMethod, (System.currentTimeMillis() - start));
      return result;
    } catch (Throwable e) {
      logger.error("SQL execute error {} {}",callMethod, e);
      throw e;
    }

  }

}
