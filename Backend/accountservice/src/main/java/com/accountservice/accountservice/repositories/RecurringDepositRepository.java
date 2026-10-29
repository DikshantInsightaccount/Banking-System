package com.accountservice.accountservice.repositories;

import com.accountservice.accountservice.entity.DepositStatus;
import com.accountservice.accountservice.entity.RecurringDeposit;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringDepositRepository
        extends JpaRepository<RecurringDeposit, Long> {

    List<RecurringDeposit> findByAccount_Accountid(
            Long accountId
    );

    /*
     * Used by the recurring-deposit scheduler to find
     * active RDs whose installment due date has arrived.
     */
    List<RecurringDeposit>
    findByStatusAndNextduedateLessThanEqual(
            DepositStatus status,
            LocalDate dueDate
    );

    /*
     * Used during a manual installment payment to prevent
     * concurrent requests from updating the same RD.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT rd
            FROM RecurringDeposit rd
            JOIN FETCH rd.account
            WHERE rd.rdid = :rdId
            """)
    Optional<RecurringDeposit> findByIdForUpdate(
            @Param("rdId") Long rdId
    );
}