package org.example.mcsport.entity.mariadb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "return_record")
public class ReturnRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id", nullable = false)
    private Long id;

    @Column(name = "sale_number", nullable = false, length = 50)
    private String saleNumber;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "salesperson", nullable = false, length = 100)
    private String salesperson;

    @Column(name = "return_date", nullable = false)
    private Instant returnDate;

    @Column(name = "warehousing_date", nullable = false)
    private Instant warehousingDate;

    @Column(name = "SKU_id", nullable = false, length = 50)
    private String skuId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "size", nullable = false, length = 50)
    private String size;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "return_reason", nullable = false, length = 200)
    private String returnReason;

    @Column(name = "quality_status", nullable = false, length = 50)
    private String qualityStatus;

    @Column(name = "shelves", nullable = false, length = 100)
    private String shelves;

    @Column(name = "final_result", nullable = false, length = 50)
    private String finalResult;

    @Column(name = "handler", nullable = false)
    private Long handler;

    @Column(name = "remark", length = 200)
    private String remark;

    @ColumnDefault("current_timestamp()")
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @ColumnDefault("current_timestamp()")
    @Column(name = "updated_date", nullable = false)
    private Instant updatedDate;


}