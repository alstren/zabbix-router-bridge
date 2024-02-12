package ru.krsmon.zabbixrouterbridge.config;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static feign.Logger.Level.BASIC;
import static feign.Logger.Level.FULL;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import feign.Capability;
import feign.Logger;
import feign.Retryer;
import feign.micrometer.MicrometerCapability;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = "ru.krsmon.zabbixrouterbridge.external")
class FeignConfig {

  @Bean
  Capability capability(@NonNull MeterRegistry registry) {
    return new MicrometerCapability(registry);
  }

  @Bean
  TimedAspect timedAspect(@NonNull MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  @Bean
  CountedAspect countedAspect(@NonNull MeterRegistry registry) {
    return new CountedAspect(registry);
  }

  @Bean
  Logger.Level feignLoggerLeve() {
    return log.isDebugEnabled() ? FULL : BASIC;
  }

  @Bean
  Retryer retryer() {
    return new Retryer.Default(SECONDS.toMillis(500L), SECONDS.toMillis(300L), 3);
  }

  @Bean
  @Primary
  ObjectMapper objectMapper() {
    return new ObjectMapper()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JSR310Module());
  }

}
