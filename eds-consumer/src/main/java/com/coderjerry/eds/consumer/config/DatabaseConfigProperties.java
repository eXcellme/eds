package com.coderjerry.eds.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:datasource.properties")
@ConfigurationProperties()
public class DatabaseConfigProperties {
    private String url ,
                   username,
                   password,
                   validationQuery = "SELECT 'x'";
    private int initialSize,
                minIdle ,
                maxActive ,
                maxWait ,
                maxPoolPreparedStatementPerConnectionSize,
                removeAbandonedTimeout;
    
    private long timeBetweenEvictionRunsMillis,
                 minEvictableIdleTimeMillis,
                 timeBetweenLogStatsMillis,
                 phyTimeoutMillis,
                 maxEvictableIdleTimeMillis;
    
    private boolean testWhileIdle,
                    testOnBorrow,
                    testOnReturn,
                    poolPreparedStatements,
                    removeAbandoned,
                    logAbandoned ;
                    
                    
                 
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getValidationQuery() {
        return validationQuery;
    }
    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }
    public int getInitialSize() {
        return initialSize;
    }
    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }
    public int getMinIdle() {
        return minIdle;
    }
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }
    public int getMaxActive() {
        return maxActive;
    }
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }
    public int getMaxWait() {
        return maxWait;
    }
    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }
    public int getMaxPoolPreparedStatementPerConnectionSize() {
      return maxPoolPreparedStatementPerConnectionSize;
    }
    public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
      this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }
    public int getRemoveAbandonedTimeout() {
      return removeAbandonedTimeout;
    }
    public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
      this.removeAbandonedTimeout = removeAbandonedTimeout;
    }
    public long getTimeBetweenEvictionRunsMillis() {
      return timeBetweenEvictionRunsMillis;
    }
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
      this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }
    public long getMinEvictableIdleTimeMillis() {
      return minEvictableIdleTimeMillis;
    }
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
      this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public long getTimeBetweenLogStatsMillis() {
      return timeBetweenLogStatsMillis;
    }
    public void setTimeBetweenLogStatsMillis(long timeBetweenLogStatsMillis) {
      this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
    }
    public long getPhyTimeoutMillis() {
      return phyTimeoutMillis;
    }
    public void setPhyTimeoutMillis(long phyTimeoutMillis) {
      this.phyTimeoutMillis = phyTimeoutMillis;
    }
    public long getMaxEvictableIdleTimeMillis() {
      return maxEvictableIdleTimeMillis;
    }
    public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
      this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
    }
    public boolean isTestWhileIdle() {
      return testWhileIdle;
    }
    public void setTestWhileIdle(boolean testWhileIdle) {
      this.testWhileIdle = testWhileIdle;
    }
    public boolean isTestOnBorrow() {
      return testOnBorrow;
    }
    public void setTestOnBorrow(boolean testOnBorrow) {
      this.testOnBorrow = testOnBorrow;
    }
    public boolean isTestOnReturn() {
      return testOnReturn;
    }
    public void setTestOnReturn(boolean testOnReturn) {
      this.testOnReturn = testOnReturn;
    }
    public boolean isPoolPreparedStatements() {
      return poolPreparedStatements;
    }
    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
      this.poolPreparedStatements = poolPreparedStatements;
    }
    public boolean isRemoveAbandoned() {
      return removeAbandoned;
    }
    public void setRemoveAbandoned(boolean removeAbandoned) {
      this.removeAbandoned = removeAbandoned;
    }
    public boolean isLogAbandoned() {
      return logAbandoned;
    }
    public void setLogAbandoned(boolean logAbandoned) {
      this.logAbandoned = logAbandoned;
    }
    
                
}
