package com.bankingsystem.bankingsystem.repositories;

import com.bankingsystem.bankingsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser_Userid(Long userId);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByUser_Userid(Long userId);
}