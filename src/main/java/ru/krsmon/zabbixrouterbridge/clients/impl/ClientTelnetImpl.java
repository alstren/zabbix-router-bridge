package ru.krsmon.zabbixrouterbridge.clients.impl;

import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.sf.expectit.matcher.Matchers.contains;
import static ru.krsmon.zabbixrouterbridge.exception.BridgeError.EXECUTION_ERROR;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.BREAK;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.COMMAND;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.END;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.LINE_SEPARATOR;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.LOGIN;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.PASS;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.REPLACED_SYMBOLS;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.START;

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
  private String invite;

  @Value("${server.tomcat.connection-timeout}")
  protected Integer timeout;

  @Override
  public boolean connect(@NonNull ClientCfg cfg) {
    try {
      log.info("TELNET: Connect to '%s:%s'...".formatted(cfg.ip(), cfg.port()));
      this.invite = cfg.invite();
      client.setDefaultTimeout(timeout * 10_000);
      client.connect(cfg.ip(), cfg.port());

      if (client.isConnected()) {
        expect = new ExpectBuilder()
            .withOutput(client.getOutputStream())
            .withInputs(client.getInputStream())
            .withEchoOutput(routerLog)
            .withEchoInput(routerLog)
            .withLineSeparator(LINE_SEPARATOR)
            .withAutoFlushEcho(true)
            .withTimeout(timeout, SECONDS)
            .withExceptionOnFailure()
            .build();

        log.info("TELNET: Login to host by user '%s'".formatted(cfg.login()));
        expect.expect(contains(LOGIN));
        expect.sendLine(COMMAND.formatted(cfg.login()));
        expect.expect(contains(PASS));
        expect.sendLine(COMMAND.formatted(cfg.password()));
        expect.expect(contains(invite));
      }

      log.info("TELNET: Connected to '%s:%s'.".formatted(cfg.ip(), cfg.port()));
      return client.isConnected();
    } catch (Exception exception) {
      log.error("TELNET: Fail connection to '%s:%s', message: '%s'"
          .formatted(cfg.ip(), cfg.port(), exception.getLocalizedMessage()));
      return false;
    }
  }

  @NonNull
  @Override
  public String execute(@NonNull String cmd) throws BridgeException {
    try {
      log.info("TELNET: Execute command '%s'".formatted(cmd));
      expect.sendLine(COMMAND.formatted(cmd));
      routerLog.append(comment(START, cmd));
      expect.expect(contains(invite));
      routerLog.append(comment(END, cmd));
      var result = routerLog.substring(
          routerLog.toString().indexOf(comment(START, cmd)),
          routerLog.toString().indexOf(comment(END, cmd)));
      log.debug("TELNET: Response of executed command: \n'%s'".formatted(result));
      return result.replaceAll(BREAK, "");
    } catch (Exception ex) {
      log.error("TELNET: Execution error: '%s'".formatted(ex.getLocalizedMessage()));
      throw new BridgeException(EXECUTION_ERROR, ex);
    }
  }

  @Override
  @SneakyThrows
  public void close() {
    if (nonNull(expect)) expect.close();
    if (client.isConnected()) client.disconnect();
  }

  private String comment(@NonNull String pattern, @NonNull String command) {
    return pattern.formatted(command).replace(REPLACED_SYMBOLS, "");
  }

}
