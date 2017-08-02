package com.coderjerry.eds.dispatch.config;

import com.coderjerry.eds.core.EdsConstants;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jmx.export.annotation.ManagedResource;

@ConfigurationProperties(prefix="consumers")
@ManagedResource
public class LogicConsumerConfig {

  private static final Logger LOG = LoggerFactory.getLogger(LogicConsumerConfig.class);

  @Value("#{systemProperties['" + EdsConstants.EDS_ENABLED_CONSUMER_GROUP + "']}")
  private String enabledConsumerGroup = null;
  @Value("#{systemProperties['" + EdsConstants.EDS_ENABLED_CONSUMER_NAME + "']}")
  private String enabledConsumerName = null;

  private Set<String> enabledConsumerGroupSet;
  private Set<String> enabledConsumerNameSet;

  public Set<String> getEnabledConsumerGroupSet() {
    if (enabledConsumerGroupSet != null) {
      return enabledConsumerGroupSet;
    }
    enabledConsumerGroupSet = new HashSet<>();
    if (enabledConsumerGroup != null) {
      for (String group : enabledConsumerGroup.split(EdsConstants.EDS_PROPERTIES_SPLIT_REGEX)) {
        enabledConsumerGroupSet.add(group.trim());
      }
    }
    if (enabledConsumerGroupSet.size() > 0) {
      LOG.info("启用consumer group : " + enabledConsumerGroupSet);
    }
    return enabledConsumerGroupSet;

  }

  public Set<String> getEnabledConsumerNameSet() {
    if (enabledConsumerNameSet != null) {
      return enabledConsumerNameSet;
    }
    enabledConsumerNameSet = new HashSet<>();
    if (enabledConsumerName != null) {
      for (String name : enabledConsumerName.split(EdsConstants.EDS_PROPERTIES_SPLIT_REGEX)) {
        enabledConsumerNameSet.add(name.trim());
      }
    }
    if (enabledConsumerNameSet.size() > 0) {
      LOG.info("启用consumer name : " + enabledConsumerNameSet);
    }
    return enabledConsumerNameSet;
  }

  private List<Element> list = new ArrayList<>();

  public List<Element> getList() {
    return list;
  }

  public void setList(List<Element> list) {
    this.list = list;
  }

  public static class Element {
    private String name;
    private String group;
    private String from;
    private String fromProtocol;
    private int concurrencyMin = 1; // 线程池最小数量
    private int concurrencyMax = 1; // 线程池最大数量
    private String options; // 自定义 如activemq:queue:foo?concurrentConsumers=5 中的
                            // concurrentConsumers=5

    public enum Type {
      custom, internal // 内部使用 有_eds_前缀
    }

    private Type type = Type.internal;

    private String processor;

    public Type getType() {
      return type;
    }

    public void setType(Type type) {
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getFrom() {
      return from;
    }

    public int getConcurrencyMin() {
      return concurrencyMin;
    }

    public void setConcurrencyMin(int concurrencyMin) {
      this.concurrencyMin = concurrencyMin;
    }

    public int getConcurrencyMax() {
      return concurrencyMax;
    }

    public void setConcurrencyMax(int concurrencyMax) {
      this.concurrencyMax = concurrencyMax;
    }

    public void setFrom(String from) {
      this.from = from;
    }

    public String getFromProtocol() {
      return fromProtocol;
    }

    public void setFromProtocol(String fromProtocol) {
      this.fromProtocol = fromProtocol;
    }

    public String getProcessor() {
      return processor;
    }

    public void setProcessor(String processor) {
      this.processor = processor;
    }

    public String getGroup() {
      return group;
    }

    public void setGroup(String group) {
      this.group = group;
    }
    
    public String getOptions() {
      return options;
    }

    public void setOptions(String options) {
      this.options = options;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

  }

}
