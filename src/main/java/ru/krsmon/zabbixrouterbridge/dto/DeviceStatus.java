package ru.krsmon.zabbixrouterbridge.dto;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class DeviceStatus {
  private final int code;
  private final String status;
  private final String message;

  public DeviceStatus(@NonNull Status status, @Nullable String message) {
    this.code = status.getCode();
    this.status = status.getStatus();
    this.message = message;
  }

  @Getter
  public enum Status {
    ONLINE(1, "Online"),
    OFFLINE(2,"Offline"),
    NAN(3, "Not verified"),
    ERROR(4, "Validation error");

    private final int code;
    private final String status;

    Status(int code, String status) {
      this.code = code;
      this.status = status;
    }
  }
}
