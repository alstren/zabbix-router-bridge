package ru.krsmon.zabbixrouterbridge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BridgeError {
  EXECUTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
  INTERNAL_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR)
  ;

  private final HttpStatus status;

  BridgeError(HttpStatus status) {
    this.status = status;
  }
}
