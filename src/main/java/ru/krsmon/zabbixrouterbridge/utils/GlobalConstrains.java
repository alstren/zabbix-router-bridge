package ru.krsmon.zabbixrouterbridge.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GlobalConstrains {
  public static final String NEW_LINE = "\n";
  public static final String LOGIN = "ogin";
  public static final String PASS = "assword";

  public static final String DEFAULT_IP = "0.0.0.0";
  public static final String DEFAULT_MAC = "00:00:00:00:00:00";

  // Special flags
  public static final String POR = "POR"; // Flag for forced policy change.

}
