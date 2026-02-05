package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.UserTab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTab, Long> {
    Optional<UserTab> findUserTabByName(String name);
}