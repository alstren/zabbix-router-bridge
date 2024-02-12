package ru.krsmon.zabbixrouterbridge.external.zabbix.service;

import java.util.Map;
import org.springframework.lang.NonNull;

public interface ZabbixService {

  /**
   * Get ID host by name
   *
   * @param hostName имя хоста
   * @return ID хост
   */
  int getHostId(@NonNull String hostName);

  /**
   * Update or create macros into zabbix
   *
   * @param hostId ID хоста
   * @param macros карта макрос/значение
   */
  void updateMacros(int hostId, @NonNull Map<String, String> macros);

}
