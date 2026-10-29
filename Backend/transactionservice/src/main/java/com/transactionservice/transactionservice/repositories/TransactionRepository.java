package com.transactionservice.transactionservice.repositories;

import com.transactionservice.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_Accountid(Long accountid);

    List<Transaction> findByAccount_AccountidAndTxntimestampBetween(
            Long accountid,
            LocalDateTime from,
            LocalDateTime to
    );

    //  support statement by account number
    List<Transaction> findByAccount_AccountnumberAndTxntimestampBetween(
            String accountnumber,
            LocalDateTime from,
            LocalDateTime to
    );
}
