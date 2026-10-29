package com.authservice.authservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txnid")
    private Long txnid;

    @ManyToOne
    @JoinColumn(name = "accountid", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "txntype", nullable = false)
    private TransactionType txntype;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "txntimestamp", updatable = false)
    private LocalDateTime txntimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.SUCCESS;

    @PrePersist
    public void prePersist() {
        if (this.txntimestamp == null) {
            this.txntimestamp = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = TransactionStatus.SUCCESS;
        }
    }

    // Constructors
    public Transaction() {
    }

    public Transaction(Account account, TransactionType txntype, BigDecimal amount) {
        this.account = account;
        this.txntype = txntype;
        this.amount = amount;
        this.status = TransactionStatus.SUCCESS;
    }

    // Getters & Setters
    public Long getTxnid() {
        return txnid;
    }

    public void setTxnid(Long txnid) {
        this.txnid = txnid;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public TransactionType getTxntype() {
        return txntype;
    }

    public void setTxntype(TransactionType txntype) {
        this.txntype = txntype;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTxntimestamp() {
        return txntimestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

}

