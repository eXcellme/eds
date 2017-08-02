package com.coderjerry.eds.consumer.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.coderjerry.eds.consumer.aop.DaoAspect;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@MapperScan(basePackages="com.coderjerry.eds.consumer.mybatis.mapper.*")
public class DatabaseConfig {
    @Autowired
    private DatabaseConfigProperties config ;
    
    @Bean(initMethod="init",destroyMethod="close")
    public DataSource dataSource(){
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(config.getUrl());
        ds.setUsername(config.getUsername());
        ds.setPassword(config.getPassword());
        ds.setInitialSize(ds.getInitialSize());
        ds.setMinIdle(config.getMinIdle());
        ds.setMaxActive(config.getMaxActive());
        ds.setMaxWait(config.getMaxWait());
        ds.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis()); // 关闭检测间隔 毫秒
        ds.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis()); // 一个连接的最小生存时间
        ds.setValidationQuery("SELECT 'x'");
        ds.setTestWhileIdle(config.isTestWhileIdle());
        ds.setTestOnBorrow(config.isTestOnBorrow());
        ds.setTestOnReturn(config.isTestOnReturn());
        ds.setPoolPreparedStatements(config.isPoolPreparedStatements());
        ds.setMaxPoolPreparedStatementPerConnectionSize(config.getMaxPoolPreparedStatementPerConnectionSize());
        ds.setRemoveAbandoned(config.isRemoveAbandoned());
        ds.setRemoveAbandonedTimeout(config.getRemoveAbandonedTimeout());
        ds.setLogAbandoned(config.isLogAbandoned());
        ds.setTimeBetweenLogStatsMillis(config.getTimeBetweenLogStatsMillis());
        ds.setPhyTimeoutMillis(config.getPhyTimeoutMillis());
        ds.setMaxEvictableIdleTimeMillis(config.getMaxEvictableIdleTimeMillis());
        ds.setDbType("mysql");
        try {
            ds.setFilters("stat,slf4j");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds ; 
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception{
        final SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }
    
    @Bean(name="transactionManager")
    PlatformTransactionManager transactionManager(){
        return new DataSourceTransactionManager(dataSource());
    }
    
    @Bean
    DaoAspect daoAspect(){
      return new DaoAspect();
    }

}
