package ru.krsmon.zabbixrouterbridge.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
@UtilityClass
public class LogUtils {

  public static void printLog(@NonNull String msg, @NonNull Exception ex) {
    if (log.isDebugEnabled()) log.error(msg, ex);
    else log.error(msg);
  }

}
