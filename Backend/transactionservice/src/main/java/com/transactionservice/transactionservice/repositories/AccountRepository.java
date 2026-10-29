package com.transactionservice.transactionservice.repositories;

import com.transactionservice.transactionservice.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository
        extends JpaRepository<Account, Long> {

    /*
     * Normal read-only account lookup.
     */
    Optional<Account> findByAccountnumber(
            String accountNumber
    );

    /*
     * MariaDB-compatible pessimistic row lock by account ID.
     *
     * This method must be called inside an active transaction.
     */
    @Query(
            value = """
                    SELECT *
                    FROM accounts
                    WHERE accountid = :accountId
                    FOR UPDATE
                    """,
            nativeQuery = true
    )
    Optional<Account> findByIdForUpdate(
            @Param("accountId")
            Long accountId
    );

    /*
     * MariaDB-compatible pessimistic row lock by
     * account number.
     *
     * This method must be called inside an active transaction.
     */
    @Query(
            value = """
                    SELECT *
                    FROM accounts
                    WHERE accountnumber = :accountNumber
                    FOR UPDATE
                    """,
            nativeQuery = true
    )
    Optional<Account> findByAccountnumberForUpdate(
            @Param("accountNumber")
            String accountNumber
    );

    /*
     * Resolves an account number to its account ID.
     *
     * TransactionService then locks both participating
     * accounts in ascending account-ID order.
     */
    @Query(
            value = """
                    SELECT accountid
                    FROM accounts
                    WHERE accountnumber = :accountNumber
                    """,
            nativeQuery = true
    )
    Optional<Long> findIdByAccountnumber(
            @Param("accountNumber")
            String accountNumber
    );
}