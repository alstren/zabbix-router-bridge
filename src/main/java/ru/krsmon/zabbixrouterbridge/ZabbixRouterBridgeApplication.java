package ru.krsmon.zabbixrouterbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableCaching
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.krsmon.zabbixrouterbridge.domain.repository")
public class ZabbixRouterBridgeApplication {

	public static void main(String... args) {
		SpringApplication.run(ZabbixRouterBridgeApplication.class, args);
	}

}
