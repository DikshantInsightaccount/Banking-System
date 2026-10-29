package com.accountservice.accountservice.dto;

import java.math.BigDecimal;

public class CardControlsRequest {
    private boolean atmEnabled;
    private BigDecimal atmLimit;

    private boolean onlineEnabled;
    private BigDecimal onlineLimit;

    private boolean contactlessEnabled;
    private BigDecimal contactlessLimit;

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