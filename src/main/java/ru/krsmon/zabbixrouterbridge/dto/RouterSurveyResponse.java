package ru.krsmon.zabbixrouterbridge.dto;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Builder.Default;
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

  @Default
  private Map<String, String> discovery = Map.of();

  @Default
  private Map<String, DeviceStatus> result = errorResult(Status.NAN);

  public static Map<String, DeviceStatus> errorResult(@NonNull Status status) {
    return Stream.of("ROUTER", "DVR1", "DVR2", "CAM1", "CAM2", "KEEPER1", "KEEPER2", "HOTSPOT1", "TERMINAL1", "TERMINAL2", "KKT1", "KKT2")
        .map(key -> Map.entry(
            key,
            DeviceStatus.builder()
                .code(status.getCode())
                .message("n/a")
                .hasError(false)
                .build()))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}
