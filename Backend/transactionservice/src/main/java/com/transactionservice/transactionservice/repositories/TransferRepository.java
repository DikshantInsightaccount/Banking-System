package com.transactionservice.transactionservice.repositories;

import com.transactionservice.transactionservice.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("""
        SELECT t FROM Transfer t
        WHERE (t.fromAccount.accountid = :accountId
            OR t.toAccount.accountid = :accountId)
        AND t.txntimestamp BETWEEN :from AND :to
    """)
    List<Transfer> findByAccountInEitherSide(
            @Param("accountId") Long accountId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    //  NEW: statement/lookup by account number
    @Query("""
        SELECT t FROM Transfer t
        WHERE (t.fromAccount.accountnumber = :accountNumber
            OR t.toAccount.accountnumber = :accountNumber)
        AND t.txntimestamp BETWEEN :from AND :to
    """)
    List<Transfer> findByAccountNumberInEitherSide(
            @Param("accountNumber") String accountNumber,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}