package ru.krsmon.zabbixrouterbridge.external.zabbix.service.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpStatus.OK;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.DATA;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.ERROR;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.EXTEND;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.FILTER;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.HOSTID;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.HOSTIDS;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.HOSTMACROID;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.HOST_GET;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.MACRO;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.MESSAGE;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.NAME;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.OUTPUT;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.RESULT;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.USERMACRO_CREATE;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.USERMACRO_GET;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.USERMACRO_UPDATE;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.krsmon.zabbixrouterbridge.external.zabbix.api.ZabbixApi;
import ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixRequest;
import ru.krsmon.zabbixrouterbridge.external.zabbix.service.ZabbixService;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class ZabbixServiceImpl implements ZabbixService {
  protected final ZabbixApi api;

  @Value("${external.zabbix.token}")
  protected String apiToken;

  @Override
  public int getHostId(@NonNull String hostName) {
    int id = 0;
    try {
      log.info("ZABBIX: Get host ID by NAME '%s'...".formatted(hostName));
      var request = ZabbixRequest.builder()
          .method(HOST_GET)
          .auth(apiToken)
          .params(Map.of(
              OUTPUT, List.of(HOSTID),
              FILTER, Map.of(NAME, hostName)
          )).build();

      var response = validateResponse(api.request(request));
      var hosts = (List<Map<String, String>>) response.get(RESULT);

      if (hosts.isEmpty()) {
        log.warn("ZABBIX: Hosts lists is empty (not found).");
      }
      id = Integer.parseInt(hosts.get(0).get(HOSTID));
    } catch (NoClassDefFoundError | Exception exception) {
      log.warn("ZABBIX: Fail getting host, message: '%s'".formatted(exception.getLocalizedMessage()));
    }
    return id;
  }

  @Override
  public void updateMacros(int hostId, @NonNull Map<String, String> macros) {
    var hostMacros = (List<Map<String, String>>) getHostMacros(hostId, macros).get(RESULT);
    var macros2Update = new ArrayList<Map<String, String>>();
    var macros2Create = new ArrayList<Map<String, String>>();

    for (var updMacro : macros.entrySet()) {
      var targetMap = hostMacros.stream()
          .filter(ssMap -> ssMap.containsValue(updMacro.getKey()))
          .findFirst();

      if (targetMap.isPresent()) {
        // UPDATE
        macros2Update.add(Map.of(
            HOSTMACROID, targetMap.get().get(HOSTMACROID),
            VALUE, updMacro.getValue()
        ));
      } else {
        // CREATE
        macros2Create.add(Map.of(
            HOSTID, String.valueOf(hostId),
            MACRO, updMacro.getKey(),
            VALUE, updMacro.getValue()
        ));
      }
    }

    if (!macros2Update.isEmpty()) {
      try {
        log.info("ZABBIX: Update host macros by hostId '%s'...".formatted(hostId));
        var request = ZabbixRequest.builder()
            .method(USERMACRO_UPDATE)
            .auth(apiToken)
            .params(macros2Update)
            .build();
        validateResponse(api.request(request));
      } catch (NoClassDefFoundError | Exception exception) {
        log.warn("ZABBIX: Fail updating host macros, message: '%s'".formatted(exception.getLocalizedMessage()));
      }
    }

    if (!macros2Create.isEmpty()) {
      log.info("ZABBIX: Create host macros by hostId '%s'...".formatted(hostId));
      try {
        var request = ZabbixRequest.builder()
            .method(USERMACRO_CREATE)
            .auth(apiToken)
            .params(macros2Create)
            .build();
        validateResponse(api.request(request));
      } catch (NoClassDefFoundError | Exception exception) {
        log.warn("ZABBIX: Fail creating host macros, message: '%s'".formatted(exception.getLocalizedMessage()));
      }
    }
  }

  protected Map<String, Object> getHostMacros(int hostId, @NonNull Map<String, String> macros) {
    try {
      log.info("ZABBIX: Get host macros by hostId '%s'...".formatted(hostId));
      var request = ZabbixRequest.builder()
          .method(USERMACRO_GET)
          .auth(apiToken)
          .params(Map.of(
              OUTPUT, EXTEND,
              HOSTIDS, String.valueOf(hostId)
          )).build();

      var response = validateResponse(api.request(request));
      log.info("ZABBIX: Map macros from host: '%s'".formatted(response));
      return response;
    } catch (NoClassDefFoundError | Exception exception) {
      log.warn("ZABBIX: Fail getting host macros, message: '%s'".formatted(exception.getLocalizedMessage()));
      return Map.of();
    }
  }

  private Map<String, Object> validateResponse(ResponseEntity<Map<String, Object>> response) {
    if (!OK.equals(response.getStatusCode())) {
      log.warn("ZABBIX: Fail validation, response: '%s'.".formatted(response));
    }

    if (isNull(response.getBody())) {
      log.warn("ZABBIX: Fail validation, response is null.");
    }

    if (nonNull(response.getBody()) && response.getBody().containsKey(ERROR)) {
      var error = (Map<String, Object>) response.getBody().get(ERROR);
      var message = Optional.of(error.get(MESSAGE)).orElse("n/a");
      var data = Optional.of(error.get(DATA)).orElse("n/a");
      log.warn("ZABBIX: Fail validation, message: '%s', data: '%s'".formatted(message, data));
    }

    return response.getBody();
  }

}
