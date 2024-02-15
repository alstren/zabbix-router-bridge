package ru.krsmon.zabbixrouterbridge.service.impl;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.NAN;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.OFFLINE;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.ONLINE;
import static ru.krsmon.zabbixrouterbridge.dto.RouterSurveyResponse.errorResult;
import static ru.krsmon.zabbixrouterbridge.exception.BridgeError.INTERNAL_EXCEPTION;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.MACROS_LIP;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.MACRO_ROUTER_ID;
import static ru.krsmon.zabbixrouterbridge.utils.CheckUtils.fullCheck;
import static ru.krsmon.zabbixrouterbridge.utils.CheckUtils.isShortScenery;
import static ru.krsmon.zabbixrouterbridge.utils.CheckUtils.shortCheck;
import static ru.krsmon.zabbixrouterbridge.utils.CheckUtils.validateResult;
import static ru.krsmon.zabbixrouterbridge.utils.PortScanner.scanPorts;
import static ru.krsmon.zabbixrouterbridge.utils.RegexUtils.toArpMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.krsmon.zabbixrouterbridge.clients.Client;
import ru.krsmon.zabbixrouterbridge.domain.mapper.RouterMapper;
import ru.krsmon.zabbixrouterbridge.dto.Brand;
import ru.krsmon.zabbixrouterbridge.dto.DeviceDto;
import ru.krsmon.zabbixrouterbridge.dto.DeviceStatus;
import ru.krsmon.zabbixrouterbridge.dto.Protocol;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyRequest;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyResponse;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;
import ru.krsmon.zabbixrouterbridge.external.zabbix.service.ZabbixService;
import ru.krsmon.zabbixrouterbridge.service.RouterService;
import ru.krsmon.zabbixrouterbridge.utils.CheckUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouterServiceImpl implements RouterService {
  private static final String ROUTER_KEY = "ROUTER";
  private static final String SHORT_CHECK = "Short check, available ports: %s";
  private static final String FULL_CHECK_SUCCESS = "Connected, available ports: %s";
  private static final String FULL_CHECK_FAIL = "Fail connect, available ports: %s";

  private final RouterMapper routerMapper;
  private final ZabbixService zabbixService;
  private final Map<String, Client> clients;

  @NonNull
  @Override
  public RouterSurveyResponse survey(@NonNull Brand brand, @NonNull Protocol proto, @NonNull RouterSurveyRequest request) {
    var macrosNeedUpdate = new HashMap<String, String>();
    var resultMap = new HashMap<String, DeviceStatus>();
    var arpMap = new HashMap<String, String>();
    var message = new StringBuilder();

    if (request.getZabbixId() == 0) {
      request.setZabbixId(zabbixService.getHostId(request.getName()));
      macrosNeedUpdate.put(MACRO_ROUTER_ID, valueOf(request.getZabbixId()));
      if (request.getZabbixId() == 0) log.warn("SHELL: Zabbix router ID not set.");
    }

    var routerAvailablePorts = scanPorts(request.getIp(), request.getPorts());
    if (isShortScenery(brand, proto, routerAvailablePorts.contains(request.getService()))) {

      resultMap.put(
          ROUTER_KEY,
          DeviceStatus.builder()
              .code(routerAvailablePorts.isEmpty() ? OFFLINE.getCode() : ONLINE.getCode())
              .message(SHORT_CHECK.formatted(routerAvailablePorts))
              .hasError(routerAvailablePorts.isEmpty() || routerAvailablePorts.contains(request.getService()))
              .build());

      resultMap.putAll(request.getDevices().stream()
          .filter(CheckUtils::isNonDefaultDevice)
          .map(device -> shortCheck(request.getIp(), device))
          .collect(toMap(Entry::getKey, Entry::getValue)));

    } else {

      try (var client = clients.get(proto.name())) {
        if (client.connect(routerMapper.toCfg(request, brand))) {
          resultMap.put(
              ROUTER_KEY,
              DeviceStatus.builder()
                  .code(routerAvailablePorts.isEmpty() ? OFFLINE.getCode() : ONLINE.getCode())
                  .message(FULL_CHECK_SUCCESS.formatted(routerAvailablePorts))
                  .hasError(false)
                  .build());

          arpMap.putAll(toArpMap(client.execute(brand.getArp())));
          resultMap.putAll(request.getDevices().stream()
              .filter(CheckUtils::isNonDefaultDevice)
              .peek(device -> add2UpdateIfNeed(device, arpMap, macrosNeedUpdate))
              .peek(device -> device.setIp(arpMap.getOrDefault(device.getMac(), device.getIp())))
              .map(device -> fullCheck(request.getIp(), device, brand.getPing(device.getIp()), client))
              .collect(toMap(Entry::getKey, Entry::getValue)));

        } else {
          resultMap.put(
              ROUTER_KEY,
              DeviceStatus.builder()
                  .code(routerAvailablePorts.isEmpty() ? OFFLINE.getCode() : ONLINE.getCode())
                  .message(FULL_CHECK_FAIL.formatted(routerAvailablePorts))
                  .hasError(true)
                  .build());

          message.append("Fail connect to host.");
          resultMap.putAll(request.getDevices().stream()
              .filter(CheckUtils::isNonDefaultDevice)
              .map(device -> device.getPorts().isEmpty()
                  ? Map.entry(
                      device.getKey(),
                      DeviceStatus.builder()
                          .code(NAN.getCode())
                          .message("Failed to check")
                          .hasError(true)
                          .build())
                  : shortCheck(request.getIp(), device))
              .collect(toMap(Entry::getKey, Entry::getValue)));
        }
      } catch (Exception ex) {
        log.error("SURVEY FAIL: '%s'".formatted(ex.getLocalizedMessage()));
        throw new BridgeException(INTERNAL_EXCEPTION, ex);
      }
    }

    if (request.getZabbixId() != 0 && !macrosNeedUpdate.isEmpty()) {
      zabbixService.updateMacros(request.getZabbixId(), macrosNeedUpdate);
    }

    var isSuccess = validateResult(resultMap);

    return RouterSurveyResponse.builder()
        .code(isSuccess ? message.isEmpty() ? 200 : 201 : 503)
        .message(isSuccess ? message.toString().isEmpty() ? "Success" : message.toString() : "HOST OFFLINE")
        .result(isSuccess ? resultMap : errorResult(OFFLINE))
        .discovery(arpMap)
        .build();
  }

  @SneakyThrows
  protected void add2UpdateIfNeed(@NonNull DeviceDto device, @NonNull Map<String, String> arpMap,
                                  @NonNull Map<String, String> macrosNeedUpdate) {
    if (arpMap.containsKey(device.getMac()) && !device.getIp().equalsIgnoreCase(arpMap.get(device.getMac()))) {
      log.info("IP '%s' will change to '%s'".formatted(device.getIp(), arpMap.get(device.getMac())));
      macrosNeedUpdate.put(
          MACROS_LIP.formatted(device.getKey().toUpperCase()),
          arpMap.get(device.getMac()));
    }
  }

}
