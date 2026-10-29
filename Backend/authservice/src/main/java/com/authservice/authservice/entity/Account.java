package com.authservice.authservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accountid")
    private Long accountid;

    @ManyToOne
    @JoinColumn(name = "customerid", nullable = false)
    private Customer customer;

    @Column(name = "accountnumber", nullable = false, unique = true, length = 20)
    private String accountnumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounttype", nullable = false)
    private AccountType accounttype;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.INACTIVE;

    @Column(name = "openedat", updatable = false)
    private LocalDateTime openedat;

    @PrePersist
    public void prePersist() {
        if (this.openedat == null) {
            this.openedat = LocalDateTime.now();
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = AccountStatus.INACTIVE;
        }
    }

    // Constructors
    public Account() {
    }

    public Account(Customer customer, String accountnumber, AccountType accounttype) {
        this.customer = customer;
        this.accountnumber = accountnumber;
        this.accounttype = accounttype;
        this.balance = BigDecimal.ZERO;
        this.status = AccountStatus.INACTIVE;
    }

    // Getters & Setters
    public Long getAccountid() {
        return accountid;
    }

    public void setAccountid(Long accountid) {
        this.accountid = accountid;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public AccountType getAccounttype() {
        return accounttype;
    }

    public void setAccounttype(AccountType accounttype) {
        this.accounttype = accounttype;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getOpenedat() {
        return openedat;
    }

}