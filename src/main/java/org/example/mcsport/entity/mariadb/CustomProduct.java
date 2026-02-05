package org.example.mcsport.entity.mariadb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "custom_product")
public class CustomProduct {
    @Id
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Column(name = "size_template_id", length = 20)
    private String sizeTemplateId;

    @Column(name = "cloth_number", length = 50)
    private String clothNumber;

    @Column(name = "cloth_name", length = 500)
    private String clothName;

    @Column(name = "cloth_size", length = 20)
    private String clothSize;

    @Column(name = "pants_number", length = 50)
    private String pantsNumber;

    @Column(name = "pants_name", length = 500)
    private String pantsName;

    @Column(name = "pants_size", length = 20)
    private String pantsSize;

    @ColumnDefault("1")
    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @ColumnDefault("current_timestamp()")
    @Column(name = "update_date")
    private Instant updateDate;

    @ColumnDefault("current_timestamp()")
    @Column(name = "create_date")
    private Instant createDate;


}