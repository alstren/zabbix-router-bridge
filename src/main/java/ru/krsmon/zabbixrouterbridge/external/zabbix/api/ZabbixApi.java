package ru.krsmon.zabbixrouterbridge.external.zabbix.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixRequest;

@FeignClient(name = "zabbix-api", url = "${external.zabbix.url}")
public interface ZabbixApi {

  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<Map<String, Object>> request(@NonNull ZabbixRequest request);

}
