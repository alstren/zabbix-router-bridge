package ru.krsmon.zabbixrouterbridge.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
@Builder
public class DeviceStatus {
  private final int code;
  private final String message;
  private final boolean hasError;

  public DeviceStatus(int statusCode, @NonNull String message, boolean hasError) {
    this.code = statusCode;
    this.message = message;
    this.hasError = hasError;
  }

  @Getter
  public enum Status {
    ONLINE(1),  // Device online
    OFFLINE(2), // Device offline
    NAN(3),     // Service general error
    ERROR(4);   // Ping error, ports not declared

    private final int code;

    Status(int code) {
      this.code = code;
    }
  }
}
