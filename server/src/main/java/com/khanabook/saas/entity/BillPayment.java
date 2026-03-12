package com.khanabook.saas.entity;

import com.khanabook.saas.sync.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bill_payments")
@Getter
@Setter
public class BillPayment extends BaseSyncEntity {

    @Column(name = "bill_id", nullable = true)
    private Integer billId;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "amount")
    private Double amount;
}
