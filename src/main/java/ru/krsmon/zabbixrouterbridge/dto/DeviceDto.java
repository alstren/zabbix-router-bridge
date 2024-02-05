package ru.krsmon.zabbixrouterbridge.dto;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Stream;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@ToString
public class DeviceDto {

  @NotBlank(message = "The device MAC is required.")
  @Pattern(regexp = "^([a-zA-Z0-9]{2}:){5}[a-zA-Z0-9]{2}$", message = "Invalid format of device MAC.")
  @Size(min = 17, max = 17, message = "The size of mac must be 17 characters.")
  private String mac;

  @NotNull(message = "The TYPE of device is required.")
  private DeviceType type;

  @NotBlank(message = "The IP address of device is required.")
  @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", message = "Invalid format of IP device.")
  @Size(min = 7, max = 15, message = "The IP length of device must be from 7 to 15 characters.")
  private String ip;

  @Range(min = 0, max = 65535, message = "The HTTP port of router must be from 0 to 65535.")
  private int http;

  @Range(min = 0, max = 65535, message = "The MEDIA port of router must be from 0 to 65535.")
  private int media;

  @Range(min = 0, max = 65535, message = "The SERVICE port of router must be from 0 to 65535.")
  private int service;

  @NotBlank(message = "The external KEY of device is required.")
  private String key;

  public String getMac() {
    return mac.toUpperCase();
  }

  public Set<Integer> getPorts() {
    return Stream.of(http, media, service)
        .filter(port -> port != 0)
        .collect(toSet());
  }

}
