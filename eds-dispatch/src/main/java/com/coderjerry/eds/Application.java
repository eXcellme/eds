package com.coderjerry.eds;

import java.util.Arrays;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
//@EnableConfigurationProperties({DispatcherConfig.class,LogicConsumerConfig.class})
@RestController
public class Application {
	
	public static void main(String[] args) {
		new SpringApplicationBuilder()
				.listeners(new EnvironmentPrepareListener())
				.sources(Application.class)
				.bannerMode(Mode.CONSOLE)
				.build()
				.run(args);
	}

	static class EnvironmentPrepareListener implements
			ApplicationListener<ApplicationEnvironmentPreparedEvent> {

		@Override
		public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
			// 设置动态加载文件要使用的active.profile系统变量
			ConfigurableEnvironment env = event.getEnvironment();
			if(env != null){
				System.out.println("start eds Application , active profiles : "
						+ Arrays.toString(env.getActiveProfiles()));
				String[] activeProfiles = env.getActiveProfiles();
				if(activeProfiles != null && activeProfiles.length > 0){
					String active = activeProfiles[0];
					System.setProperty("spring.profiles.active",active);
					System.out.println("eds system property [spring.profiles.active] variable : "+active);
				}
			}
		}
	}

}
