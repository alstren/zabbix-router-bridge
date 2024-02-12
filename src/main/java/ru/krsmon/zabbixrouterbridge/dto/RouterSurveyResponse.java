package ru.krsmon.zabbixrouterbridge.dto;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status;

@Getter
@Setter
@Builder
public class RouterSurveyResponse {
  private int code;
  private String message;
  private Map<String, String> discovery;
  private Map<String, Object> result;

  public static Map<String, Object> errorResult(@NonNull Status status) {
    return Stream.of("ROUTER", "DVR1", "DVR2", "CAM1", "CAM2", "KEEPER1", "KEEPER2", "HOTSPOT1", "TERMINAL1", "TERMINAL2", "KKT1", "KKT2")
        .map(key -> Map.entry(key, (Object) new DeviceStatus(status, null)))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}
