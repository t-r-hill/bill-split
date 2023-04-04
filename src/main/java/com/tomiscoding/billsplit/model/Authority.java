package com.tomiscoding.billsplit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue
    private long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Roles role;

    @Override
    @JsonIgnore
    public String getAuthority() {
        return role.name();
    }

    public Authority(Roles role){
        this.role = role;
    }

    public enum Roles {
        ROLE_USER,
        ROLE_ADMIN
    }
}
