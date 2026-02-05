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
@Table(name = "pre_order")
public class PreOrder {
    @Id
    @GeneratedValue // 這裡不需要指定策略
    @Generated(event = EventType.INSERT) // 告訴 Hibernate INSERT 後去資料庫重新讀取
    @Column(name = "order_id", nullable = false, length = 20)
    private String orderId;

    @Column(name = "pre_order_product", nullable = false, length = 50)
    private String preOrderProduct;

    @Column(name = "age_range", length = 20)
    private String ageRange;

    @ColumnDefault("'pending'")
    @Lob
    @Column(name = "review_status", nullable = false)
    private String reviewStatus;

    @ColumnDefault("current_timestamp()")
    @Column(name = "create_date")
    private Instant createDate;

    @ColumnDefault("current_timestamp()")
    @Column(name = "update_date")
    private Instant updateDate;
    @Column(name = "store_short_from", nullable = false, length = 10)
    private String storeShortFrom;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "customer_id", nullable = false, length = 20)
    private String customerId;


}