package ru.krsmon.zabbixrouterbridge.dto;

import lombok.Getter;

@Getter
public enum DeviceType {
  DVR(Policy.PING_AND_PORTS),
  KEEPER(Policy.PING_OR_PORTS),
  KKT(Policy.PING_OR_PORTS),
  TERMINAL(Policy.PING_OR_PORTS),
  HOTSPOT(Policy.PING_OR_PORTS);

  private final Policy policy;

  DeviceType(Policy policy) {
    this.policy = policy;
  }
}
