package com.accountservice.accountservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardid;

    private Long accountid;

    private String cardnumber;
    private String cardholdername;
    private String bankname;

    private LocalDate expiry;
    private String cvv;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private boolean locked = false;

    private boolean atmEnabled = true;
    private BigDecimal atmLimit = new BigDecimal("20000");

    private boolean onlineEnabled = true;
    private BigDecimal onlineLimit = new BigDecimal("50000");

    private boolean contactlessEnabled = true;
    private BigDecimal contactlessLimit = new BigDecimal("5000");

    public Card() {}

    @PrePersist
    public void prePersist() {
        if (status == null) status = Status.ACTIVE;

        if (bankname == null) bankname = "IG BANK";

        if (atmLimit == null) atmLimit = new BigDecimal("20000");
        if (onlineLimit == null) onlineLimit = new BigDecimal("50000");
        if (contactlessLimit == null) contactlessLimit = new BigDecimal("5000");
    }

    // getters/setters (keep your existing ones)
    public Long getCardid() { return cardid; }
    public void setCardid(Long cardid) { this.cardid = cardid; }

    public Long getAccountid() { return accountid; }
    public void setAccountid(Long accountid) { this.accountid = accountid; }

    public String getCardnumber() { return cardnumber; }
    public void setCardnumber(String cardnumber) { this.cardnumber = cardnumber; }

    public String getCardholdername() { return cardholdername; }
    public void setCardholdername(String cardholdername) { this.cardholdername = cardholdername; }

    public String getBankname() { return bankname; }
    public void setBankname(String bankname) { this.bankname = bankname; }

    public LocalDate getExpiry() { return expiry; }
    public void setExpiry(LocalDate expiry) { this.expiry = expiry; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public boolean isAtmEnabled() { return atmEnabled; }
    public void setAtmEnabled(boolean atmEnabled) { this.atmEnabled = atmEnabled; }

    public BigDecimal getAtmLimit() { return atmLimit; }
    public void setAtmLimit(BigDecimal atmLimit) { this.atmLimit = atmLimit; }

    public boolean isOnlineEnabled() { return onlineEnabled; }
    public void setOnlineEnabled(boolean onlineEnabled) { this.onlineEnabled = onlineEnabled; }

    public BigDecimal getOnlineLimit() { return onlineLimit; }
    public void setOnlineLimit(BigDecimal onlineLimit) { this.onlineLimit = onlineLimit; }

    public boolean isContactlessEnabled() { return contactlessEnabled; }
    public void setContactlessEnabled(boolean contactlessEnabled) { this.contactlessEnabled = contactlessEnabled; }

    public BigDecimal getContactlessLimit() { return contactlessLimit; }
    public void setContactlessLimit(BigDecimal contactlessLimit) { this.contactlessLimit = contactlessLimit; }
}
