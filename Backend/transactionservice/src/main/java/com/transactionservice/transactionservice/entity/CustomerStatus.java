package com.transactionservice.transactionservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customerstatus")
public class CustomerStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusid")
    private Long statusid;

    @OneToOne
    @JoinColumn(name = "customerid", nullable = false, unique = true)
    private Customer customer;

    @Column(name = "accountactive", nullable = false)
    private Boolean accountactive = false;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "updatedat")
    private LocalDateTime updatedat;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedat = LocalDateTime.now();
    }

    public CustomerStatus() {
    }

    public CustomerStatus(Customer customer) {
        this.customer = customer;
        this.accountactive = false;
    }

    public Long getStatusid() {
        return statusid;
    }

    public void setStatusid(Long statusid) {
        this.statusid = statusid;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Boolean getAccountactive() {
        return accountactive;
    }

    public void setAccountactive(Boolean accountactive) {
        this.accountactive = accountactive;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getUpdatedat() {
        return updatedat;
    }

}
