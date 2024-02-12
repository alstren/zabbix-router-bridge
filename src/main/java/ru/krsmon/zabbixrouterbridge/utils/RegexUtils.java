package ru.krsmon.zabbixrouterbridge.utils;

import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.DEFAULT_IP;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.DEFAULT_MAC;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.LINE_SEPARATOR;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import ru.krsmon.zabbixrouterbridge.dto.DeviceDto;
import ru.krsmon.zabbixrouterbridge.dto.Policy;

@UtilityClass
public class RegexUtils {
  private static final Pattern IP_FIND = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3})");
  private static final Pattern MAC_FIND = Pattern.compile("((\\w{2}:){5}\\w{2})");
  private static final Pattern PACKET_LOSS = Pattern.compile("[\\s|=]?(\\d{1,3})%");
  private static final String PATTERN_REPLACE = "[^0-9]";
  private static final String IP_PREFIX = "192.168";

  public static boolean filterNonDefaultDevice(@NonNull DeviceDto device) {
    return !DEFAULT_MAC.equalsIgnoreCase(device.getMac());
  }

  public static Map<String, String> toArpMap(@NonNull String log) {
    return Arrays.stream(log.split(LINE_SEPARATOR))
        .map(line -> {
          var ipMatcher = IP_FIND.matcher(line);
          var macMatcher = MAC_FIND.matcher(line);
          return ipMatcher.find() && macMatcher.find()
              ? Map.entry(macMatcher.group(0).toUpperCase(), ipMatcher.group(0))
              : null;
        })
        .filter(pair -> nonNull(pair) &&
            !DEFAULT_MAC.equalsIgnoreCase(pair.getKey()) &&
            !DEFAULT_IP.equalsIgnoreCase(pair.getValue()) &&
            pair.getValue().startsWith(IP_PREFIX))
        .distinct()
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static Map.Entry<Boolean, String> toPingResult(@NonNull String logs) {
    try {
      for (var line : logs.split(LINE_SEPARATOR)) {
        var matcher = PACKET_LOSS.matcher(line);
        if (matcher.find()) {
          var loss = parseInt(matcher.group(0).replaceAll(PATTERN_REPLACE, ""));
          return Map.entry(loss != 100, "packet loss %s%s".formatted(loss, "%"));
        }
      }
      return Map.entry(false, "unknown packet loss");
    } catch (Exception exception) {
      return Map.entry(false, "fail ping: message '%s'".formatted(exception.getLocalizedMessage()));
    }
  }

  public static boolean isOnline(boolean isPingSuccess, boolean isPortsOpened, @NonNull Policy policy) {
    switch (policy) {
      case PING_AND_PORTS -> { return isPingSuccess && isPortsOpened; }
      case PING_OR_PORTS -> { return isPingSuccess || isPortsOpened; }
      default -> { return  isPingSuccess; }
    }
  }

}
