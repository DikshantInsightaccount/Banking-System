package com.transactionservice.transactionservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fixeddeposits")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FixedDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fdid")
    private Long fdid;

    @ManyToOne
    @JoinColumn(name = "accountid", nullable = false)
    private Account account;

    @Column(name = "principalamount", nullable = false)
    private BigDecimal principalamount;

    @Column(name = "interestrate", nullable = false)
    private BigDecimal interestrate;

    @Column(name = "tenuremonths", nullable = false)
    private Integer tenuremonths;

    @Column(name = "maturityamount")
    private BigDecimal maturityamount;

    @Column(name = "startdate")
    private LocalDate startdate;

    @Column(name = "maturitydate")
    private LocalDate maturitydate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FixedDepositStatus status = FixedDepositStatus.ACTIVE;

    @PrePersist
    public void prePersist() {
        if (this.startdate == null) {
            this.startdate = LocalDate.now();
        }
        if (this.status == null) {
            this.status = FixedDepositStatus.ACTIVE;
        }
    }

    public FixedDeposit() {
    }

    public FixedDeposit(Account account,
                        BigDecimal principalamount,
                        BigDecimal interestrate,
                        Integer tenuremonths) {
        this.account = account;
        this.principalamount = principalamount;
        this.interestrate = interestrate;
        this.tenuremonths = tenuremonths;
        this.status = FixedDepositStatus.ACTIVE;
    }


    public Long getFdid() {
        return fdid;
    }

    public void setFdid(Long fdid) {
        this.fdid = fdid;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getPrincipalamount() {
        return principalamount;
    }

    public void setPrincipalamount(BigDecimal principalamount) {
        this.principalamount = principalamount;
    }

    public BigDecimal getInterestrate() {
        return interestrate;
    }

    public void setInterestrate(BigDecimal interestrate) {
        this.interestrate = interestrate;
    }

    public Integer getTenuremonths() {
        return tenuremonths;
    }

    public void setTenuremonths(Integer tenuremonths) {
        this.tenuremonths = tenuremonths;
    }

    public BigDecimal getMaturityamount() {
        return maturityamount;
    }

    public void setMaturityamount(BigDecimal maturityamount) {
        this.maturityamount = maturityamount;
    }

    public LocalDate getStartdate() {
        return startdate;
    }

    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
    }

    public FixedDepositStatus getStatus() {
        return status;
    }

    public void setStatus(FixedDepositStatus status) {
        this.status = status;
    }
}