package ru.krsmon.zabbixrouterbridge.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.krsmon.zabbixrouterbridge.clients.config.ClientCfg;
import ru.krsmon.zabbixrouterbridge.dto.Brand;
import ru.krsmon.zabbixrouterbridge.dto.RouterSurveyRequest;

@Mapper
public interface RouterMapper {

  @Mapping(target = "ip", source = "request.ip")
  @Mapping(target = "port", source = "request.service")
  @Mapping(target = "login", source = "request.login")
  @Mapping(target = "password", source = "request.password")
  @Mapping(target = "invite", source = "brand.invite")
  ClientCfg toCfg(RouterSurveyRequest request, Brand brand);

}
