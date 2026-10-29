package com.accountservice.accountservice.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transferid")
    private Long transferid;

    @ManyToOne
    @JoinColumn(name = "fromaccountid", nullable = false)
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "toaccountid", nullable = false)
    private Account toAccount;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "txntimestamp", updatable = false)
    private LocalDateTime txntimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status = TransferStatus.SUCCESS;

    @PrePersist
    public void prePersist() {
        if (this.txntimestamp == null) {
            this.txntimestamp = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = TransferStatus.SUCCESS;
        }
    }


    public Transfer() {
    }

    public Transfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.status = TransferStatus.SUCCESS;
    }


    public Long getTransferid() {
        return transferid;
    }

    public void setTransferid(Long transferid) {
        this.transferid = transferid;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
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

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

}
