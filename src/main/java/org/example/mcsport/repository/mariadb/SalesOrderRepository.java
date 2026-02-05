package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, String> {
}