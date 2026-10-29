package com.accountservice.accountservice.entity;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerid")
    private Long customerid;

    @OneToOne
    @JoinColumn(name = "userid", nullable = false, unique = true)
    private User user;

    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, unique = true, length = 15)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "kycstatus", nullable = false)
    private KycStatus kycstatus = KycStatus.PENDING;

    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @PrePersist
    public void prePersist() {
        if (this.createdat == null) {
            this.createdat = LocalDateTime.now();
        }
        if (this.kycstatus == null) {
            this.kycstatus = KycStatus.PENDING;
        }
    }

    public Customer() {
    }

    public Customer(User user, String fullname, String email, String phone) {
        this.user = user;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
    }

    public Long getCustomerid() {
        return customerid;
    }

    public void setCustomerid(Long customerid) {
        this.customerid = customerid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public KycStatus getKycstatus() {
        return kycstatus;
    }

    public void setKycstatus(KycStatus kycstatus) {
        this.kycstatus = kycstatus;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }

}