package org.example.mcsport.repository.sqlserver;

import org.example.mcsport.entity.sqlserver.SalesTab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesTabRepository extends JpaRepository<SalesTab, Long> {

    boolean existsBySalesOrderNumber(String result);

}