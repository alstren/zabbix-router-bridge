package ru.krsmon.zabbixrouterbridge.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.krsmon.zabbixrouterbridge.dto.Brand;
import ru.krsmon.zabbixrouterbridge.dto.Protocol;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyRequest;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyResponse;
import ru.krsmon.zabbixrouterbridge.service.RouterService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/router")
public class RouterRestController {
  private final RouterService routerService;

  @Timed(value = "router.survey.timer")
  @Counted(value = "router.survey.counter")
  @PostMapping(value = "/{brand}/{proto}/survey")
  public ResponseEntity<RouterSurveyResponse> survey(@PathVariable Brand brand,
      @PathVariable Protocol proto, @RequestBody @Valid RouterSurveyRequest request) {

    return ResponseEntity.ok(routerService.survey(brand, proto, request));
  }

}
