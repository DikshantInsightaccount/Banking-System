package com.accountservice.accountservice.services;

import com.accountservice.accountservice.clients.AuthFeignClient;
import com.accountservice.accountservice.dto.VerifyPasswordRequest;
import com.accountservice.accountservice.dto.VerifyPasswordResponse;
import com.accountservice.accountservice.entity.*;
import com.accountservice.accountservice.exceptions.AccountNotFoundException;
import com.accountservice.accountservice.exceptions.AccountResourceException;
import com.accountservice.accountservice.exceptions.DepositNotFoundException;
import com.accountservice.accountservice.exceptions.InsufficientBalanceException;
import com.accountservice.accountservice.repositories.AccountRepository;
import com.accountservice.accountservice.repositories.FixedDepositRepository;
import com.accountservice.accountservice.repositories.RecurringDepositRepository;
import com.accountservice.accountservice.repositories.IdempotencyKeyRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DepositService {

    private final AuthFeignClient authClient;

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    private final AccountRepository accountRepository;
    private final FixedDepositRepository fixedDepositRepository;
    private final RecurringDepositRepository recurringDepositRepository;

    public DepositService(AccountRepository accountRepository,
                          FixedDepositRepository fixedDepositRepository,
                          RecurringDepositRepository recurringDepositRepository,
                          AuthFeignClient authClient,
                          IdempotencyKeyRepository idempotencyKeyRepository) {

        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.accountRepository = accountRepository;
        this.fixedDepositRepository = fixedDepositRepository;
        this.recurringDepositRepository = recurringDepositRepository;
        this.authClient = authClient;
    }

    // ---------------- FIXED DEPOSIT ----------------

    @Transactional
    public FixedDeposit createFixedDepositWithPassword(
            Long accountId,
            Long userid,
            String password,
            String idempotencyKey,
            FixedDeposit fd
    ) {

        VerifyPasswordRequest request =
                new VerifyPasswordRequest();

        request.setUserId(userid);
        request.setPassword(password);

        VerifyPasswordResponse response =
                authClient.verifyPassword(
                        request
                );

        if (response == null ||
                !response.isValid()) {

            throw new RuntimeException(
                    "Invalid password"
            );
        }

        if (idempotencyKey == null ||
                idempotencyKey.isBlank()) {

            throw new RuntimeException(
                    "Idempotency key is required"
            );
        }

        Optional<IdempotencyKey>
                existing =
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                idempotencyKey
                        );

        if (existing.isPresent()) {

            return fixedDepositRepository
                    .findById(
                            existing
                                    .get()
                                    .getReferenceId()
                    )
                    .orElseThrow(
                            () ->
                                    new RuntimeException(
                                            "Original FD not found"
                                    )
                    );
        }

        FixedDeposit result =
                createFixedDeposit(
                        accountId,
                        fd
                );

        IdempotencyKey key =
                new IdempotencyKey();

        key.setIdempotencyKey(
                idempotencyKey
        );

        key.setOperationType(
                "FD_CREATE"
        );

        key.setReferenceId(
                result.getFdid()
        );

        idempotencyKeyRepository
                .saveAndFlush(
                        key
                );

        return result;
    }


    @Transactional
    public FixedDeposit createFixedDeposit(Long accountId, FixedDeposit fd) {

        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found"));
        System.out.println(
                "RECEIVED FD = " + fd
        );

        System.out.println(
                "Principal = " + fd.getPrincipalamount()
        );

        BigDecimal principal =
                requirePositive(fd.getPrincipalamount(),
                        "Principal amount");

        ensureSufficientBalance(acc, principal);

        acc.setBalance(
                safe(acc.getBalance()).subtract(principal)
        );

        accountRepository.save(acc);

        if (fd.getStartdate() == null) {
            fd.setStartdate(LocalDate.now());
        }

        if (fd.getMaturitydate() == null &&
                fd.getTenuremonths() != null) {

            fd.setMaturitydate(
                    fd.getStartdate()
                            .plusMonths(fd.getTenuremonths())
            );
        }

        fd.setAccount(acc);

        return fixedDepositRepository.save(fd);
    }

    public List<FixedDeposit> getFixedDeposits(Long accountId) {

        return fixedDepositRepository
                .findByAccount_Accountid(accountId);
    }

    public FixedDeposit getFixedDeposit(Long fdId) {

        return fixedDepositRepository.findById(fdId)
                .orElseThrow(() ->
                        new DepositNotFoundException(
                                "Fixed deposit not found"
                        ));
    }

    @Transactional
    public FixedDeposit updateFixedDeposit(
            Long accountId,
            Long fdId,
            FixedDeposit updated
    ) {

        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found"
                        ));

        FixedDeposit existing =
                fixedDepositRepository.findById(fdId)
                        .orElseThrow(() ->
                                new DepositNotFoundException(
                                        "Fixed deposit not found"
                                ));

        if (existing.getAccount() == null ||
                existing.getAccount().getAccountid() == null ||
                !existing.getAccount()
                        .getAccountid()
                        .equals(accountId)) {

            throw new AccountResourceException(
                    "This FD does not belong to the given account"
            );
        }

        BigDecimal oldPrincipal =
                safe(existing.getPrincipalamount());

        BigDecimal newPrincipal =
                (updated.getPrincipalamount() != null)
                        ? requirePositive(
                        updated.getPrincipalamount(),
                        "Principal amount"
                )
                        : oldPrincipal;

        BigDecimal diff =
                newPrincipal.subtract(oldPrincipal);

        if (diff.compareTo(BigDecimal.ZERO) > 0) {

            ensureSufficientBalance(acc, diff);

            acc.setBalance(
                    safe(acc.getBalance()).subtract(diff)
            );

        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {

            acc.setBalance(
                    safe(acc.getBalance()).add(diff.abs())
            );
        }

        accountRepository.save(acc);

        if (updated.getPrincipalamount() != null)
            existing.setPrincipalamount(
                    updated.getPrincipalamount()
            );

        if (updated.getInterestrate() != null)
            existing.setInterestrate(
                    updated.getInterestrate()
            );

        if (updated.getTenuremonths() != null)
            existing.setTenuremonths(
                    updated.getTenuremonths()
            );

        if (updated.getMaturityamount() != null)
            existing.setMaturityamount(
                    updated.getMaturityamount()
            );

        if (updated.getStartdate() != null)
            existing.setStartdate(
                    updated.getStartdate()
            );

        if (updated.getMaturitydate() != null)
            existing.setMaturitydate(
                    updated.getMaturitydate()
            );

        if (updated.getStatus() != null)
            existing.setStatus(
                    updated.getStatus()
            );

        return fixedDepositRepository.save(existing);
    }

    // ---------------- RECURRING DEPOSIT ----------------

    @Transactional
    public RecurringDeposit createRecurringDepositWithPassword(
            Long accountId,
            Long userId,
            String password,
            String idempotencyKey,
            RecurringDeposit rd
    ) {

        if (userId == null) {
            throw new AccountResourceException(
                    "User ID is required"
            );
        }

        if (password == null || password.isBlank()) {
            throw new AccountResourceException(
                    "Password is required"
            );
        }

        if (idempotencyKey == null ||
                idempotencyKey.isBlank()) {

            throw new AccountResourceException(
                    "Idempotency key is required"
            );
        }

        if (rd == null) {
            throw new AccountResourceException(
                    "Recurring deposit details are required"
            );
        }

        VerifyPasswordRequest request =
                new VerifyPasswordRequest();

        request.setUserId(userId);
        request.setPassword(password);

        VerifyPasswordResponse response =
                authClient.verifyPassword(request);

        if (response == null ||
                !response.isValid()) {

            throw new AccountResourceException(
                    "Invalid password"
            );
        }

        String normalizedKey =
                idempotencyKey.trim();

        Optional<IdempotencyKey> existingRecord =
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                normalizedKey
                        );

        if (existingRecord.isPresent()) {

            IdempotencyKey existing =
                    existingRecord.get();

            if (!"RD_CREATE".equals(
                    existing.getOperationType()
            )) {
                throw new AccountResourceException(
                        "Idempotency key was already used for another operation"
                );
            }

            return recurringDepositRepository
                    .findById(
                            existing.getReferenceId()
                    )
                    .orElseThrow(() ->
                            new DepositNotFoundException(
                                    "Original recurring deposit was not found"
                            )
                    );
        }

        RecurringDeposit result =
                createRecurringDeposit(
                        accountId,
                        rd
                );

        IdempotencyKey record =
                new IdempotencyKey();

        record.setIdempotencyKey(
                normalizedKey
        );

        record.setOperationType(
                "RD_CREATE"
        );

        record.setReferenceId(
                result.getRdid()
        );

        idempotencyKeyRepository.saveAndFlush(
                record
        );

        return result;
    }

    @Transactional
    public RecurringDeposit createRecurringDeposit(
            Long accountId,
            RecurringDeposit rd
    ) {

        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found"
                        ));

        BigDecimal monthly =
                requirePositive(
                        rd.getMonthlyamount(),
                        "Monthly amount"
                );

        ensureSufficientBalance(acc, monthly);

        acc.setBalance(
                safe(acc.getBalance()).subtract(monthly)
        );

        accountRepository.save(acc);

        LocalDate start =
                (rd.getStartdate() != null)
                        ? rd.getStartdate()
                        : LocalDate.now();

        rd.setPaidmonths(1);
        rd.setLastpaiddate(start);
        rd.setNextduedate(start.plusMonths(1));

        rd.setAccount(acc);

        return recurringDepositRepository.save(rd);
    }

    public List<RecurringDeposit> getRecurringDeposits(
            Long accountId
    ) {

        return recurringDepositRepository
                .findByAccount_Accountid(accountId);
    }

    public RecurringDeposit getRecurringDeposit(Long rdId) {

        return recurringDepositRepository.findById(rdId)
                .orElseThrow(() ->
                        new DepositNotFoundException(
                                "Recurring deposit not found"
                        ));
    }

    @Transactional
    public RecurringDeposit updateRecurringDeposit(
            Long accountId,
            Long rdId,
            RecurringDeposit updated
    ) {

        RecurringDeposit existing =
                recurringDepositRepository.findById(rdId)
                        .orElseThrow(() ->
                                new DepositNotFoundException(
                                        "Recurring deposit not found"
                                ));

        if (existing.getAccount() == null ||
                existing.getAccount().getAccountid() == null ||
                !existing.getAccount()
                        .getAccountid()
                        .equals(accountId)) {

            throw new AccountResourceException(
                    "This RD does not belong to the given account"
            );
        }

        if (updated.getMonthlyamount() != null) {

            existing.setMonthlyamount(
                    requirePositive(
                            updated.getMonthlyamount(),
                            "Monthly amount"
                    )
            );
        }

        if (updated.getInterestrate() != null)
            existing.setInterestrate(
                    updated.getInterestrate()
            );

        if (updated.getTenuremonths() != null)
            existing.setTenuremonths(
                    updated.getTenuremonths()
            );

        if (updated.getStartdate() != null)
            existing.setStartdate(
                    updated.getStartdate()
            );

        if (updated.getMaturitydate() != null)
            existing.setMaturitydate(
                    updated.getMaturitydate()
            );

        if (updated.getStatus() != null)
            existing.setStatus(
                    updated.getStatus()
            );

        return recurringDepositRepository.save(existing);
    }

    @Transactional
    public RecurringDeposit payRecurringInstallment(
            Long rdId,
            Long userId,
            String password,
            LocalDate requestedDueDate
    ) {

        if (rdId == null) {
            throw new AccountResourceException(
                    "Recurring deposit ID is required"
            );
        }

        if (userId == null) {
            throw new AccountResourceException(
                    "User ID is required"
            );
        }

        if (password == null ||
                password.isBlank()) {

            throw new AccountResourceException(
                    "Password is required"
            );
        }

        if (requestedDueDate == null) {
            throw new AccountResourceException(
                    "Installment due date is required"
            );
        }

        /*
         * One deterministic key represents one RD installment.
         * Retrying the same due date generates the same key.
         */
        String idempotencyKey =
                "RD_INSTALLMENT:" +
                        rdId +
                        ":" +
                        requestedDueDate;

        Optional<IdempotencyKey> existingRecord =
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                idempotencyKey
                        );

        /*
         * The installment was already processed.
         * Return the RD without deducting balance again.
         */
        if (existingRecord.isPresent()) {

            IdempotencyKey existing =
                    existingRecord.get();

            if (!"RD_INSTALLMENT".equals(
                    existing.getOperationType()
            )) {
                throw new AccountResourceException(
                        "Idempotency key was already used for another operation"
                );
            }

            return recurringDepositRepository
                    .findById(
                            existing.getReferenceId()
                    )
                    .orElseThrow(() ->
                            new DepositNotFoundException(
                                    "Recurring deposit for the original installment was not found"
                            )
                    );
        }

        /*
         * Verify sensitive financial action.
         */
        VerifyPasswordRequest verifyRequest =
                new VerifyPasswordRequest();

        verifyRequest.setUserId(userId);
        verifyRequest.setPassword(password);

        VerifyPasswordResponse verifyResponse =
                authClient.verifyPassword(
                        verifyRequest
                );

        if (verifyResponse == null ||
                !verifyResponse.isValid()) {

            throw new AccountResourceException(
                    "Invalid password"
            );
        }

        /*
         * Lock the RD before changing paid months,
         * due date, and account balance.
         */
        RecurringDeposit rd =
                recurringDepositRepository
                        .findByIdForUpdate(rdId)
                        .orElseThrow(() ->
                                new DepositNotFoundException(
                                        "Recurring deposit not found"
                                )
                        );

        if (rd.getStatus() == null ||
                rd.getStatus() != DepositStatus.ACTIVE) {

            throw new AccountResourceException(
                    "Recurring deposit is not active"
            );
        }

        if (rd.getNextduedate() == null) {
            throw new AccountResourceException(
                    "Recurring deposit due date is unavailable"
            );
        }

        /*
         * The requested installment must match the currently
         * expected installment unless this is a replay.
         *
         * Replays have already been handled above.
         */
        if (!rd.getNextduedate()
                .equals(requestedDueDate)) {

            throw new AccountResourceException(
                    "The requested due date does not match the next installment due date"
            );
        }

        int paidMonths =
                rd.getPaidmonths() == null
                        ? 0
                        : rd.getPaidmonths();

        int tenureMonths =
                rd.getTenuremonths() == null
                        ? 0
                        : rd.getTenuremonths();

        if (tenureMonths <= 0) {
            throw new AccountResourceException(
                    "Invalid recurring deposit tenure"
            );
        }

        if (paidMonths >= tenureMonths) {
            throw new AccountResourceException(
                    "All recurring deposit installments are already paid"
            );
        }

        Account account =
                rd.getAccount();

        if (account == null ||
                account.getAccountid() == null) {

            throw new AccountNotFoundException(
                    "Account not found"
            );
        }

        BigDecimal monthlyAmount =
                requirePositive(
                        rd.getMonthlyamount(),
                        "Monthly amount"
                );

        ensureSufficientBalance(
                account,
                monthlyAmount
        );

        /*
         * Deduct exactly once.
         */
        account.setBalance(
                safe(account.getBalance())
                        .subtract(monthlyAmount)
        );

        accountRepository.save(account);

        LocalDate paymentDate =
                LocalDate.now();

        int updatedPaidMonths =
                paidMonths + 1;

        rd.setPaidmonths(
                updatedPaidMonths
        );

        rd.setLastpaiddate(
                paymentDate
        );

        /*
         * Advance from the installment due date instead of
         * paymentDate. This avoids schedule drift.
         */
        if (updatedPaidMonths >= tenureMonths) {

            /*
             * Replace MATURED with the actual completed status
             * name in your DepositStatus enum if it differs.
             */
            rd.setStatus(
                    DepositStatus.MATURED
            );

            rd.setNextduedate(null);

        } else {

            rd.setNextduedate(
                    requestedDueDate.plusMonths(1)
            );
        }

        RecurringDeposit result =
                recurringDepositRepository.save(rd);

        IdempotencyKey record =
                new IdempotencyKey();

        record.setIdempotencyKey(
                idempotencyKey
        );

        record.setOperationType(
                "RD_INSTALLMENT"
        );

        /*
         * For now, the reference points to the RD.
         * Later, when you add an installment-payment journal,
         * this should point to the payment record instead.
         */
        record.setReferenceId(
                result.getRdid()
        );

        idempotencyKeyRepository.saveAndFlush(
                record
        );

        return result;
    }

    // ---------------- helpers ----------------

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal requirePositive(
            BigDecimal v,
            String field
    ) {

        if (v == null ||
                v.compareTo(BigDecimal.ZERO) <= 0) {

            throw new AccountResourceException(
                    field + " must be greater than 0"
            );
        }

        return v;
    }

    private void ensureSufficientBalance(
            Account acc,
            BigDecimal amount
    ) {

        if (safe(acc.getBalance())
                .compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }
    }
}