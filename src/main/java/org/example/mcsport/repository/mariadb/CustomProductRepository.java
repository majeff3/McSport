package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.CustomProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomProductRepository extends JpaRepository<CustomProduct, String> {
}