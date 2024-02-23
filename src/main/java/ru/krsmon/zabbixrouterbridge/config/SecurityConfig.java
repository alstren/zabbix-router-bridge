package ru.krsmon.zabbixrouterbridge.config;

import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.krsmon.zabbixrouterbridge.domain.service.UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
  public static final String ROLE_ROOT = "ROLE_ROOT";
  protected static final String ROLE_ZABBIX = "ROLE_ZABBIX";
  protected static final int STRENGTH = 8;
  protected final UserService userService;

  @Bean
  SecurityFilterChain filterChain(@NonNull HttpSecurity http) throws Exception {
    return http
        .cors()
        .and().csrf().disable()
        .userDetailsService(userService)
        .authorizeRequests()
        .antMatchers("/metrics/**").permitAll()
        .antMatchers("/router/**").hasAnyAuthority(ROLE_ROOT, ROLE_ZABBIX)
        .anyRequest().authenticated()
        .and().formLogin().permitAll()
        .and().logout().permitAll()
        .and().httpBasic()
        .and().rememberMe()
        .and().build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(STRENGTH, new SecureRandom());
  }

}
