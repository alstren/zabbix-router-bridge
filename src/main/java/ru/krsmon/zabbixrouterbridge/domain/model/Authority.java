package ru.krsmon.zabbixrouterbridge.domain.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Getter
@Setter
@ToString
@Table(name = "authorities")
public class Authority extends BaseModel implements GrantedAuthority {

  @Column(name = "authority", nullable = false, unique = true, length = 30)
  protected String authority;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Authority authority1 = (Authority) o;
    return new EqualsBuilder()
        .append(id, authority1.id)
        .append(authority, authority1.authority)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(id)
        .append(authority)
        .toHashCode();
  }

}
