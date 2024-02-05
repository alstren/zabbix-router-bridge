package ru.krsmon.zabbixrouterbridge.service.impl;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;
import static ru.krsmon.zabbixrouterbridge.dto.Brand.TPLINK;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.ERROR;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.NAN;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.OFFLINE;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.ONLINE;
import static ru.krsmon.zabbixrouterbridge.dto.Protocol.NONE;
import static ru.krsmon.zabbixrouterbridge.dto.RouterSurveyResponse.errorResult;
import static ru.krsmon.zabbixrouterbridge.exception.BridgeError.INTERNAL_EXCEPTION;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.MACROS_LIP;
import static ru.krsmon.zabbixrouterbridge.external.zabbix.model.ZabbixConstrains.MACRO_ROUTER_ID;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.DEFAULT_IP;
import static ru.krsmon.zabbixrouterbridge.utils.PortScanner.scanPorts;
import static ru.krsmon.zabbixrouterbridge.utils.RegexUtils.isOnline;
import static ru.krsmon.zabbixrouterbridge.utils.RegexUtils.toArpMap;
import static ru.krsmon.zabbixrouterbridge.utils.RegexUtils.toPingResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import ru.krsmon.zabbixrouterbridge.utils.RegexUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouterServiceImpl implements RouterService {
  //private final BridgeNotificationBot notificationBot;
  private final RouterMapper routerMapper;
  private final ZabbixService zabbixService;
  private final Map<String, Client> clients;

  @NonNull
  @Override
  public RouterSurveyResponse survey(@NonNull Brand brand, @NonNull Protocol proto, @NonNull RouterSurveyRequest request) {
    var macrosNeedUpdate = new HashMap<String, String>();
    var resultMap = new HashMap<String, Object>();
    var arpMap = new HashMap<String, String>();
    var message = new StringBuilder();

    if (request.getZabbixId() == 0) {
      request.setZabbixId(zabbixService.getHostId(request.getName()));
      macrosNeedUpdate.put(MACRO_ROUTER_ID, valueOf(request.getZabbixId()));
      if (request.getZabbixId() == 0) log.warn("SHELL: Zabbix router ID not set.");
    }

    var routerOpenedPorts = scanPorts(request.getIp(), request.getPorts());
    var isShortScenery = isShortScenery(brand, proto, request.getService(), routerOpenedPorts);

    if (isShortScenery) {
      resultMap.put("ROUTER", new DeviceStatus(routerOpenedPorts.isEmpty() ? OFFLINE : ONLINE,
          "WITHOUT CONNECT, OPENED PORTS %s".formatted(routerOpenedPorts.toString())));

      resultMap.putAll(request.getDevices().stream()
          .filter(RegexUtils::filterNonDefaultDevice)
          .map(device -> processShortSurvey(request.getIp(), device))
          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));

    } else {

      try (var client = clients.get(proto.name())) {
        var isConnected = client.connect(routerMapper.toCfg(request, brand));

        if (isConnected) {
          resultMap.put("ROUTER", new DeviceStatus(routerOpenedPorts.isEmpty() ? OFFLINE : ONLINE,
              "CONNECT: success, OPENED PORTS %s".formatted(routerOpenedPorts.toString())));

          arpMap.putAll(toArpMap(client.execute(brand.getArp())));
          resultMap.putAll(request.getDevices().stream()
              .filter(RegexUtils::filterNonDefaultDevice)
              .peek(device -> add2UpdateIfNeed(device, arpMap, macrosNeedUpdate))
              .peek(device -> device.setIp(arpMap.getOrDefault(device.getMac(), device.getIp())))
              .map(device -> processFullSurvey(request, device, brand, client))
              .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        } else {
          resultMap.put("ROUTER", new DeviceStatus(routerOpenedPorts.isEmpty() ? OFFLINE : ONLINE,
              "ERROR CONNECTION, OPENED PORTS %s".formatted(routerOpenedPorts.toString())));

//          notificationBot.notifyAdmin("Error connect by proto '%s' to '%s'"
//              .formatted(proto.name(), request.getName()));
          message.append("Error connect by proto!");
          resultMap.putAll(request.getDevices().stream()
              .filter(RegexUtils::filterNonDefaultDevice)
              .map(device -> device.getPorts().isEmpty()
                  ? Map.entry(device.getKey(), new DeviceStatus(NAN, "Error connection, not scanned!"))
                  : processShortSurvey(request.getIp(), device))
              .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
      } catch (Exception ex) {
        log.error("SHELL: Error: '%s'".formatted(ex.getLocalizedMessage()));
        throw new BridgeException(INTERNAL_EXCEPTION, ex);
      }
    }

    if (request.getZabbixId() != 0 && !macrosNeedUpdate.isEmpty()) {
     // zabbixService.updateMacros(request.getZabbixId(), macrosNeedUpdate);
    }

    var isSuccess = validateResult(routerOpenedPorts, resultMap);
    return RouterSurveyResponse.builder()
        .code(isSuccess ? message.isEmpty() ? 200 : 201 : 503)
        .message(isSuccess ? message.toString().isEmpty() ? "Success" : message.toString() : "HOST OFFLINE")
        .result(isSuccess ? resultMap : errorResult(OFFLINE))
        .discovery(arpMap)
        .build();
  }

  private boolean validateResult(@NonNull Set<Integer> openedPorts, @NonNull Map<String, Object> resultMap) {
    return resultMap.values().stream()
        .map(value -> ((DeviceStatus) value).getCode())
        .anyMatch(code -> code == 1) || !openedPorts.isEmpty();
  }

  private Map.Entry<String, Object> processFullSurvey(@NonNull RouterSurveyRequest request,
      @NonNull DeviceDto device, Brand brand, @NonNull Client client) {

    try {
      var openedPorts = scanPorts(request.getIp(), device.getPorts());
      var pingResult = DEFAULT_IP.equalsIgnoreCase(device.getIp())
          ? Map.entry(false, "IP not found in ARP")
          : executePing(client, brand.getPing(device.getIp()));
      var extMessage = "PING: %s, OPENED PORTS: %s".formatted(pingResult, openedPorts.toString());

      return Map.entry(device.getKey(), new DeviceStatus(
          isOnline(pingResult.getKey(), !openedPorts.isEmpty(), device.getType().getPolicy()) ? ONLINE : OFFLINE, extMessage));
    } catch (Exception ex) {
      log.error("SHELL: Message: '%s'".formatted(ex.getLocalizedMessage()));
      return Map.entry(device.getKey(), new DeviceStatus(ERROR, ex.getMessage()));
    }
  }

  @NonNull
  // Repeated ping to prevent frequent false signals
  private Map.Entry<Boolean, String> executePing(@NonNull Client client, @NonNull String cmd) {
    var result = toPingResult(client.execute(cmd));
    return result.getKey() ? result : toPingResult(client.execute(cmd));
  }

  private Map.Entry<String, Object> processShortSurvey(@NonNull String hostIp, @NonNull DeviceDto device) {
    var openedPorts = scanPorts(hostIp, device.getPorts());
    return Map.entry(device.getKey(), new DeviceStatus(openedPorts.isEmpty() ? OFFLINE : ONLINE,
        "NO PING, OPENED PORTS: %s".formatted(openedPorts.toString())));
  }

  @SneakyThrows
  protected void add2UpdateIfNeed(@NonNull DeviceDto device, @NonNull Map<String, String> arpMap, @NonNull Map<String, String> macrosNeedUpdate) {
    if (arpMap.containsKey(device.getMac()) && !device.getIp().equalsIgnoreCase(arpMap.get(device.getMac()))) {
      log.info("SHELL: IP '%s' need changed to '%s'".formatted(device.getIp(), arpMap.get(device.getMac())));
      var key = MACROS_LIP.formatted(device.getKey().toUpperCase());
      macrosNeedUpdate.put(key, arpMap.get(device.getMac()));
    }
  }

  private boolean isShortScenery(@NonNull Brand brand, @NonNull Protocol proto, int service, @NonNull Set<Integer> routerOpenedPorts) {
    return TPLINK.equals(brand) || NONE.equals(proto) || !routerOpenedPorts.contains(service);
  }

}
