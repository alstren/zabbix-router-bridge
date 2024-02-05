package ru.krsmon.zabbixrouterbridge.utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

@UtilityClass
public class PortScanner {

  public static Set<Integer> scanPorts(@NonNull String host, @NonNull Set<Integer> ports) {
    var result = new HashSet<Integer>();
    for (var port : ports) {
      try (var socket = new Socket()) {
        socket.connect(new InetSocketAddress(host, port), 6_000);
        if (socket.isConnected()) result.add(port);
      } catch (Exception ignored) {}
    }
    return result;
  }

}
