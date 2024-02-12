package ru.krsmon.zabbixrouterbridge.external.zabbix.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ZabbixRequest implements Serializable {

  @Builder.Default
  private String jsonrpc = "2.0";

  @Builder.Default
  private int id = 1;

  private String auth;
  private String method;
  private Object params;

}
