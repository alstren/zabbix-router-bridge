package ru.krsmon.zabbixrouterbridge.clients.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ru.krsmon.zabbixrouterbridge.exception.BridgeError.EXECUTION_ERROR;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.COMMAND;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.LOGIN;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.PASS;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import ru.krsmon.zabbixrouterbridge.clients.Client;
import ru.krsmon.zabbixrouterbridge.clients.config.ClientCfg;
import ru.krsmon.zabbixrouterbridge.exception.BridgeException;

@Slf4j
@RequestScope
@Service("TELNET")
public class ClientTelnet2Impl implements Client {
  private final TelnetClient client = new TelnetClient();

  @Value("${server.tomcat.connection-timeout}")
  private Integer timeout;

  private String invite;

  private PrintStream writer;
  private InputStream reader;

  @Override
  public boolean connect(@NonNull ClientCfg cfg) {
    try {
      log.info("TELNET: Connect to '%s:%s'...".formatted(cfg.ip(), cfg.port()));
      client.setConnectTimeout(timeout * 1000);
      client.connect(cfg.ip(), cfg.port());
      client.setKeepAlive(true);
      client.setCharset(UTF_8);
      writer = new PrintStream(client.getOutputStream());
      reader = client.getInputStream();

      log.debug(readUntil(LOGIN));
      write(COMMAND.formatted(cfg.login()));
      log.debug(readUntil(PASS));
      write(COMMAND.formatted(cfg.password()));
      log.debug(readUntil(this.invite = cfg.invite()));

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
      write(COMMAND.formatted(cmd));
      var response = readUntil(invite);
      log.debug("TELNET: Response of executed command: \n'%s'".formatted(response));
      return response;
    } catch (Exception ex) {
      log.error("TELNET: Execution error: '%s'".formatted(ex.getLocalizedMessage()));
      throw new BridgeException(EXECUTION_ERROR, ex);
    }
  }

  @Override
  @SneakyThrows
  public void close() {
    if (client.isConnected()) client.disconnect();
  }

  public String readUntil(String pattern) throws IOException {
    var sb = new StringBuilder();
    while (true) {
      sb.append((char) reader.read());
      if (sb.toString().endsWith(pattern)) return sb.toString();
    }
  }

  public void write(String value) {
    writer.println(value);
    writer.flush();
  }

}
