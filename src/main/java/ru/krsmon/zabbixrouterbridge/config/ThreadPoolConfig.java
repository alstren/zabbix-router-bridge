package ru.krsmon.zabbixrouterbridge.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
class ThreadPoolConfig {

  @Bean
  @Primary
  Executor executor() {
    var executor =  new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(300);
    executor.setMaxPoolSize(1000);
    executor.setQueueCapacity(200);
    executor.setKeepAliveSeconds(50);
    executor.setAllowCoreThreadTimeOut(true);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.initialize();
    return executor;
  }

}
