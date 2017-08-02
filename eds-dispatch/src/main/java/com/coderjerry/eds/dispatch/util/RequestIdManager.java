package com.coderjerry.eds.dispatch.util;

import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.ThreadContext;

/**
 * requestId is useful for trace log
 */
public class RequestIdManager {

  public static final String REQUEST_ID = "requestId";
  private static final int SERVICE_ID_TAG_LENGTH = 4;
  private static final int RANDOM_NUM_LENGTH = 10 - SERVICE_ID_TAG_LENGTH;

  private String serviceIdTag; // 扩充至四位，原始三位，如031 -> 0031

  public RequestIdManager(String serviceId) {
    if (serviceId == null) {
      serviceIdTag = "9999";
    }
    if (serviceId.length() > SERVICE_ID_TAG_LENGTH) {
      serviceIdTag = StringUtils
          .substring(serviceId, serviceId.length() - SERVICE_ID_TAG_LENGTH, serviceId.length() - 1);
    } else {
      serviceIdTag = StringUtils.leftPad(serviceId, SERVICE_ID_TAG_LENGTH, '0');
    }
  }


  public void generate(Exchange exchange) {
    String requestId = generateReqId();
    ThreadContext.put(REQUEST_ID, requestId);
    exchange.setProperty(REQUEST_ID, requestId);
  }

  public void clean(Exchange exchange) {
    ThreadContext.remove(REQUEST_ID);
  }

  public static String currentRequestId(Exchange exchange) {
    return exchange == null ? ThreadContext.get(REQUEST_ID)
        : exchange.getProperty(REQUEST_ID, String.class);
  }

  public static String currentRequestId() {
    return ThreadContext.get(REQUEST_ID);
  }

  /**
   * datetime(15) + serviceId(4) + random(6)
   */
  private String generateReqId() {
    return DateFormatUtils.format(new Date(), "yyMMddHHmmssSSS") + serviceIdTag + RandomStringUtils
        .randomNumeric(RANDOM_NUM_LENGTH);
  }

}
