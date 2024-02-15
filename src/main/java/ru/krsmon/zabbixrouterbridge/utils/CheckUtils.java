package ru.krsmon.zabbixrouterbridge.utils;

import static ru.krsmon.zabbixrouterbridge.dto.Brand.TPLINK;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.ERROR;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.OFFLINE;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.ONLINE;
import static ru.krsmon.zabbixrouterbridge.dto.Policy.PING_OR_PORTS;
import static ru.krsmon.zabbixrouterbridge.dto.Protocol.NONE;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.DEFAULT_IP;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.DEFAULT_MAC;
import static ru.krsmon.zabbixrouterbridge.utils.PortScanner.scanPorts;
import static ru.krsmon.zabbixrouterbridge.utils.RegexUtils.toPingResult;

import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import ru.krsmon.zabbixrouterbridge.clients.Client;
import ru.krsmon.zabbixrouterbridge.dto.Brand;
import ru.krsmon.zabbixrouterbridge.dto.DeviceDto;
import ru.krsmon.zabbixrouterbridge.dto.DeviceStatus;
import ru.krsmon.zabbixrouterbridge.dto.Policy;
import ru.krsmon.zabbixrouterbridge.dto.Protocol;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;

@Slf4j
@UtilityClass
public class CheckUtils {
  private static final String FAIL_PREFIX = "fail:";
  private static final String IP_NOT_FOUND_IN_ARP = "IP not found in ARP";
  private static final String FULL_CHECK_RESULT = "Ping: %s, available ports: %s";
  private static final String SHORT_CHECK_RESULT = "No ping, available ports: %s";

  public static boolean isNonDefaultDevice(@NonNull DeviceDto device) {
    return !DEFAULT_MAC.equalsIgnoreCase(device.getMac());
  }

  public static boolean isShortScenery(@NonNull Brand brand, @NonNull Protocol proto, boolean isServicePortAvail) {
    return TPLINK.equals(brand) || NONE.equals(proto) || !isServicePortAvail;
  }

  public static boolean isOnline(boolean isSuccessPing, boolean hasOpenedPorts, @NonNull Policy policy) {
    switch (policy) {
      case PING_AND_PORTS -> { return isSuccessPing && hasOpenedPorts; }
      case PING_OR_PORTS -> { return isSuccessPing || hasOpenedPorts; }
      default -> { return  isSuccessPing; }
    }
  }

  public static boolean validateResult(@NonNull Map<String, DeviceStatus> resultMap) {
    return resultMap.values().stream()
        .map(DeviceStatus::getCode)
        .anyMatch(code -> code == 1);
  }

  public static Map.Entry<String, DeviceStatus> fullCheck(@NonNull String hostIp, @NonNull DeviceDto device,
                                                          @NonNull String pingCmd, @NonNull Client client) {
    var openedPorts = scanPorts(hostIp, device.getPorts());

    try {
      var pingResult = DEFAULT_IP.equalsIgnoreCase(device.getIp())
          ? Map.entry(false, IP_NOT_FOUND_IN_ARP)
          : executePing(client, pingCmd);
      var extMessage = FULL_CHECK_RESULT.formatted(pingResult.getValue(), openedPorts);

      return Map.entry(
          device.getKey(),
          DeviceStatus.builder()
              .code(isOnline(pingResult.getKey(), !openedPorts.isEmpty(), device.getType().getPolicy())
                  ? ONLINE.getCode()
                  : OFFLINE.getCode())
              .message(extMessage)
              .hasError(pingResult.getValue().startsWith(FAIL_PREFIX))
              .build());

    } catch (BridgeException be) {
      log.error("FULL-CHECK-FAIL: Message: '%s'".formatted(be.getLocalizedMessage()));
      var extMessage = FULL_CHECK_RESULT.formatted(be.getLocalizedMessage(), openedPorts);

      return Map.entry(
          device.getKey(),
          DeviceStatus.builder()
              .code(isOnline(false, !openedPorts.isEmpty(), PING_OR_PORTS)
                  ? ONLINE.getCode()
                  : device.getPorts().isEmpty() ? ERROR.getCode() : OFFLINE.getCode())
              .message(extMessage)
              .hasError(true)
              .build());
    }
  }

  public static Map.Entry<String, DeviceStatus> shortCheck(@NonNull String hostIp, @NonNull DeviceDto device) {
    var openedPorts = scanPorts(hostIp, device.getPorts());
    return Map.entry(
        device.getKey(),
        DeviceStatus.builder()
            .code(openedPorts.isEmpty() ? OFFLINE.getCode() : ONLINE.getCode())
            .message(SHORT_CHECK_RESULT.formatted(openedPorts))
            .hasError(device.getPorts().isEmpty())
            .build());
  }

  @NonNull
  private Map.Entry<Boolean, String> executePing(@NonNull Client client, @NonNull String cmd) throws BridgeException {
    var result = toPingResult(client.execute(cmd));
    // Repeated ping to prevent frequent false signals
    return result.getKey() ? result : toPingResult(client.execute(cmd));
  }

}
