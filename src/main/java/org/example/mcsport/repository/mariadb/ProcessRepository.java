package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.Process;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRepository extends JpaRepository<Process, String> {
}