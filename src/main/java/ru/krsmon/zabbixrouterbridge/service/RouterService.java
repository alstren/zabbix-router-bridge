package ru.krsmon.zabbixrouterbridge.service;

import org.springframework.lang.NonNull;
import ru.krsmon.zabbixrouterbridge.dto.Brand;
import ru.krsmon.zabbixrouterbridge.dto.Protocol;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyRequest;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyResponse;

public interface RouterService {

  /**
   * Execute survey on router
   *
   * @param brand router brand
   * @param proto router protocol
   * @param request device list and params
   * @return result of survey
   */
  @NonNull RouterSurveyResponse survey(@NonNull Brand brand, @NonNull Protocol proto, @NonNull RouterSurveyRequest request);
}
