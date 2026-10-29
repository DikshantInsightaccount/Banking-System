package com.bankingsystem.bankingsystem.repositories;


import com.bankingsystem.bankingsystem.entity.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerStatusRepository extends JpaRepository<CustomerStatus, Long> {

    Optional<CustomerStatus> findByCustomer_Customerid(Long customerId);
}