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

@ConfigurationProperties("dispatchers")
@ManagedResource
public class DispatcherConfig {

  private static final Logger LOG = LoggerFactory.getLogger(DispatcherConfig.class);
  @Value("#{systemProperties['"+EdsConstants.EDS_ENABLED_DISPATCH_NAME+"']}")
  private String enabledDispatchName = null; // 启用的dispatch名称
  @Value("#{systemProperties['"+EdsConstants.EDS_ENABLED_DISPATCH_GROUP+"']}")
  private String enabledDispatchGroup = null; // 启用的dispatch组名
  
  private Set<String> enabledDispatchGroupSet ;
  private Set<String> enabledDispatchNameSet ;
  
  public Set<String> getEnabledDispatchGroupSet() {
    if(enabledDispatchGroupSet != null){
      return enabledDispatchGroupSet;
    }
    enabledDispatchGroupSet = new HashSet<>();
    if(enabledDispatchGroup != null){
      for(String group : enabledDispatchGroup.split(EdsConstants.EDS_PROPERTIES_SPLIT_REGEX)){
        enabledDispatchGroupSet.add(group.trim());
      }
    }
    if(enabledDispatchGroupSet.size() > 0){
      LOG.info("启用dispatch group : "+enabledDispatchGroupSet);
    }
    return enabledDispatchGroupSet;
    
  }

  public Set<String> getEnabledDispatchNameSet() {
    if(enabledDispatchNameSet != null){
      return enabledDispatchNameSet;
    }
    enabledDispatchNameSet = new HashSet<>();
    if(enabledDispatchName != null){
      for(String name : enabledDispatchName.split(EdsConstants.EDS_PROPERTIES_SPLIT_REGEX)){
        enabledDispatchNameSet.add(name.trim());
      }
    }
    if(enabledDispatchNameSet.size() > 0){
      LOG.info("启用dispatch name : "+enabledDispatchNameSet);
    }
    return enabledDispatchNameSet;
  }

  private List<Element> list = new ArrayList<>();

	public List<Element> getList() {
		return list;
	}

	public void setList(List<Element> list) {
		this.list = list;
	}
	
	public static class Element{
		private String name ;
		private String from ;
		private String fromProtocol ;
		private String toProtocol ;
		private String group ;
		private int concurrencyMin = 1 ; //  线程池最小数量
    private int concurrencyMax = 1; //  线程池最大数量
		private List<String> to = new ArrayList<>();
		
		public String getFromProtocol() {
			return fromProtocol;
		}
		public void setFromProtocol(String fromProtocol) {
			this.fromProtocol = fromProtocol;
		}
		public String getToProtocol() {
			return toProtocol;
		}
		public void setToProtocol(String toProtocol) {
			this.toProtocol = toProtocol;
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
		public void setFrom(String from) {
			this.from = from;
		}
		public List<String> getTo() {
			return to;
		}
		public void setTo(List<String> to) {
			this.to = to;
		}
		public String getGroup() {
      return group;
    }
    public void setGroup(String group) {
      this.group = group;
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
    @Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
		}

	}

}
