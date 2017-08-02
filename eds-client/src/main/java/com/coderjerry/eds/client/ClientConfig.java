package com.coderjerry.eds.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.coderjerry.eds.core.exception.EdsException;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;

/**
 * eds client config
 *
 * @author baofan.li
 */
abstract class ClientConfig {

  private static final Logger LOG = LoggerFactory.getLogger(PublishManager.class);
  private static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";
  private static final String EDS_PROFILES_ACTIVE = "eds.profiles.active";

  static final String PUBLISHER_ATTRIBUTE_CONCURRENT = ".publisher.concurrent";
  static final String PUBLISHER_ATTRIBUTE_IS_DEFAULT = ".publisher.isDefault";

  static final String PUBLISH_MANAGER_ATTRIBUTE_PROCESSORS = ".processors";
  static final String PUBLISH_MANAGER_ATTRIBUTE_RING_BUFFER_SIZE = ".ringBufferSize";

  private static Properties props;

  private static void fromDefaultFile(){
    props = new Properties();
    String envFileTemp = "eds-client{}.properties";
    String envFile = "";
    try {
      // 读取环境设置，优先级 eds-client-{eds.profiles.active}.properties > eds-client-{spring.profiles.active}.properties > eds-client.properties
      // -Dspring.profiles.active=dev -Deds.profiles.active=dev
      String active = System.getProperty(EDS_PROFILES_ACTIVE, "");
      if (active.isEmpty()) {
        active = System.getProperty(SPRING_PROFILES_ACTIVE, "");
      }
      envFile = envFileTemp.replace("{}", active.isEmpty() ? active : "-" + active);

      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(envFile);
      if (in == null) {
        envFile = envFileTemp.replace("{}", "");
        in = Thread.currentThread().getContextClassLoader().getResourceAsStream(envFile);
      }
      props.load(in);
      LOG.info("eds envirourment : " + active + ", eds client will read props from " + envFile);

    } catch (IOException e) {
//      LOG.error("please make sure " + envFile + " is in classpath! ", e);
//      throw new EdsException("eds缺少配置文件 " + envFile);
      LOG.warn(envFile + " not exists , please init ClientConfig manually! ");
    } catch (NullPointerException e) {
      LOG.error("please make sure " + envFile + " is in classpath! ", e);
      throw new EdsException("eds读取配置文件失败 " + envFile);
    }
  }

  static {
    fromDefaultFile();
  }

  public static void fromProperties(Properties properties){
    props = properties;
  }

  public static String getProperty(String key) {
    String value = props.getProperty(key);
    if (value != null && value.contains("${") && value.contains("}")) {
      String[] vars = StringUtils.substringsBetween(value, "${", "}");
      if (vars != null && vars.length > 0) {
        for (String var : vars) {
          String varValue = System.getProperty(var, "");
          value = value.replace("${" + var + "}", varValue);
          LOG.info("eds-client.properties replace var: " + var + " => " + varValue);
        }
      }
    }
    return value;
  }

  public static String getProperty(String key, String defaultValue) {
    String value = props.getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public static boolean getBooleanProperty(String key, boolean defaultValue){
    String value = props.getProperty(key);
    if(value == null){
      return defaultValue;
    }
    return Boolean.valueOf(value);
  }

  public static int getIntProperty(String key, int defaultValue){
    String value = props.getProperty(key);
    if(value == null){
      return defaultValue;
    }
    return Integer.valueOf(value);
  }
}
