package com.authservice.authservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurringdeposits")
public class RecurringDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rdid")
    private Long rdid;

    @ManyToOne
    @JoinColumn(name = "accountid", nullable = false)
    private Account account;

    @Column(name = "monthlyamount", nullable = false)
    private BigDecimal monthlyamount;

    @Column(name = "tenuremonths", nullable = false)
    private Integer tenuremonths;

    @Column(name = "interestrate", nullable = false)
    private BigDecimal interestrate;

    @Column(name = "startdate")
    private LocalDate startdate;

    @Column(name = "maturitydate")
    private LocalDate maturitydate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DepositStatus status = DepositStatus.ACTIVE;

    @PrePersist
    public void prePersist() {
        if (this.startdate == null) {
            this.startdate = LocalDate.now();
        }
        if (this.status == null) {
            this.status = DepositStatus.ACTIVE;
        }
    }

    public RecurringDeposit() {
    }

    public RecurringDeposit(Account account,
                            BigDecimal monthlyamount,
                            Integer tenuremonths,
                            BigDecimal interestrate) {
        this.account = account;
        this.monthlyamount = monthlyamount;
        this.tenuremonths = tenuremonths;
        this.interestrate = interestrate;
        this.status = DepositStatus.ACTIVE;
    }

    public Long getRdid() {
        return rdid;
    }

    public void setRdid(Long rdid) {
        this.rdid = rdid;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getMonthlyamount() {
        return monthlyamount;
    }

    public void setMonthlyamount(BigDecimal monthlyamount) {
        this.monthlyamount = monthlyamount;
    }

    public Integer getTenuremonths() {
        return tenuremonths;
    }

    public void setTenuremonths(Integer tenuremonths) {
        this.tenuremonths = tenuremonths;
    }

    public BigDecimal getInterestrate() {
        return interestrate;
    }

    public void setInterestrate(BigDecimal interestrate) {
        this.interestrate = interestrate;
    }

    public LocalDate getStartdate() {
        return startdate;
    }

    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
    }

    public LocalDate getMaturitydate() {
        return maturitydate;
    }

    public void setMaturitydate(LocalDate maturitydate) {
        this.maturitydate = maturitydate;
    }

    public DepositStatus getStatus() {
        return status;
    }

    public void setStatus(DepositStatus status) {
        this.status = status;
    }

}
