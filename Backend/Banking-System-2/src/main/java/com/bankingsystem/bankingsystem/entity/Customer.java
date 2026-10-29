package com.bankingsystem.bankingsystem.entity;

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
    private KycStatus kycstatus = KycStatus.PENDING;  // PENDING/VERIFIED/REJECTED [2](https://insightgloballlc.sharepoint.com/sites/msd/IandI/Advance%20Services%20Intake/PortalTempUploads/30318446-d416-4892-9e21-157a0a71b12f__Customer%20Journey%20-%20Amazon%20Gabby%20Lizzul.pdf?web=1)

    // ✅ NEW COLUMNS (you said you already added in DB)
    @Column(name = "idprooftype", length = 50)
    private String idprooftype;

    @Column(name = "idproofnumber", length = 100)
    private String idproofnumber;

    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @PrePersist
    public void prePersist() {
        if (this.createdat == null) this.createdat = LocalDateTime.now();
        if (this.kycstatus == null) this.kycstatus = KycStatus.PENDING;
    }

    public Customer() {
        // Default constructor required by JPA/Hibernate for entity instantiation
    }

    // ---- getters/setters ----

    public Long getCustomerid() { return customerid; }
    public void setCustomerid(Long customerid) { this.customerid = customerid; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public KycStatus getKycstatus() { return kycstatus; }
    public void setKycstatus(KycStatus kycstatus) { this.kycstatus = kycstatus; }

    // ✅ NEW getters/setters
    public String getIdprooftype() { return idprooftype; }
    public void setIdprooftype(String idprooftype) { this.idprooftype = idprooftype; }

    public String getIdproofnumber() { return idproofnumber; }
    public void setIdproofnumber(String idproofnumber) { this.idproofnumber = idproofnumber; }

    public LocalDateTime getCreatedat() { return createdat; }
    public void setCreatedat(LocalDateTime createdat) { this.createdat = createdat; }
}