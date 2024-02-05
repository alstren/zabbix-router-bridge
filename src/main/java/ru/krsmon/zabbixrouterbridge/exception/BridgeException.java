package ru.krsmon.zabbixrouterbridge.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
@Getter
public class BridgeException extends RuntimeException {
  private final BridgeError error;

  public BridgeException(@NonNull BridgeError error, @NonNull Exception exception) {
    super(exception.getMessage(), exception, true, log.isDebugEnabled());
    this.error = error;
  }

}
