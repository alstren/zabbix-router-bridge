package ru.krsmon.zabbixrouterbridge.controller;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static ru.krsmon.zabbixrouterbridge.dto.DeviceStatus.Status.NAN;
import static ru.krsmon.zabbixrouterbridge.dto.RouterSurveyResponse.errorResult;

import io.micrometer.core.annotation.Counted;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyResponse;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {
  //private final BridgeNotificationBot notificationBot;

  @Counted(value = "router.survey.error.validation")
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<RouterSurveyResponse> methodArgumentNotValidException(@NonNull MethodArgumentNotValidException exception) {
    var message = "VALIDATION: %s".formatted(requireNonNull(exception.getBindingResult().getFieldError()).getDefaultMessage());
    //notificationBot.notifyAdmin(message);
    log.error(message);
    return ResponseEntity.ok(RouterSurveyResponse.builder()
        .code(BAD_REQUEST.value())
        .message(message)
        .result(errorResult(NAN))
        .discovery(Map.of())
        .build());
  }

  @Counted(value = "router.survey.error.internal")
  @ExceptionHandler(BridgeException.class)
  public ResponseEntity<RouterSurveyResponse> methodArgumentNotValidException(@NonNull BridgeException exception) {
    var message = "INTERNAL: %s".formatted(exception.getLocalizedMessage());
    //notificationBot.notifyAdmin(message);
    log.error(message);
    return ResponseEntity.ok(RouterSurveyResponse.builder()
        .code(exception.getError().getStatus().value())
        .message(message)
        .result(errorResult(NAN))
        .discovery(Map.of())
        .build());
  }

  @Counted(value = "router.survey.error.critical")
  @ExceptionHandler(Exception.class)
  public ResponseEntity<RouterSurveyResponse> exception(@NonNull Exception exception) {
    var message = "CRITICAL: %s".formatted(exception.getLocalizedMessage());
    //notificationBot.notifyAdmin(message);
    log.error(message);
    return ResponseEntity.ok(RouterSurveyResponse.builder()
        .code(INTERNAL_SERVER_ERROR.value())
        .message(message)
        .result(errorResult(NAN))
        .discovery(Map.of())
        .build());
  }

}
