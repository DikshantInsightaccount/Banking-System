package com.transactionservice.transactionservice;

import com.transactionservice.transactionservice.clients.AuthClient;
import com.transactionservice.transactionservice.dto.StatementItem;
import com.transactionservice.transactionservice.dto.TransferRequest;
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
import com.transactionservice.transactionservice.services.TransactionService;
import com.transactionservice.transactionservice.kafka.TransferEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private IdempotencyKeyRepository
            idempotencyKeyRepository;

    @Mock
    private TransactionRepository
            transactionRepository;

    @Mock
    private TransferRepository
            transferRepository;

    @Mock
    private AccountRepository
            accountRepository;

    @Mock
    private TransferEventPublisher
            transferEventPublisher;

    @InjectMocks
    private TransactionService
            transactionService;

    private Account activeAccountOne;
    private Account activeAccountTwo;

    @BeforeEach
    void setUp() {

        activeAccountOne =
                activeAccount(
                        1L,
                        "A1",
                        "1000.00"
                );

        activeAccountTwo =
                activeAccount(
                        2L,
                        "A2",
                        "500.00"
                );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private static Account activeAccount(
            Long id,
            String number,
            String balance
    ) {

        Account account =
                new Account();

        account.setAccountid(id);
        account.setAccountnumber(number);

        account.setBalance(
                new BigDecimal(balance)
        );

        account.setStatus(
                AccountStatus.ACTIVE
        );

        return account;
    }

    private static Account inactiveAccount(
            Long id,
            String number,
            String balance
    ) {

        Account account =
                activeAccount(
                        id,
                        number,
                        balance
                );

        account.setStatus(
                AccountStatus.INACTIVE
        );

        return account;
    }

    private VerifyPasswordResponse
    validPasswordResponse() {

        VerifyPasswordResponse response =
                new VerifyPasswordResponse();

        response.setValid(true);

        return response;
    }

    private TransferRequest transferRequestByNumbers(
            String idempotencyKey,
            BigDecimal amount
    ) {

        TransferRequest request =
                new TransferRequest();

        request.setUserId(19L);
        request.setPassword("test-password");

        request.setFromAccountNumber(
                "A1"
        );

        request.setToAccountNumber(
                "A2"
        );

        request.setAmount(amount);

        request.setIdempotencyKey(
                idempotencyKey
        );

        return request;
    }

    private TransferRequest transferRequestByIds(
            String idempotencyKey,
            BigDecimal amount
    ) {

        TransferRequest request =
                new TransferRequest();

        request.setUserId(19L);
        request.setPassword("test-password");

        request.setFromAccountId(1L);
        request.setToAccountId(2L);

        request.setAmount(amount);

        request.setIdempotencyKey(
                idempotencyKey
        );

        return request;
    }

    private void mockValidPassword() {

        when(
                authClient.verifyPassword(any())
        ).thenReturn(
                validPasswordResponse()
        );
    }

    private void mockSuccessfulTransferSave(
            Long transferId
    ) {

        when(
                transferRepository.save(
                        any(Transfer.class)
                )
        ).thenAnswer(invocation -> {

            Transfer transfer =
                    invocation.getArgument(0);

            transfer.setTransferid(
                    transferId
            );

            return transfer;
        });

        when(
                idempotencyKeyRepository
                        .saveAndFlush(
                                any(IdempotencyKey.class)
                        )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );
    }

    // =====================================================
    // CRUD
    // =====================================================

    @Test
    void getAllTransactions_returnsList() {

        when(
                transactionRepository.findAll()
        ).thenReturn(
                List.of(
                        new Transaction(),
                        new Transaction()
                )
        );

        List<Transaction> result =
                transactionService
                        .getAllTransactions();

        assertEquals(
                2,
                result.size()
        );

        verify(
                transactionRepository
        ).findAll();
    }

    @Test
    void getOneTransaction_found_returnsTransaction() {

        Transaction transaction =
                new Transaction();

        transaction.setTxnid(10L);

        when(
                transactionRepository.findById(10L)
        ).thenReturn(
                Optional.of(transaction)
        );

        Transaction result =
                transactionService
                        .getOneTransaction(10L);

        assertEquals(
                10L,
                result.getTxnid()
        );
    }

    @Test
    void getOneTransaction_notFound_throws() {

        when(
                transactionRepository.findById(99L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                TransactionNotFoundException.class,
                () ->
                        transactionService
                                .getOneTransaction(99L)
        );
    }

    @Test
    void save_existingTransaction_throws() {

        Transaction transaction =
                new Transaction();

        transaction.setTxnid(5L);

        when(
                transactionRepository.existsById(5L)
        ).thenReturn(true);

        assertThrows(
                TransactionAlreadyExistsException.class,
                () ->
                        transactionService.save(
                                transaction
                        )
        );

        verify(
                transactionRepository,
                never()
        ).save(any());
    }

    @Test
    void save_newTransaction_succeeds() {

        Transaction transaction =
                new Transaction();

        when(
                transactionRepository.save(
                        any(Transaction.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Transaction result =
                transactionService.save(
                        transaction
                );

        assertNotNull(result);

        verify(
                transactionRepository
        ).save(transaction);
    }

    @Test
    void updateTransaction_missingId_throws() {

        Transaction transaction =
                new Transaction();

        assertThrows(
                TransactionNotFoundException.class,
                () ->
                        transactionService
                                .updateTransaction(
                                        transaction
                                )
        );

        verify(
                transactionRepository,
                never()
        ).save(any());
    }

    @Test
    void updateTransaction_existing_succeeds() {

        Transaction transaction =
                new Transaction();

        transaction.setTxnid(7L);

        when(
                transactionRepository.existsById(7L)
        ).thenReturn(true);

        when(
                transactionRepository.save(
                        any(Transaction.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Transaction result =
                transactionService
                        .updateTransaction(
                                transaction
                        );

        assertNotNull(result);

        verify(
                transactionRepository
        ).save(transaction);
    }

    @Test
    void deleteTransaction_existing_deletes() {

        when(
                transactionRepository.existsById(1L)
        ).thenReturn(true);

        transactionService.deleteTransaction(1L);

        verify(
                transactionRepository
        ).deleteById(1L);
    }

    @Test
    void deleteTransaction_missing_throws() {

        when(
                transactionRepository.existsById(1L)
        ).thenReturn(false);

        assertThrows(
                TransactionNotFoundException.class,
                () ->
                        transactionService
                                .deleteTransaction(1L)
        );

        verify(
                transactionRepository,
                never()
        ).deleteById(1L);
    }

    @Test
    void partialUpdate_updatesNonNullFields() {

        Transaction existing =
                new Transaction();

        existing.setTxnid(1L);
        existing.setTxntype(
                TransactionType.DEPOSIT
        );

        existing.setAmount(
                new BigDecimal("10.00")
        );

        existing.setStatus(
                TransactionStatus.SUCCESS
        );

        existing.setAccount(
                activeAccountOne
        );

        Transaction partial =
                new Transaction();

        partial.setTxntype(
                TransactionType.WITHDRAW
        );

        partial.setAmount(
                new BigDecimal("50.00")
        );

        partial.setAccount(
                activeAccountTwo
        );

        when(
                transactionRepository.findById(1L)
        ).thenReturn(
                Optional.of(existing)
        );

        when(
                transactionRepository.save(
                        any(Transaction.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Transaction result =
                transactionService.partialUpdate(
                        1L,
                        partial
                );

        assertEquals(
                TransactionType.WITHDRAW,
                result.getTxntype()
        );

        assertEquals(
                new BigDecimal("50.00"),
                result.getAmount()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                activeAccountTwo,
                result.getAccount()
        );
    }

    // =====================================================
    // LOCKED DEPOSIT
    // =====================================================

    @Test
    void deposit_byId_usesLockedLookup() {

        when(
                accountRepository
                        .findByIdForUpdate(1L)
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        when(
                accountRepository.save(
                        any(Account.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                transactionRepository.save(
                        any(Transaction.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Transaction transaction =
                transactionService.deposit(
                        1L,
                        new BigDecimal("200.00")
                );

        assertEquals(
                TransactionType.DEPOSIT,
                transaction.getTxntype()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                transaction.getStatus()
        );

        assertEquals(
                new BigDecimal("1200.00"),
                activeAccountOne.getBalance()
        );

        verify(
                accountRepository
        ).findByIdForUpdate(1L);

        verify(
                accountRepository,
                never()
        ).findById(1L);
    }

    @Test
    void deposit_byNumber_usesLockedLookup() {

        when(
                accountRepository
                        .findByAccountnumberForUpdate(
                                "A1"
                        )
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        when(
                accountRepository.save(
                        any(Account.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                transactionRepository.save(
                        any(Transaction.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        transactionService.deposit(
                "A1",
                new BigDecimal("100.00")
        );

        assertEquals(
                new BigDecimal("1100.00"),
                activeAccountOne.getBalance()
        );

        verify(
                accountRepository
        ).findByAccountnumberForUpdate(
                "A1"
        );
    }

    @Test
    void deposit_invalidAmount_throws() {

        assertThrows(
                InvalidAmountException.class,
                () ->
                        transactionService.deposit(
                                1L,
                                BigDecimal.ZERO
                        )
        );

        verify(
                accountRepository,
                never()
        ).findByIdForUpdate(any());
    }

    @Test
    void deposit_accountNotFound_throws() {

        when(
                accountRepository.findByIdForUpdate(9L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AccountNotFoundException.class,
                () ->
                        transactionService.deposit(
                                9L,
                                new BigDecimal("10.00")
                        )
        );
    }

    @Test
    void deposit_inactiveAccount_throws() {

        Account inactive =
                inactiveAccount(
                        3L,
                        "X",
                        "100.00"
                );

        when(
                accountRepository.findByIdForUpdate(3L)
        ).thenReturn(
                Optional.of(inactive)
        );

        assertThrows(
                AccountInactiveException.class,
                () ->
                        transactionService.deposit(
                                3L,
                                new BigDecimal("10.00")
                        )
        );
    }

    // =====================================================
    // LOCKED WITHDRAWAL
    // =====================================================

    @Test
    void withdraw_byId_usesLockedLookup() {

        when(
                accountRepository
                        .findByIdForUpdate(1L)
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        when(
                accountRepository.save(
                        any(Account.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                transactionRepository.save(
                        any(Transaction.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Transaction transaction =
                transactionService.withdraw(
                        1L,
                        new BigDecimal("300.00")
                );

        assertEquals(
                TransactionType.WITHDRAW,
                transaction.getTxntype()
        );

        assertEquals(
                new BigDecimal("700.00"),
                activeAccountOne.getBalance()
        );

        verify(
                accountRepository
        ).findByIdForUpdate(1L);
    }

    @Test
    void withdraw_byNumber_usesLockedLookup() {

        when(
                accountRepository
                        .findByAccountnumberForUpdate(
                                "A1"
                        )
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        when(
                accountRepository.save(
                        any(Account.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                transactionRepository.save(
                        any(Transaction.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        transactionService.withdraw(
                "A1",
                new BigDecimal("100.00")
        );

        assertEquals(
                new BigDecimal("900.00"),
                activeAccountOne.getBalance()
        );

        verify(
                accountRepository
        ).findByAccountnumberForUpdate(
                "A1"
        );
    }

    @Test
    void withdraw_insufficientBalance_doesNotSaveTransaction() {

        when(
                accountRepository.findByIdForUpdate(1L)
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        assertThrows(
                InsufficientBalanceException.class,
                () ->
                        transactionService.withdraw(
                                1L,
                                new BigDecimal("99999.00")
                        )
        );

        verify(
                accountRepository,
                never()
        ).save(any());

        verify(
                transactionRepository,
                never()
        ).save(any());
    }

    // =====================================================
    // SECURE TRANSFER AND IDEMPOTENCY
    // =====================================================

    @Test
    void secureTransfer_byAccountNumbers_success() {

        mockValidPassword();

        when(
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                "TRANSFER-001"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                accountRepository
                        .findIdByAccountnumber("A1")
        ).thenReturn(
                Optional.of(1L)
        );

        when(
                accountRepository
                        .findIdByAccountnumber("A2")
        ).thenReturn(
                Optional.of(2L)
        );

        when(
                accountRepository
                        .findByIdForUpdate(1L)
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        when(
                accountRepository
                        .findByIdForUpdate(2L)
        ).thenReturn(
                Optional.of(activeAccountTwo)
        );

        when(
                accountRepository.saveAll(
                        anyCollection()
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        mockSuccessfulTransferSave(101L);

        Transfer result =
                transactionService.secureTransfer(
                        transferRequestByNumbers(
                                "TRANSFER-001",
                                new BigDecimal("200.00")
                        )
                );

        assertEquals(
                101L,
                result.getTransferid()
        );

        assertEquals(
                TransferStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                new BigDecimal("800.00"),
                activeAccountOne.getBalance()
        );

        assertEquals(
                new BigDecimal("700.00"),
                activeAccountTwo.getBalance()
        );

        verify(
                accountRepository
        ).findByIdForUpdate(1L);

        verify(
                accountRepository
        ).findByIdForUpdate(2L);

        verify(
                idempotencyKeyRepository
        ).saveAndFlush(
                any(IdempotencyKey.class)
        );
    }

    @Test
    void secureTransfer_byAccountIds_success() {

        mockValidPassword();

        when(
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                "TRANSFER-002"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                accountRepository
                        .findByIdForUpdate(1L)
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        when(
                accountRepository
                        .findByIdForUpdate(2L)
        ).thenReturn(
                Optional.of(activeAccountTwo)
        );

        when(
                accountRepository.saveAll(
                        anyCollection()
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        mockSuccessfulTransferSave(102L);

        Transfer result =
                transactionService.secureTransfer(
                        transferRequestByIds(
                                "TRANSFER-002",
                                new BigDecimal("250.00")
                        )
                );

        assertEquals(
                TransferStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                new BigDecimal("750.00"),
                activeAccountOne.getBalance()
        );

        assertEquals(
                new BigDecimal("750.00"),
                activeAccountTwo.getBalance()
        );
    }

    @Test
    void secureTransfer_replay_returnsExistingTransfer() {

        mockValidPassword();

        IdempotencyKey record =
                new IdempotencyKey();

        record.setIdempotencyKey(
                "TRANSFER-REPLAY"
        );

        record.setOperationType(
                "TRANSFER"
        );

        record.setReferenceId(
                500L
        );

        Transfer existingTransfer =
                new Transfer();

        existingTransfer.setTransferid(
                500L
        );

        existingTransfer.setStatus(
                TransferStatus.SUCCESS
        );

        when(
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                "TRANSFER-REPLAY"
                        )
        ).thenReturn(
                Optional.of(record)
        );

        when(
                transferRepository.findById(500L)
        ).thenReturn(
                Optional.of(existingTransfer)
        );

        Transfer result =
                transactionService.secureTransfer(
                        transferRequestByNumbers(
                                "TRANSFER-REPLAY",
                                new BigDecimal("200.00")
                        )
                );

        assertEquals(
                500L,
                result.getTransferid()
        );

        verify(
                accountRepository
        ).findByIdForUpdate(1L);

        verify(
                accountRepository
        ).findByIdForUpdate(2L);

        verify(
                transferRepository,
                never()
        ).save(any());

        verify(
                idempotencyKeyRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void secureTransfer_invalidPassword_throws() {

        VerifyPasswordResponse response =
                new VerifyPasswordResponse();

        response.setValid(false);

        when(
                authClient.verifyPassword(any())
        ).thenReturn(response);

        assertThrows(
                InvalidTransferException.class,
                () ->
                        transactionService.secureTransfer(
                                transferRequestByNumbers(
                                        "PASSWORD-FAIL",
                                        new BigDecimal("100.00")
                                )
                        )
        );

        verify(
                accountRepository
        ).findByIdForUpdate(1L);

        verify(
                accountRepository
        ).findByIdForUpdate(2L);
    }

    @Test
    void secureTransfer_insufficientBalance_throws() {

        mockValidPassword();

        when(
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                "INSUFFICIENT-001"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                accountRepository
                        .findIdByAccountnumber("A1")
        ).thenReturn(
                Optional.of(1L)
        );

        when(
                accountRepository
                        .findIdByAccountnumber("A2")
        ).thenReturn(
                Optional.of(2L)
        );

        when(
                accountRepository
                        .findByIdForUpdate(1L)
        ).thenReturn(
                Optional.of(activeAccountOne)
        );

        when(
                accountRepository
                        .findByIdForUpdate(2L)
        ).thenReturn(
                Optional.of(activeAccountTwo)
        );

        assertThrows(
                InsufficientBalanceException.class,
                () ->
                        transactionService.secureTransfer(
                                transferRequestByNumbers(
                                        "INSUFFICIENT-001",
                                        new BigDecimal("99999.00")
                                )
                        )
        );

        verify(
                transferRepository,
                never()
        ).save(any());

        verify(
                idempotencyKeyRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void secureTransfer_missingAccount_throws() {

        mockValidPassword();

        when(
                idempotencyKeyRepository
                        .findByIdempotencyKey(
                                "MISSING-ACCOUNT"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                accountRepository
                        .findIdByAccountnumber("A1")
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AccountNotFoundException.class,
                () ->
                        transactionService.secureTransfer(
                                transferRequestByNumbers(
                                        "MISSING-ACCOUNT",
                                        new BigDecimal("100.00")
                                )
                        )
        );
    }

    // =====================================================
    // STATEMENT BUILDING
    // =====================================================

    @Test
    void getStatementByAccountId_buildsDepositAndWithdrawItems() {

        LocalDateTime from =
                LocalDateTime.now()
                        .minusDays(2);

        LocalDateTime to =
                LocalDateTime.now();

        Transaction deposit =
                org.mockito.Mockito.mock(
                        Transaction.class
                );

        when(
                deposit.getTxntimestamp()
        ).thenReturn(
                from.plusHours(1)
        );

        when(
                deposit.getTxntype()
        ).thenReturn(
                TransactionType.DEPOSIT
        );

        when(
                deposit.getStatus()
        ).thenReturn(
                TransactionStatus.SUCCESS
        );

        when(
                deposit.getTxnid()
        ).thenReturn(1L);

        when(
                deposit.getAmount()
        ).thenReturn(
                new BigDecimal("100.00")
        );

        Transaction withdrawal =
                org.mockito.Mockito.mock(
                        Transaction.class
                );

        when(
                withdrawal.getTxntimestamp()
        ).thenReturn(
                from.plusHours(2)
        );

        when(
                withdrawal.getTxntype()
        ).thenReturn(
                TransactionType.WITHDRAW
        );

        when(
                withdrawal.getStatus()
        ).thenReturn(
                TransactionStatus.SUCCESS
        );

        when(
                withdrawal.getTxnid()
        ).thenReturn(2L);

        when(
                withdrawal.getAmount()
        ).thenReturn(
                new BigDecimal("50.00")
        );

        when(
                transactionRepository
                        .findByAccount_AccountidAndTxntimestampBetween(
                                1L,
                                from,
                                to
                        )
        ).thenReturn(
                List.of(
                        deposit,
                        withdrawal
                )
        );

        when(
                transferRepository
                        .findByAccountInEitherSide(
                                1L,
                                from,
                                to
                        )
        ).thenReturn(
                List.of()
        );

        List<StatementItem> items =
                transactionService
                        .getStatementByAccountId(
                                1L,
                                from,
                                to
                        );

        assertEquals(
                2,
                items.size()
        );

        assertTrue(
                items.stream()
                        .anyMatch(item ->
                                "DEPOSIT".equals(
                                        item.getType()
                                )
                        )
        );

        assertTrue(
                items.stream()
                        .anyMatch(item ->
                                "WITHDRAW".equals(
                                        item.getType()
                                )
                        )
        );
    }

    @Test
    void getStatementByAccountNumber_buildsTransferDescriptions() {

        LocalDateTime from =
                LocalDateTime.now()
                        .minusDays(2);

        LocalDateTime to =
                LocalDateTime.now();

        Account accountOne =
                activeAccount(
                        1L,
                        "A1",
                        "1000.00"
                );

        Account accountTwo =
                activeAccount(
                        2L,
                        "A2",
                        "500.00"
                );

        Transfer debitTransfer =
                org.mockito.Mockito.mock(
                        Transfer.class
                );

        when(
                debitTransfer.getTxntimestamp()
        ).thenReturn(
                from.plusHours(1)
        );

        when(
                debitTransfer.getStatus()
        ).thenReturn(
                TransferStatus.SUCCESS
        );

        when(
                debitTransfer.getTransferid()
        ).thenReturn(100L);

        when(
                debitTransfer.getAmount()
        ).thenReturn(
                new BigDecimal("10.00")
        );

        when(
                debitTransfer.getFromAccount()
        ).thenReturn(accountOne);

        when(
                debitTransfer.getToAccount()
        ).thenReturn(accountTwo);

        Transfer creditTransfer =
                org.mockito.Mockito.mock(
                        Transfer.class
                );

        when(
                creditTransfer.getTxntimestamp()
        ).thenReturn(
                from.plusHours(2)
        );

        when(
                creditTransfer.getStatus()
        ).thenReturn(
                TransferStatus.SUCCESS
        );

        when(
                creditTransfer.getTransferid()
        ).thenReturn(200L);

        when(
                creditTransfer.getAmount()
        ).thenReturn(
                new BigDecimal("20.00")
        );

        when(
                creditTransfer.getFromAccount()
        ).thenReturn(accountTwo);

        when(
                creditTransfer.getToAccount()
        ).thenReturn(accountOne);

        when(
                transactionRepository
                        .findByAccount_AccountnumberAndTxntimestampBetween(
                                "A1",
                                from,
                                to
                        )
        ).thenReturn(
                List.of()
        );

        when(
                transferRepository
                        .findByAccountNumberInEitherSide(
                                "A1",
                                from,
                                to
                        )
        ).thenReturn(
                List.of(
                        debitTransfer,
                        creditTransfer
                )
        );

        List<StatementItem> items =
                transactionService
                        .getStatementByAccountNumber(
                                "A1",
                                from,
                                to
                        );

        assertEquals(
                2,
                items.size()
        );

        assertTrue(
                items.stream()
                        .anyMatch(item ->
                                "Transfer to A/C A2"
                                        .equals(
                                                item.getDescription()
                                        )
                        )
        );

        assertTrue(
                items.stream()
                        .anyMatch(item ->
                                "Transfer from A/C A2"
                                        .equals(
                                                item.getDescription()
                                        )
                        )
        );
    }
}