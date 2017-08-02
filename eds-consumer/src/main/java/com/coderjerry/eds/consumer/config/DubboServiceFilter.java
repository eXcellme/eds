package com.coderjerry.eds.consumer.config;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.fastjson.JSON;
import com.coderjerry.eds.consumer.model.LogBean;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dubbo RPC调用的filter，用于记录RPC调用时间等信息
 *
 */
@Activate(group = Constants.CONSUMER)
public class DubboServiceFilter implements Filter {

  private static final Logger dubboLogger = LogManager.getLogger("DUBBO-PERF");
  private static final Logger logger = LogManager.getLogger(DubboServiceFilter.class);

  private static final String REQUEST_ID = "requestId";

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    long start = System.currentTimeMillis();
    String classDotMethod = (invocation.getInvoker().getInterface().getName().concat(".").concat(invocation
        .getMethodName()));

    try {
      Result r = invoker.invoke(invocation);
      long end = System.currentTimeMillis();
      LogBean log = new LogBean();
      log.ct = (end - start);
      log.k = (invocation.getInvoker().getInterface().getName() + "." + invocation.getMethodName());
      // provider
      log.ext = (invoker.getUrl().getAddress());
      log.t = System.currentTimeMillis();
      log.p = Arrays.toString(invocation.getArguments());
      log.r = (r != null ? r.getValue() : null);
      log.tp = LogBean.TYPE_DUBBO_PERF; // DUBBO_PERF
      dubboLogger.info(JSON.toJSONString(log));
      return r;
    } catch (Exception e) {
      logger.error("DubboServiceFilter.invoke error args : invoker={},invocation={},e={} ", invoker, invocation, e);
      throw e;
    }
  }

}
