package com.coderjerry.eds.consumer.model;

import java.io.Serializable;

public class LogBean implements Serializable {
  public static final int TYPE_CACHE = 1;
  public static final int TYPE_AUTO_CACHE_READ = 2;
  public static final int TYPE_AUTO_CACHE_WRITE = 3;
  public static final int TYPE_WEB_API = 4;
  public static final int TYPE_DUBBO_PERF = 5;
  public static final int TYPE_MONGO = 6;
  public static final int TYPE_NOTIFY_CP = 7;
  private static final long serialVersionUID = -4386556478311992885L;
  public static final int CODE_SUCCESS = 1;
  public static final int CODE_FAILURE = 0;
  public long t;
  public int tp;
  public int c;
  public Object m;
  public Object p;
  public Object r;
  public String ex;
  public long ct;
  public String k;
  public String rq;
  public Object ext;
  public Object ext2;

  public LogBean() {
  }
}
