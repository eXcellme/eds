package com.coderjerry.eds.consumer.util;

import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import com.coderjerry.eds.core.exception.EdsException;

/**    
 * Dynamically load configuration classes
 */
public class DynamicConfiguration {
  
  private static final PropertiesConfiguration _config ;
  private static final int REFRESH_DELAY = 5 * 1000; // ms
  private static final String FILE_PATH = "dynamic_conf.properties";
  
  static{
    try {
      _config = new PropertiesConfiguration(FILE_PATH);
      FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
      strategy.setRefreshDelay(REFRESH_DELAY);
      AbstractConfiguration.setDefaultListDelimiter(';');
      _config.setReloadingStrategy(strategy);
      _config.setEncoding("UTF-8");
      _config.reload();
    } catch (ConfigurationException e) {
      throw new EdsException("DynamicConfiguration initialized error - loading data failed ",e);
    }
  }
  
  public static String getString(String key){
    return _config.getString(key);
  }
  
  public static String getString(String key,String defaultValue){
    return _config.getString(key,defaultValue);
  }
  
  public static int getInt(String key){
    return _config.getInt(key);
  }
  
  public static int getInt(String key,int defaultValue){
    return _config.getInt(key,defaultValue);
  }
  
  public static boolean getBoolean(String key){
    return _config.getBoolean(key);
  }
  
  public static boolean getBoolean(String key,boolean defaultValue){
    return _config.getBoolean(key,defaultValue);
  }
  
  public static List<?> getList(String key){
	  return _config.getList(key);
  }
}
