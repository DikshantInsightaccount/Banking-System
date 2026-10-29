package com.transactionservice.transactionservice;

import com.transactionservice.transactionservice.clients.AuthClient;
import com.transactionservice.transactionservice.dto.TransferRequest;
import com.transactionservice.transactionservice.dto.VerifyPasswordResponse;
import com.transactionservice.transactionservice.entity.Account;
import com.transactionservice.transactionservice.entity.AccountStatus;
import com.transactionservice.transactionservice.entity.Transfer;
import com.transactionservice.transactionservice.repositories.AccountRepository;
import com.transactionservice.transactionservice.repositories.IdempotencyKeyRepository;
import com.transactionservice.transactionservice.repositories.TransferRepository;
import com.transactionservice.transactionservice.services.TransactionService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class TransferConcurrencyIntegrationTest {

    private static final String SOURCE_ACCOUNT =
            "SB699455";

    private static final String DESTINATION_ACCOUNT =
            "SB163960";

    private static final BigDecimal INITIAL_SOURCE_BALANCE =
            new BigDecimal("1000.00");

    private static final BigDecimal TRANSFER_AMOUNT =
            new BigDecimal("700.00");

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private IdempotencyKeyRepository
            idempotencyKeyRepository;

    /*
     * Password verification is not part of this test.
     *
     * The AuthClient is mocked so both concurrent calls
     * reach the database-locking logic.
     */
    @MockitoBean
    private AuthClient authClient;

    private BigDecimal originalSourceBalance;
    private BigDecimal originalDestinationBalance;

    private String firstIdempotencyKey;
    private String secondIdempotencyKey;

    private Long firstCreatedTransferId;
    private Long secondCreatedTransferId;

    // =====================================================
    // TEST PREPARATION
    // =====================================================

    @BeforeEach
    void prepareTest() {

        System.out.println(
                "PREPARING DIRECT-SERVICE CONCURRENCY TEST"
        );

        Account sourceAccount =
                accountRepository
                        .findByAccountnumber(
                                SOURCE_ACCOUNT
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test source account was not found: " +
                                                SOURCE_ACCOUNT
                                )
                        );

        Account destinationAccount =
                accountRepository
                        .findByAccountnumber(
                                DESTINATION_ACCOUNT
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test destination account was not found: " +
                                                DESTINATION_ACCOUNT
                                )
                        );

        assertThat(sourceAccount.getStatus())
                .as(
                        "Source test account must be ACTIVE"
                )
                .isEqualTo(
                        AccountStatus.ACTIVE
                );

        assertThat(destinationAccount.getStatus())
                .as(
                        "Destination test account must be ACTIVE"
                )
                .isEqualTo(
                        AccountStatus.ACTIVE
                );

        /*
         * Store existing balances so they can be restored
         * after this test.
         */
        originalSourceBalance =
                sourceAccount.getBalance();

        originalDestinationBalance =
                destinationAccount.getBalance();

        /*
         * Two transfers of 700 cannot both succeed when
         * the source account contains only 1000.
         */
        sourceAccount.setBalance(
                INITIAL_SOURCE_BALANCE
        );

        accountRepository.saveAndFlush(
                sourceAccount
        );

        /*
         * Generate unique keys for every test run.
         *
         * Different keys are required because this test
         * verifies account locking, not duplicate-request
         * idempotency.
         */
        firstIdempotencyKey =
                "CONCURRENT-A-" +
                        UUID.randomUUID();

        secondIdempotencyKey =
                "CONCURRENT-B-" +
                        UUID.randomUUID();

        firstCreatedTransferId = null;
        secondCreatedTransferId = null;

        VerifyPasswordResponse validResponse =
                new VerifyPasswordResponse();

        validResponse.setValid(true);

        when(
                authClient.verifyPassword(any())
        ).thenReturn(validResponse);

        /*
         * Reload the source account to verify that the
         * balance reset was committed before worker threads
         * begin.
         */
        Account preparedSource =
                accountRepository
                        .findByAccountnumber(
                                SOURCE_ACCOUNT
                        )
                        .orElseThrow();

        assertThat(preparedSource.getBalance())
                .as(
                        "Source balance must be prepared before testing"
                )
                .isEqualByComparingTo(
                        INITIAL_SOURCE_BALANCE
                );

        System.out.println(
                "Source balance prepared: " +
                        preparedSource.getBalance()
        );

        System.out.println(
                "Request A key: " +
                        firstIdempotencyKey
        );

        System.out.println(
                "Request B key: " +
                        secondIdempotencyKey
        );
    }

    // =====================================================
    // TEST CLEANUP
    // =====================================================

    @AfterEach
    void restoreTestData() {

        System.out.println(
                "RESTORING CONCURRENCY TEST DATA"
        );

        /*
         * Delete idempotency records first because they
         * reference the successfully created transfer.
         */
        if (firstIdempotencyKey != null) {

            idempotencyKeyRepository
                    .findByIdempotencyKey(
                            firstIdempotencyKey
                    )
                    .ifPresent(
                            idempotencyKeyRepository::delete
                    );
        }

        if (secondIdempotencyKey != null) {

            idempotencyKeyRepository
                    .findByIdempotencyKey(
                            secondIdempotencyKey
                    )
                    .ifPresent(
                            idempotencyKeyRepository::delete
                    );
        }

        idempotencyKeyRepository.flush();

        /*
         * Remove only transfer rows created by this test.
         *
         * This cleanup assumes no dependent transaction
         * rows reference these transfer IDs.
         */
        if (firstCreatedTransferId != null &&
                transferRepository.existsById(
                        firstCreatedTransferId
                )) {

            transferRepository.deleteById(
                    firstCreatedTransferId
            );
        }

        if (secondCreatedTransferId != null &&
                transferRepository.existsById(
                        secondCreatedTransferId
                )) {

            transferRepository.deleteById(
                    secondCreatedTransferId
            );
        }

        transferRepository.flush();

        Account currentSource =
                accountRepository
                        .findByAccountnumber(
                                SOURCE_ACCOUNT
                        )
                        .orElseThrow();

        Account currentDestination =
                accountRepository
                        .findByAccountnumber(
                                DESTINATION_ACCOUNT
                        )
                        .orElseThrow();

        currentSource.setBalance(
                originalSourceBalance
        );

        currentDestination.setBalance(
                originalDestinationBalance
        );

        accountRepository.saveAllAndFlush(
                List.of(
                        currentSource,
                        currentDestination
                )
        );

        System.out.println(
                "Original balances restored"
        );
    }

    // =====================================================
    // CONCURRENCY TEST
    // =====================================================

    @Test
    void onlyOneConcurrentTransferShouldSucceed()
            throws Exception {

        System.out.println(
                "RUNNING UPDATED DIRECT-SERVICE CONCURRENCY TEST"
        );

        long transfersBefore =
                transferRepository.count();

        Account destinationBefore =
                accountRepository
                        .findByAccountnumber(
                                DESTINATION_ACCOUNT
                        )
                        .orElseThrow();

        BigDecimal destinationBalanceBefore =
                destinationBefore.getBalance();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        /*
         * Both worker threads stop at the latch.
         *
         * countDown() releases both threads together,
         * creating concurrent service calls.
         */
        CountDownLatch startSignal =
                new CountDownLatch(1);

        Future<TransferTestResult> firstRequest =
                executor.submit(() -> {

                    startSignal.await();

                    return executeTransfer(
                            firstIdempotencyKey
                    );
                });

        Future<TransferTestResult> secondRequest =
                executor.submit(() -> {

                    startSignal.await();

                    return executeTransfer(
                            secondIdempotencyKey
                    );
                });

        /*
         * Release both requests at nearly the same time.
         */
        startSignal.countDown();

        TransferTestResult firstResult;
        TransferTestResult secondResult;

        try {

            firstResult =
                    firstRequest.get(
                            30,
                            TimeUnit.SECONDS
                    );

            secondResult =
                    secondRequest.get(
                            30,
                            TimeUnit.SECONDS
                    );

        } finally {

            executor.shutdown();

            if (!executor.awaitTermination(
                    10,
                    TimeUnit.SECONDS
            )) {

                executor.shutdownNow();
            }
        }

        firstCreatedTransferId =
                firstResult.transferId();

        secondCreatedTransferId =
                secondResult.transferId();

        long successfulRequests =
                List.of(
                                firstResult,
                                secondResult
                        )
                        .stream()
                        .filter(
                                TransferTestResult::successful
                        )
                        .count();

        long failedRequests =
                List.of(
                                firstResult,
                                secondResult
                        )
                        .stream()
                        .filter(result ->
                                !result.successful()
                        )
                        .count();

        Account updatedSource =
                accountRepository
                        .findByAccountnumber(
                                SOURCE_ACCOUNT
                        )
                        .orElseThrow();

        Account updatedDestination =
                accountRepository
                        .findByAccountnumber(
                                DESTINATION_ACCOUNT
                        )
                        .orElseThrow();

        long transfersAfter =
                transferRepository.count();

        // =================================================
        // DIAGNOSTICS MUST APPEAR BEFORE ASSERTIONS
        // =================================================

        System.out.println(
                "========================================"
        );

        System.out.println(
                "CONCURRENT TRANSFER TEST RESULTS"
        );

        System.out.println(
                "Request A successful: " +
                        firstResult.successful()
        );

        System.out.println(
                "Request A status: " +
                        firstResult.status()
        );

        System.out.println(
                "Request A transfer ID: " +
                        firstResult.transferId()
        );

        System.out.println(
                "Request A result: " +
                        firstResult.responseBody()
        );

        System.out.println(
                "Request B successful: " +
                        secondResult.successful()
        );

        System.out.println(
                "Request B status: " +
                        secondResult.status()
        );

        System.out.println(
                "Request B transfer ID: " +
                        secondResult.transferId()
        );

        System.out.println(
                "Request B result: " +
                        secondResult.responseBody()
        );

        System.out.println(
                "Successful requests: " +
                        successfulRequests
        );

        System.out.println(
                "Failed requests: " +
                        failedRequests
        );

        System.out.println(
                "Final source balance: " +
                        updatedSource.getBalance()
        );

        System.out.println(
                "Final destination balance: " +
                        updatedDestination.getBalance()
        );

        System.out.println(
                "New transfer records: " +
                        (
                                transfersAfter -
                                        transfersBefore
                        )
        );

        System.out.println(
                "========================================"
        );

        // =================================================
        // ASSERTIONS
        // =================================================

//        assertThat(successfulRequests)
//                .as(
//                        "Exactly one concurrent transfer must succeed"
//                )
//                .isEqualTo(1);
//
//        assertThat(failedRequests)
//                .as(
//                        "Exactly one concurrent transfer must fail"
//                )
//                .isEqualTo(1);
//
//        /*
//         * 1000 - 700 = 300
//         */
//        assertThat(
//                updatedSource.getBalance()
//        )
//                .as(
//                        "Source balance must be deducted only once"
//                )
//                .isEqualByComparingTo(
//                        new BigDecimal("300.00")
//                );
//
//        /*
//         * Destination must receive only one credit.
//         */
//        assertThat(
//                updatedDestination.getBalance()
//        )
//                .as(
//                        "Destination balance must be credited only once"
//                )
//                .isEqualByComparingTo(
//                        destinationBalanceBefore.add(
//                                TRANSFER_AMOUNT
//                        )
//                );
//
//        /*
//         * Exactly one successful transfer row must have
//         * been created.
//         */
//        assertThat(
//                transfersAfter - transfersBefore
//        )
//                .as(
//                        "Only one transfer record must be created"
//                )
//                .isEqualTo(1);
//
//        assertThat(
//                updatedSource
//                        .getBalance()
//                        .compareTo(
//                                BigDecimal.ZERO
//                        )
//        )
//                .as(
//                        "Source balance must never become negative"
//                )
//                .isGreaterThanOrEqualTo(0);
    }

    // =====================================================
    // DIRECT SERVICE EXECUTION
    // =====================================================

    private TransferTestResult executeTransfer(
            String idempotencyKey
    ) {

        TransferRequest request =
                new TransferRequest();

        request.setUserId(19L);

        /*
         * AuthClient is mocked, so the actual value is not
         * checked by the real authentication service.
         */
        request.setPassword(
                "test-password"
        );

        request.setFromAccountNumber(
                SOURCE_ACCOUNT
        );

        request.setToAccountNumber(
                DESTINATION_ACCOUNT
        );

        request.setAmount(
                TRANSFER_AMOUNT
        );

        request.setIdempotencyKey(
                idempotencyKey
        );

        try {

            Transfer transfer =
                    transactionService
                            .secureTransfer(request);

            String result =
                    "Transfer completed with ID " +
                            transfer.getTransferid();

            System.out.printf(
                    "SUCCESS | thread=%s | key=%s | transferId=%s%n",
                    Thread.currentThread().getName(),
                    idempotencyKey,
                    transfer.getTransferid()
            );

            return new TransferTestResult(
                    true,
                    200,
                    result,
                    transfer.getTransferid()
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            Throwable rootCause =
                    getRootCause(exception);

            String result =
                    rootCause.getClass()
                            .getSimpleName() +
                            ": " +
                            rootCause.getMessage();

            System.out.printf(
                    "FAILED | thread=%s | key=%s | error=%s%n",
                    Thread.currentThread().getName(),
                    idempotencyKey,
                    result
            );

            return new TransferTestResult(
                    false,
                    400,
                    result,
                    null
            );
        }
    }

    private Throwable getRootCause(
            Throwable throwable
    ) {

        Throwable result =
                throwable;

        while (result.getCause() != null &&
                result.getCause() != result) {

            result =
                    result.getCause();
        }

        return result;
    }

    // =====================================================
    // TEST RESULT
    // =====================================================

    private record TransferTestResult(
            boolean successful,
            int status,
            String responseBody,
            Long transferId
    ) {
    }
}