package ru.krsmon.zabbixrouterbridge.domain.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.krsmon.zabbixrouterbridge.domain.model.User;
import ru.krsmon.zabbixrouterbridge.domain.repository.UserRepository;
import ru.krsmon.zabbixrouterbridge.domain.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repository;

  @NonNull
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
    log.info("Find user by username '%s'...".formatted(username));

    var user = repository.findUserByUsername(username);
    if (user.isEmpty()) {
      throw new UsernameNotFoundException("User '%s' not found.".formatted(username));
    }

    return user.get();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findUserByTelegramId(@NonNull String id) {
    log.info("Find user by telegram id '%s'...".formatted(id));
    return repository.findUserByTelegramId(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findAll() {
    log.info("Getting all users.");
    return repository.findAll();
  }
}
