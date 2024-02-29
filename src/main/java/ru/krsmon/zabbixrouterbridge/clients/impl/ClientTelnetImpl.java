package ru.krsmon.zabbixrouterbridge.clients.impl;

import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.sf.expectit.matcher.Matchers.contains;
import static org.apache.commons.net.SocketClient.NETASCII_EOL;
import static ru.krsmon.zabbixrouterbridge.exception.BridgeError.EXECUTION_ERROR;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.LOGIN;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.PASS;
import static ru.krsmon.zabbixrouterbridge.utils.LogUtils.printLog;

import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import ru.krsmon.zabbixrouterbridge.clients.Client;
import ru.krsmon.zabbixrouterbridge.clients.config.ClientCfg;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;

@Slf4j
@RequestScope
@Component("TELNET")
public class ClientTelnetImpl implements Client {
  private final TelnetClient client = new TelnetClient();
  private final StringBuilder routerLog = new StringBuilder();
  private Expect expect;
  private ClientCfg cfg;

  @Value("${server.tomcat.connection-timeout}")
  private Integer timeout;

  @Override
  public boolean connect(@NonNull ClientCfg cfg) {
    try {
      log.info("TELNET: Connect to '%s:%s'...".formatted(cfg.ip(), cfg.port()));
      this.cfg = cfg;
      client.setDefaultTimeout(timeout * 1000);
      client.connect(cfg.ip(), cfg.port());

      if (client.isConnected()) {
        expect = new ExpectBuilder()
            .withOutput(client.getOutputStream())
            .withInputs(client.getInputStream())
            .withEchoOutput(routerLog)
            .withEchoInput(routerLog)
            .withLineSeparator(NETASCII_EOL)
            .withAutoFlushEcho(true)
            .withTimeout(timeout, SECONDS)
            .withExceptionOnFailure()
            .build();

        log.info("TELNET: Login to host by user '%s'".formatted(cfg.login()));
        expect.expect(contains(LOGIN));
        expect.sendLine(cfg.login());
        expect.expect(contains(PASS));
        expect.sendLine(cfg.password());
        expect.expect(contains(cfg.invite()));
      }

      log.info("TELNET: Connected to '%s:%s'.".formatted(cfg.ip(), cfg.port()));
      return client.isConnected();
    } catch (Exception ex) {
      printLog("TELNET: Fail connection to '%s:%s', message: '%s'"
          .formatted(cfg.ip(), cfg.port(), ex.getLocalizedMessage()), ex);
      return false;
    }
  }

  @NonNull
  @Override
  public String execute(@NonNull String cmd) throws BridgeException {
    try {
      log.info("TELNET: Execute command '%s'".formatted(cmd));
      expect.sendLine(cmd);
      var uuid = UUID.randomUUID().toString();
      routerLog.append(uuid);
      expect.expect(contains(cfg.invite()));
      var result = routerLog.substring(routerLog.toString().indexOf(uuid));
      log.debug("TELNET: Response of executed command: \n'%s'".formatted(result));
      return result;
    } catch (Exception ex) {
      printLog("TELNET: Execution error: '%s'".formatted(ex.getLocalizedMessage()), ex);
      throw new BridgeException(EXECUTION_ERROR, ex);
    }
  }

  @Override
  @SneakyThrows
  public void close() {
    if (nonNull(expect)) {
      expect.close();
      expect = null;
    }
    if (client.isConnected()) {
      client.disconnect();
    }
  }

}
