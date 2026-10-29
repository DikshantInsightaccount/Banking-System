package com.accountservice.accountservice.repositories;



import com.accountservice.accountservice.entity.FixedDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {

    List<FixedDeposit> findByAccount_Accountid(Long accountid);
}