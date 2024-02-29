package ru.krsmon.zabbixrouterbridge.clients.impl;

import static java.lang.System.err;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static org.apache.sshd.client.SshClient.setUpDefaultSimpleClient;
import static ru.krsmon.zabbixrouterbridge.exception.BridgeError.EXECUTION_ERROR;
import static ru.krsmon.zabbixrouterbridge.utils.LogUtils.printLog;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.simple.SimpleClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import ru.krsmon.zabbixrouterbridge.clients.Client;
import ru.krsmon.zabbixrouterbridge.clients.config.ClientCfg;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;

@Slf4j
@RequestScope
@Service("SSH")
public class ClientSshImpl implements Client {
  private final SimpleClient client = setUpDefaultSimpleClient();
  private ClientSession session;

  @Value("${server.tomcat.connection-timeout}")
  private Integer timeout;

  @Override
  public boolean connect(@NonNull ClientCfg cfg) {
    try {
      client.setConnectTimeout(timeout * 1000);
      client.setAuthenticationTimeout(timeout * 100);
      session = client.sessionLogin(cfg.ip(), cfg.port(), cfg.login(), cfg.password());
      boolean isConnected = session.isOpen() && session.isAuthenticated();
      log.info("SSH: %s %s:%s.".formatted(
          isConnected ? "Successfully connected to" : "Fail to open SSH session with",
          cfg.ip(), cfg.port()));
      return isConnected;
    } catch (Exception ex) {
      printLog("SSH: Error connect to '%s:%s', message: '%s'"
          .formatted(cfg.ip(), cfg.port(), ex.getLocalizedMessage()), ex);
      return false;
    }
  }

  @NonNull
  @Override
  public String execute(@NonNull String cmd) throws BridgeException {
    try {
      var result = session.executeRemoteCommand(cmd, err, UTF_8);
      log.debug("SSH: Execute command '%s', response: \n'%s'".formatted(cmd, result));
      return result;
    } catch (Exception ex) {
      printLog("SSH: Execution error: '%s'".formatted(ex.getLocalizedMessage()), ex);
      throw new BridgeException(EXECUTION_ERROR, ex);
    }
  }

  @Override
  public void close() {
    try{
      if (nonNull(session) && !session.isClosed()) {
        session.close();
        session = null;
      }
      if (client.isOpen()) {
        client.close();
      }
    } catch (Exception ex) {
      printLog("SSH: Fail disconnect, message: '%s'"
          .formatted(ex.getLocalizedMessage()), ex);
    }
  }

}
