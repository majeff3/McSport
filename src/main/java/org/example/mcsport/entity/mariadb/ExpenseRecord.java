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
@Table(name = "expense_records")
public class ExpenseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "sales_order_id", length = 50)
    private String salesOrderId;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "expense_type", nullable = false, length = 100)
    private String expenseType;

    @Column(name = "expense_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal expenseAmount;

    @ColumnDefault("'MOP'")
    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "handler", nullable = false)
    private Long handler;

    @ColumnDefault("'pending'")
    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @ColumnDefault("current_timestamp()")
    @Column(name = "expense_date", nullable = false)
    private Instant expenseDate;

    @Lob
    @Column(name = "remarks")
    private String remarks;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @ColumnDefault("current_timestamp()")
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @ColumnDefault("current_timestamp()")
    @Column(name = "updated_date", nullable = false)
    private Instant updatedDate;

    @Column(name = "reviewer", length = 50)
    private String reviewer;

    @Column(name = "recorder", nullable = false)
    private Long recorder;

    @Column(name = "shipping_number")
    private String shippingNumber;

    @Column(name = "ship_company")
    private String shipCompany;

    @Column(name = "pdf_path")
    private String pdfPath;


}