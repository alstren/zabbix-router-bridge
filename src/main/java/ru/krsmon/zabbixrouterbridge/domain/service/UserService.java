package ru.krsmon.zabbixrouterbridge.domain.service;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.krsmon.zabbixrouterbridge.domain.model.User;

public interface UserService extends UserDetailsService {

  /**
   * Find user by telegram id
   *
   * @param id user telegram id
   * @return user if found
   */
  Optional<User> findUserByTelegramId(@NonNull String id);

  /**
   * Getting users
   *
   * @return users
   */
  List<User> findAll();
}
