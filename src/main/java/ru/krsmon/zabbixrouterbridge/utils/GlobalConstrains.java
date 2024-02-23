package ru.krsmon.zabbixrouterbridge.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GlobalConstrains {
  public static final String LINE_SEPARATOR = "\n";
  public static final String REPLACED_SYMBOLS = "[\\W]";
  public static final String LOGIN = "ogin";
  public static final String PASS = "assword";
  public static final String COMMAND = "%s\r";
  public static final String START = "%s_start";
  public static final String END = "%s_end";
  public static final String BREAK = "\r";

  public static final String DEFAULT_IP = "0.0.0.0";
  public static final String DEFAULT_MAC = "00:00:00:00:00:00";

  // Special flags
  public static final String POR = "POR"; // Flag for forced policy change.

}
