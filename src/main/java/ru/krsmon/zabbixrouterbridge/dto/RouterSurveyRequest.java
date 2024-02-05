package ru.krsmon.zabbixrouterbridge.dto;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Stream;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@ToString
public class RouterSurveyRequest {

  @PositiveOrZero(message = "The zabbix ID of router must be zero or more.")
  private int zabbixId;

  @NotBlank(message = "The IP address of router is required.")
  @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", message = "Invalid format of IP router.")
  @Size(min = 7, max = 15, message = "The IP length of router must be from 7 to 15 characters.")
  private String ip;

  @NotBlank(message = "The NAME of router is required.")
  @Pattern(regexp = "^(\\[[A-Z\\s]+])?.+$", message = "Invalid format of router NAME.")
  @Size(min = 3, max = 100, message = "The NAME length of router must be from 3 to 50 characters.")
  private String name;

  @NotBlank(message = "The LOGIN of router is required.")
  @Size(min = 3, max = 50, message = "The LOGIN length of router must be from 3 to 50 characters.")
  private String login;

  @NotBlank(message = "The PASSWORD of router is required.")
  @Size(min = 3, max = 50, message = "The PASSWORD length of router must be from 3 to 50 characters.")
  private String password;

  @Range(min = 1, max = 65535, message = "The HTTP port of router must be from 1 to 65535.")
  private int http;

  @Range(min = 1, max = 65535, message = "The SERVICE port of router must be from 1 to 65535.")
  private int service;

  @Valid
  @NotNull(message = "The DEVICES is required.")
  private Set<DeviceDto> devices;

  public Set<Integer> getPorts() {
    return Stream.of(http, service)
        .filter(port -> port != 0)
        .collect(toSet());
  }

}
