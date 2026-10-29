package com.accountservice.accountservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeid")
    private Long employeeid;

    @OneToOne
    @JoinColumn(name = "userid", nullable = false, unique = true)
    private User user;

    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Column(name = "designation", length = 50)
    private String designation;

    @Column(name = "branch", length = 50)
    private String branch;

    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @PrePersist
    public void prePersist() {
        if (this.createdat == null) {
            this.createdat = LocalDateTime.now();
        }
    }

    public Employee() {
    }

    public Employee(User user, String fullname, String designation, String branch) {
        this.user = user;
        this.fullname = fullname;
        this.designation = designation;
        this.branch = branch;
    }

    public Long getEmployeeid() {
        return employeeid;
    }

    public void setEmployeeid(Long employeeid) {
        this.employeeid = employeeid;
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

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }

}