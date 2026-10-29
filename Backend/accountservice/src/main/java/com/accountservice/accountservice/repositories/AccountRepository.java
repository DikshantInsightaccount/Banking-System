package com.accountservice.accountservice.repositories;

import com.accountservice.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountnumber(String accountnumber);

    boolean existsByAccountnumber(String accountnumber);

    List<Account> findByCustomerid(Long customerid);
}