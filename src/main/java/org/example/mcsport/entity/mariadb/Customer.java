package org.example.mcsport.entity.mariadb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue // 這裡不需要指定策略
    @Generated(event = EventType.INSERT) // 告訴 Hibernate INSERT 後去資料庫重新讀取
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Column(name = "customer_name", length = 500)
    private String customerName;

    @Column(name = "customer_po_number", length = 50)
    private String customerPoNumber;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "customer_type", length = 50)
    private String customerType;

    @Column(name = "industry", length = 50)
    private String industry;

    @ColumnDefault("current_timestamp()")
    @Column(name = "update_date")
    private Instant updateDate;

    @ColumnDefault("current_timestamp()")
    @Column(name = "create_date")
    private Instant createDate;


}