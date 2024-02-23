package ru.krsmon.zabbixrouterbridge.domain.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@ToString
@Cacheable
@Table(name = "users", indexes = {
    @Index(name = "idx_user_name", unique = true, columnList = "username"),
    @Index(name = "idx_user_telegram_id", unique = true, columnList = "telegram_id")
})
public class User extends BaseModel implements UserDetails {

  @Column(name = "username", nullable = false, unique = true, length = 50)
  protected String username;

  @Column(name = "password", nullable = false, length = 100)
  protected String password;

  @Column(name = "telegram_id", nullable = false, unique = true, length = 50)
  protected String telegramId;

  @Column(name = "enabled")
  protected boolean enabled;

  @ManyToMany(cascade = ALL, fetch = EAGER)
  @JoinTable(name = "users_authorities",
      joinColumns = @JoinColumn(name = "users_username"),
      inverseJoinColumns = @JoinColumn(name = "authorities_authority"))
  protected Set<Authority> authorities;

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    User user = (User) o;
    return new EqualsBuilder()
        .append(id, user.id)
        .append(enabled, user.enabled)
        .append(username, user.username)
        .append(password, user.password)
        .append(telegramId, user.telegramId)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(id)
        .append(username)
        .append(password)
        .append(telegramId)
        .append(enabled)
        .toHashCode();
  }

}
