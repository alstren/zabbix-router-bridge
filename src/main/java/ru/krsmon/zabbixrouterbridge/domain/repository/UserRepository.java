package ru.krsmon.zabbixrouterbridge.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.krsmon.zabbixrouterbridge.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  /**
   * Find user by username
   *
   * @param username name of user
   * @return user if found
   */
  Optional<User> findUserByUsername(@NonNull String username);

  /**
   * Find user by telegram id
   *
   * @param telegramId telegram id user
   * @return user if found
   */
  Optional<User> findUserByTelegramId(@NonNull String telegramId);
}
