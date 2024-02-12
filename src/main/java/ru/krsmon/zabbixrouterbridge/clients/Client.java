package ru.krsmon.zabbixrouterbridge.clients;

import org.springframework.lang.NonNull;
import ru.krsmon.zabbixrouterbridge.clients.config.ClientCfg;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;

public interface Client extends AutoCloseable {

  /**
   * Connect to router
   *
   * @param cfg connection params
   * @return is connected
   */
  boolean connect(@NonNull ClientCfg cfg);

  /**
   * Execute command on router
   *
   * @param cmd command
   * @return result(response from router) of executed command
   * @throws BridgeException error of execution
   */
  @NonNull String execute(@NonNull String cmd) throws BridgeException;

}
