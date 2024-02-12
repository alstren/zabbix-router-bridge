package ru.krsmon.zabbixrouterbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.krsmon.zabbixrouterbridge.domain.repository")
public class ZabbixRouterBridgeApplication {

	public static void main(String... args) {
		SpringApplication.run(ZabbixRouterBridgeApplication.class, args);
	}

}
