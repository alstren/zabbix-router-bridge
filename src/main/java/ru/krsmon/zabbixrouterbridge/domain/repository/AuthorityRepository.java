package ru.krsmon.zabbixrouterbridge.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.krsmon.zabbixrouterbridge.domain.model.Authority;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
}
