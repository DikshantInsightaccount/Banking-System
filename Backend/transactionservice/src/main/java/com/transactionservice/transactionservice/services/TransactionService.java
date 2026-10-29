package com.transactionservice.transactionservice.services;

import com.transactionservice.transactionservice.clients.AuthClient;
import com.transactionservice.transactionservice.dto.StatementItem;
import com.transactionservice.transactionservice.dto.TransferRequest;
import com.transactionservice.transactionservice.dto.VerifyPasswordRequest;
import com.transactionservice.transactionservice.dto.VerifyPasswordResponse;
import com.transactionservice.transactionservice.entity.Account;
import com.transactionservice.transactionservice.entity.AccountStatus;
import com.transactionservice.transactionservice.entity.IdempotencyKey;
import com.transactionservice.transactionservice.entity.Transaction;
import com.transactionservice.transactionservice.entity.TransactionStatus;
import com.transactionservice.transactionservice.entity.TransactionType;
import com.transactionservice.transactionservice.entity.Transfer;
import com.transactionservice.transactionservice.entity.TransferStatus;
import com.transactionservice.transactionservice.exceptions.AccountInactiveException;
import com.transactionservice.transactionservice.exceptions.AccountNotFoundException;
import com.transactionservice.transactionservice.exceptions.InsufficientBalanceException;
import com.transactionservice.transactionservice.exceptions.InvalidAmountException;
import com.transactionservice.transactionservice.exceptions.InvalidTransferException;
import com.transactionservice.transactionservice.exceptions.TransactionAlreadyExistsException;
import com.transactionservice.transactionservice.exceptions.TransactionNotFoundException;
import com.transactionservice.transactionservice.repositories.AccountRepository;
import com.transactionservice.transactionservice.repositories.IdempotencyKeyRepository;
import com.transactionservice.transactionservice.repositories.TransactionRepository;
import com.transactionservice.transactionservice.repositories.TransferRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transactionservice.transactionservice.kafka.TransferEventPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private static final String TRANSFER_OPERATION =
            "TRANSFER";

    private final AuthClient authClient;

    private final IdempotencyKeyRepository
            idempotencyKeyRepository;

    private final TransactionRepository
            transactionRepository;

    private final TransferRepository
            transferRepository;

    private final AccountRepository
            accountRepository;
    private final TransferEventPublisher transferEventPublisher;
    public TransactionService(
            AuthClient authClient,
            IdempotencyKeyRepository idempotencyKeyRepository,
            TransactionRepository transactionRepository,
            TransferRepository transferRepository,
            AccountRepository accountRepository,
            TransferEventPublisher transferEventPublisher
    ) {

        this.authClient =
                authClient;

        this.idempotencyKeyRepository =
                idempotencyKeyRepository;

        this.transactionRepository =
                transactionRepository;

        this.transferRepository =
                transferRepository;

        this.accountRepository =
                accountRepository;
        this.transferEventPublisher = transferEventPublisher;
    }

    // =====================================================
    // PASSWORD VERIFICATION
    // =====================================================

    private void verifyPassword(
            Long userId,
            String password
    ) {

        if (userId == null) {
            throw new InvalidTransferException(
                    "User ID is required"
            );
        }

        if (password == null ||
                password.isBlank()) {

            throw new InvalidTransferException(
                    "Password is required"
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

            throw new InvalidTransferException(
                    "Invalid password"
            );
        }
    }

    // =====================================================
    // SECURE AND IDEMPOTENT TRANSFER ENTRY POINT
    // =====================================================

    @Transactional
    public Transfer secureTransfer(
            TransferRequest request
    ) {

        if (request == null) {
            throw new InvalidTransferException(
                    "Transfer request is required"
            );
        }

        String idempotencyKey =
                normalizeIdempotencyKey(
                        request.getIdempotencyKey()
                );

        /*
         * Authenticate the request even when it is a replay.
         * A valid idempotency key must not bypass password
         * verification.
         */
        verifyPassword(
                request.getUserId(),
                request.getPassword()
        );

        Optional<IdempotencyKey> existingRecord =
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                idempotencyKey
                        );

        if (existingRecord.isPresent()) {
            return getExistingTransfer(
                    existingRecord.get()
            );
        }

        Transfer result;

        boolean accountNumbersProvided =
                request.getFromAccountNumber() != null &&
                        request.getToAccountNumber() != null &&
                        !request.getFromAccountNumber().isBlank() &&
                        !request.getToAccountNumber().isBlank();

        if (accountNumbersProvided) {

            result = transferByAccountNumbers(
                    request
                            .getFromAccountNumber()
                            .trim(),

                    request
                            .getToAccountNumber()
                            .trim(),

                    request.getAmount()
            );

        } else {

            result = transferByAccountIds(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount()
            );
        }

        saveTransferIdempotencyRecord(
                idempotencyKey,
                result.getTransferid()
        );

        /*
         * Publish the internal transfer event.
         *
         * The Kafka producer listens with AFTER_COMMIT, so the
         * event reaches Kafka only after the database transaction
         * commits successfully.
         */
        transferEventPublisher
                .publishTransferCompleted(
                        result
                );

        return result;
    }

    private String normalizeIdempotencyKey(
            String idempotencyKey
    ) {

        if (idempotencyKey == null ||
                idempotencyKey.isBlank()) {

            throw new InvalidTransferException(
                    "Idempotency key is required"
            );
        }

        return idempotencyKey.trim();
    }

    private Transfer getExistingTransfer(
            IdempotencyKey record
    ) {

        if (!TRANSFER_OPERATION.equals(
                record.getOperationType()
        )) {

            throw new InvalidTransferException(
                    "Idempotency key was already used for another operation"
            );
        }

        return transferRepository
                .findById(
                        record.getReferenceId()
                )
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Original transfer for idempotency key was not found"
                        )
                );
    }

    private void saveTransferIdempotencyRecord(
            String idempotencyKey,
            Long transferId
    ) {

        IdempotencyKey record =
                new IdempotencyKey();

        record.setIdempotencyKey(
                idempotencyKey
        );

        record.setOperationType(
                TRANSFER_OPERATION
        );

        record.setReferenceId(
                transferId
        );

        try {
            idempotencyKeyRepository
                    .saveAndFlush(record);

        } catch (DataIntegrityViolationException exception) {

            /*
             * A concurrent request may have inserted the same
             * idempotency key first. Throwing the exception
             * causes this entire transaction to roll back,
             * including account balance modifications.
             */
            throw new InvalidTransferException(
                    "A transfer with this idempotency key is already being processed"
            );
        }
    }

    // =====================================================
    // TRANSACTION READ AND LEGACY CRUD OPERATIONS
    // =====================================================

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getOneTransaction(
            Long id
    ) {

        return transactionRepository
                .findById(id)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction with id " +
                                        id +
                                        " not found"
                        )
                );
    }

    public Transaction save(
            Transaction transaction
    ) {

        if (transaction.getTxnid() != null &&
                transactionRepository.existsById(
                        transaction.getTxnid()
                )) {

            throw new TransactionAlreadyExistsException(
                    "Transaction already exists in database"
            );
        }

        return transactionRepository.save(
                transaction
        );
    }

    public Transaction updateTransaction(
            Transaction transaction
    ) {

        if (transaction.getTxnid() == null ||
                !transactionRepository.existsById(
                        transaction.getTxnid()
                )) {

            throw new TransactionNotFoundException(
                    "Transaction not found"
            );
        }

        return transactionRepository.save(
                transaction
        );
    }

    public void deleteTransaction(
            Long id
    ) {

        if (!transactionRepository.existsById(id)) {
            throw new TransactionNotFoundException(
                    "Transaction not found"
            );
        }

        transactionRepository.deleteById(id);
    }

    public Transaction partialUpdate(
            Long id,
            Transaction partialTransaction
    ) {

        Transaction targetTransaction =
                transactionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found"
                                )
                        );

        if (partialTransaction.getTxntype() != null) {
            targetTransaction.setTxntype(
                    partialTransaction.getTxntype()
            );
        }

        if (partialTransaction.getAmount() != null) {
            targetTransaction.setAmount(
                    partialTransaction.getAmount()
            );
        }

        if (partialTransaction.getStatus() != null) {
            targetTransaction.setStatus(
                    partialTransaction.getStatus()
            );
        }

        if (partialTransaction.getAccount() != null) {
            targetTransaction.setAccount(
                    partialTransaction.getAccount()
            );
        }

        return transactionRepository.save(
                targetTransaction
        );
    }

    // =====================================================
    // DEPOSIT WITH ACCOUNT ROW LOCKING
    // =====================================================

    @Transactional
    public Transaction deposit(
            Long accountId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        if (accountId == null) {
            throw new AccountNotFoundException(
                    "Account ID is required"
            );
        }

        Account account =
                accountRepository
                        .findByIdForUpdate(accountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        return executeDeposit(
                account,
                amount
        );
    }

    @Transactional
    public Transaction deposit(
            String accountNumber,
            BigDecimal amount
    ) {

        validateAmount(amount);

        if (accountNumber == null ||
                accountNumber.isBlank()) {

            throw new AccountNotFoundException(
                    "Account number is required"
            );
        }

        Account account =
                accountRepository
                        .findByAccountnumberForUpdate(
                                accountNumber.trim()
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        return executeDeposit(
                account,
                amount
        );
    }

    private Transaction executeDeposit(
            Account account,
            BigDecimal amount
    ) {

        validateAccountActive(account);

        BigDecimal currentBalance =
                safeBalance(account);

        account.setBalance(
                currentBalance.add(amount)
        );

        accountRepository.save(account);

        Transaction transaction =
                new Transaction(
                        account,
                        TransactionType.DEPOSIT,
                        amount
                );

        transaction.setStatus(
                TransactionStatus.SUCCESS
        );

        return transactionRepository.save(
                transaction
        );
    }

    // =====================================================
    // WITHDRAWAL WITH ACCOUNT ROW LOCKING
    // =====================================================

    @Transactional
    public Transaction withdraw(
            Long accountId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        if (accountId == null) {
            throw new AccountNotFoundException(
                    "Account ID is required"
            );
        }

        Account account =
                accountRepository
                        .findByIdForUpdate(accountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        return executeWithdrawal(
                account,
                amount
        );
    }

    @Transactional
    public Transaction withdraw(
            String accountNumber,
            BigDecimal amount
    ) {

        validateAmount(amount);

        if (accountNumber == null ||
                accountNumber.isBlank()) {

            throw new AccountNotFoundException(
                    "Account number is required"
            );
        }

        Account account =
                accountRepository
                        .findByAccountnumberForUpdate(
                                accountNumber.trim()
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        return executeWithdrawal(
                account,
                amount
        );
    }

    private Transaction executeWithdrawal(
            Account account,
            BigDecimal amount
    ) {

        validateAccountActive(account);

        BigDecimal currentBalance =
                safeBalance(account);

        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }

        account.setBalance(
                currentBalance.subtract(amount)
        );

        accountRepository.save(account);

        Transaction transaction =
                new Transaction(
                        account,
                        TransactionType.WITHDRAW,
                        amount
                );

        transaction.setStatus(
                TransactionStatus.SUCCESS
        );

        return transactionRepository.save(
                transaction
        );
    }

    // =====================================================
    // TRANSFER WITH DETERMINISTIC ACCOUNT LOCKING
    // =====================================================

    private Transfer transferByAccountIds(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        if (fromAccountId == null ||
                toAccountId == null) {

            throw new InvalidTransferException(
                    "Source and destination account IDs are required"
            );
        }

        if (fromAccountId.equals(toAccountId)) {

            throw new InvalidTransferException(
                    "Source and destination accounts cannot be the same"
            );
        }

        Long firstLockId =
                Math.min(
                        fromAccountId,
                        toAccountId
                );

        Long secondLockId =
                Math.max(
                        fromAccountId,
                        toAccountId
                );

        Account firstLockedAccount =
                accountRepository
                        .findByIdForUpdate(firstLockId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        Account secondLockedAccount =
                accountRepository
                        .findByIdForUpdate(secondLockId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        Account fromAccount =
                firstLockedAccount
                        .getAccountid()
                        .equals(fromAccountId)
                        ? firstLockedAccount
                        : secondLockedAccount;

        Account toAccount =
                firstLockedAccount
                        .getAccountid()
                        .equals(toAccountId)
                        ? firstLockedAccount
                        : secondLockedAccount;

        return executeTransfer(
                fromAccount,
                toAccount,
                amount
        );
    }

    private Transfer transferByAccountNumbers(
            String fromAccountNumber,
            String toAccountNumber,
            BigDecimal amount
    ) {

        validateAmount(amount);

        if (fromAccountNumber == null ||
                fromAccountNumber.isBlank() ||
                toAccountNumber == null ||
                toAccountNumber.isBlank()) {

            throw new InvalidTransferException(
                    "Account numbers are required"
            );
        }

        String normalizedFrom =
                fromAccountNumber.trim();

        String normalizedTo =
                toAccountNumber.trim();

        if (normalizedFrom.equals(normalizedTo)) {
            throw new InvalidTransferException(
                    "Source and destination accounts cannot be the same"
            );
        }

        Long fromAccountId =
                accountRepository
                        .findIdByAccountnumber(
                                normalizedFrom
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Source account was not found"
                                )
                        );

        Long toAccountId =
                accountRepository
                        .findIdByAccountnumber(
                                normalizedTo
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Destination account was not found"
                                )
                        );

        return transferByAccountIds(
                fromAccountId,
                toAccountId,
                amount
        );
    }

//    private Account findLockedAccount(
//            List<Account> lockedAccounts,
//            Long accountId,
//            String errorMessage
//    ) {
//
//        return lockedAccounts.stream()
//                .filter(account ->
//                        accountId.equals(
//                                account.getAccountid()
//                        )
//                )
//                .findFirst()
//                .orElseThrow(() ->
//                        new AccountNotFoundException(
//                                errorMessage
//                        )
//                );
//    }

    private Transfer executeTransfer(
            Account fromAccount,
            Account toAccount,
            BigDecimal amount
    ) {

        validateAccountActive(
                fromAccount
        );

        validateAccountActive(
                toAccount
        );

        BigDecimal sourceBalance =
                safeBalance(fromAccount);

        BigDecimal destinationBalance =
                safeBalance(toAccount);

        if (sourceBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }

        fromAccount.setBalance(
                sourceBalance.subtract(amount)
        );

        toAccount.setBalance(
                destinationBalance.add(amount)
        );

        accountRepository.saveAll(
                List.of(
                        fromAccount,
                        toAccount
                )
        );

        Transfer transfer =
                new Transfer(
                        fromAccount,
                        toAccount,
                        amount
                );

        transfer.setStatus(
                TransferStatus.SUCCESS
        );

        return transferRepository.save(
                transfer
        );
    }

    // =====================================================
    // STATEMENTS
    // =====================================================

    public List<StatementItem> getStatementByAccountId(
            Long accountId,
            LocalDateTime from,
            LocalDateTime to
    ) {

        List<Transaction> transactions =
                transactionRepository
                        .findByAccount_AccountidAndTxntimestampBetween(
                                accountId,
                                from,
                                to
                        );

        List<Transfer> transfers =
                transferRepository
                        .findByAccountInEitherSide(
                                accountId,
                                from,
                                to
                        );

        return buildStatement(
                accountId,
                null,
                transactions,
                transfers
        );
    }

    public List<StatementItem> getStatementByAccountNumber(
            String accountNumber,
            LocalDateTime from,
            LocalDateTime to
    ) {

        List<Transaction> transactions =
                transactionRepository
                        .findByAccount_AccountnumberAndTxntimestampBetween(
                                accountNumber,
                                from,
                                to
                        );

        List<Transfer> transfers =
                transferRepository
                        .findByAccountNumberInEitherSide(
                                accountNumber,
                                from,
                                to
                        );

        return buildStatement(
                null,
                accountNumber,
                transactions,
                transfers
        );
    }

    private List<StatementItem> buildStatement(
            Long accountId,
            String accountNumber,
            List<Transaction> transactions,
            List<Transfer> transfers
    ) {

        List<StatementItem> result =
                new ArrayList<>();

        for (Transaction transaction : transactions) {

            StatementItem item =
                    new StatementItem();

            item.setTimestamp(
                    transaction.getTxntimestamp()
            );

            item.setType(
                    transaction.getTxntype().name()
            );

            item.setStatus(
                    transaction.getStatus().name()
            );

            item.setRefId(
                    transaction.getTxnid()
            );

            if (transaction.getTxntype() ==
                    TransactionType.DEPOSIT) {

                item.setCredit(
                        transaction.getAmount()
                );

                item.setDebit(
                        BigDecimal.ZERO
                );

                item.setDescription(
                        "Deposit"
                );

            } else if (transaction.getTxntype() ==
                    TransactionType.WITHDRAW) {

                item.setDebit(
                        transaction.getAmount()
                );

                item.setCredit(
                        BigDecimal.ZERO
                );

                item.setDescription(
                        "Withdraw"
                );

            } else {

                item.setDescription(
                        "Transaction"
                );
            }

            result.add(item);
        }

        for (Transfer transfer : transfers) {

            StatementItem item =
                    new StatementItem();

            item.setTimestamp(
                    transfer.getTxntimestamp()
            );

            item.setType(
                    TRANSFER_OPERATION
            );

            item.setStatus(
                    transfer.getStatus().name()
            );

            item.setRefId(
                    transfer.getTransferid()
            );

            String fromAccountNumber =
                    transfer
                            .getFromAccount()
                            .getAccountnumber();

            String toAccountNumber =
                    transfer
                            .getToAccount()
                            .getAccountnumber();

            boolean debit;

            if (accountNumber != null) {

                debit =
                        fromAccountNumber.equals(
                                accountNumber
                        );

            } else {

                debit =
                        transfer
                                .getFromAccount()
                                .getAccountid()
                                .equals(accountId);
            }

            if (debit) {

                item.setDebit(
                        transfer.getAmount()
                );

                item.setCredit(
                        BigDecimal.ZERO
                );

                item.setDescription(
                        "Transfer to A/C " +
                                toAccountNumber
                );

            } else {

                item.setCredit(
                        transfer.getAmount()
                );

                item.setDebit(
                        BigDecimal.ZERO
                );

                item.setDescription(
                        "Transfer from A/C " +
                                fromAccountNumber
                );
            }

            result.add(item);
        }

        result.sort(
                Comparator.comparing(
                        StatementItem::getTimestamp,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                ).reversed()
        );

        return result;
    }

    // =====================================================
    // VALIDATION HELPERS
    // =====================================================

    private void validateAmount(
            BigDecimal amount
    ) {

        if (amount == null ||
                amount.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new InvalidAmountException(
                    "Amount must be greater than 0"
            );
        }
    }

    private void validateAccountActive(
            Account account
    ) {

        if (account == null) {
            throw new AccountNotFoundException(
                    "Account not found"
            );
        }

        if (account.getStatus() == null ||
                account.getStatus() ==
                        AccountStatus.INACTIVE) {

            throw new AccountInactiveException(
                    "Account is INACTIVE"
            );
        }
    }

    private BigDecimal safeBalance(
            Account account
    ) {

        return account.getBalance() == null
                ? BigDecimal.ZERO
                : account.getBalance();
    }
}