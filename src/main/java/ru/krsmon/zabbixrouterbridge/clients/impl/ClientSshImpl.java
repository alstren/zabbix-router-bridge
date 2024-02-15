package ru.krsmon.zabbixrouterbridge.clients.impl;

import static java.lang.System.err;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.krsmon.zabbixrouterbridge.exception.BridgeError.EXECUTION_ERROR;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import ru.krsmon.zabbixrouterbridge.clients.Client;
import ru.krsmon.zabbixrouterbridge.clients.config.ClientCfg;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;

@Slf4j
@RequestScope
@Component("SSH")
public class ClientSshImpl implements Client {
  private final SshClient client = SshClient.setUpDefaultClient();

  @Value("${server.tomcat.connection-timeout}")
  private Integer timeout;

  private ClientSession session;

  @Override
  public boolean connect(@NonNull ClientCfg cfg) {
    try {
      log.info("SSH: Connect to '%s:%s'...".formatted(cfg.ip(), cfg.port()));
      client.start();
      session = client.connect(cfg.login(), cfg.ip(), cfg.port())
          .verify(timeout, SECONDS)
          .getSession();
      session.addPasswordIdentity(cfg.password());
      session.auth().verify(timeout, SECONDS);
      log.info("SSH: Connected to '%s:%s'.".formatted(cfg.ip(), cfg.port()));
      return client.isStarted() && session.isOpen();
    } catch (Exception exception) {
      log.error("SSH: Fail connection to '%s:%s', message: '%s'"
          .formatted(cfg.ip(), cfg.port(), exception.getLocalizedMessage()));
      return false;
    }
  }

  @NonNull
  @Override
  public String execute(@NonNull String cmd) throws BridgeException {
    try {
      log.info("SSH: Execute command '%s'".formatted(cmd));
      var result = session.executeRemoteCommand(cmd, err, UTF_8);
      log.debug("SSH: Response of executed command: \n'%s'".formatted(result));
      return result;
    } catch (Exception ex) {
      log.error("SSH: Execution error: '%s'".formatted(ex.getLocalizedMessage()));
      throw new BridgeException(EXECUTION_ERROR, ex);
    }
  }

  @Override
  @SneakyThrows
  public void close() {
    if (nonNull(session) && !session.isClosed()) session.close();
    if (nonNull(client) && !client.isClosed()) client.close();
  }

}
