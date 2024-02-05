package ru.krsmon.zabbixrouterbridge.domain.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@MappedSuperclass
public abstract class BaseModel implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "id", nullable = false, unique = true)
  protected Integer id;

  @Version
  @Column(name = "version")
  protected Integer version;

  @CreationTimestamp
  @Column(name = "created")
  protected LocalDateTime created;

  @UpdateTimestamp
  @Column(name = "updated")
  protected LocalDateTime updated;

}
