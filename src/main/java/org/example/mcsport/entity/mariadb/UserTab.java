package org.example.mcsport.entity.mariadb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user")
public class UserTab {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "password", nullable = false)
    private String password;

    @ColumnDefault("'user'")
    @Column(name = "roles", length = 100)
    private String roles;

    @Column(name = "last_login_time")
    private Instant lastLoginTime;

    @Column(name = "name", nullable = false)
    private String name;

}