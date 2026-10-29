package com.transactionservice.transactionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactionservice.transactionservice.dto.TransferRequest;
import com.transactionservice.transactionservice.entity.Transaction;
import com.transactionservice.transactionservice.entity.Transfer;
import com.transactionservice.transactionservice.services.TransactionService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    // =====================================================
    // TRANSACTION CRUD
    // =====================================================

    @Test
    void testGetAllTransactions_success()
            throws Exception {

        when(
                transactionService.getAllTransactions()
        ).thenReturn(
                List.of(new Transaction())
        );

        mvc.perform(
                        get("/api/transactions/all")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                );

        verify(
                transactionService
        ).getAllTransactions();
    }

    @Test
    void testGetTransaction_success()
            throws Exception {

        when(
                transactionService
                        .getOneTransaction(1L)
        ).thenReturn(
                new Transaction()
        );

        mvc.perform(
                        get(
                                "/api/transactions/{id}",
                                1
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                );

        verify(
                transactionService
        ).getOneTransaction(1L);
    }

    @Test
    void testCreateTransaction_success()
            throws Exception {

        Transaction transaction =
                new Transaction();

        when(
                transactionService.save(
                        any(Transaction.class)
                )
        ).thenReturn(transaction);

        mvc.perform(
                        post(
                                "/api/transactions/create"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        transaction
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                );

        verify(
                transactionService
        ).save(
                any(Transaction.class)
        );
    }

    @Test
    void testUpdateTransaction_success()
            throws Exception {

        Transaction transaction =
                new Transaction();

        when(
                transactionService.updateTransaction(
                        any(Transaction.class)
                )
        ).thenReturn(transaction);

        mvc.perform(
                        put(
                                "/api/transactions/update"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        transaction
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                );

        verify(
                transactionService
        ).updateTransaction(
                any(Transaction.class)
        );
    }

    @Test
    void testPartialUpdate_success()
            throws Exception {

        Transaction transaction =
                new Transaction();

        when(
                transactionService.partialUpdate(
                        eq(1L),
                        any(Transaction.class)
                )
        ).thenReturn(transaction);

        mvc.perform(
                        patch(
                                "/api/transactions/update/1"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        transaction
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                transactionService
        ).partialUpdate(
                eq(1L),
                any(Transaction.class)
        );
    }

    @Test
    void testDeleteTransaction_success()
            throws Exception {

        doNothing()
                .when(transactionService)
                .deleteTransaction(1L);

        mvc.perform(
                        delete(
                                "/api/transactions/delete/{id}",
                                1
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Transaction deleted successfully"
                                )
                );

        verify(
                transactionService
        ).deleteTransaction(1L);
    }

    // =====================================================
    // DEPOSIT
    // =====================================================

    @Test
    void testDeposit_withAccountNumber_branch()
            throws Exception {

        when(
                transactionService.deposit(
                        anyString(),
                        any(BigDecimal.class)
                )
        ).thenReturn(
                new Transaction()
        );

        String body =
                """
                {
                  "accountNumber": "ACC123",
                  "amount": 1000
                }
                """;

        mvc.perform(
                        post(
                                "/api/transactions/deposit"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                transactionService
        ).deposit(
                "ACC123",
                new BigDecimal("1000")
        );
    }

    @Test
    void testDeposit_withAccountId_branch()
            throws Exception {

        when(
                transactionService.deposit(
                        anyLong(),
                        any(BigDecimal.class)
                )
        ).thenReturn(
                new Transaction()
        );

        String body =
                """
                {
                  "accountId": 10,
                  "amount": 500
                }
                """;

        mvc.perform(
                        post(
                                "/api/transactions/deposit"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                transactionService
        ).deposit(
                10L,
                new BigDecimal("500")
        );
    }

    // =====================================================
    // WITHDRAW
    // =====================================================

    @Test
    void testWithdraw_withAccountNumber_branch()
            throws Exception {

        when(
                transactionService.withdraw(
                        anyString(),
                        any(BigDecimal.class)
                )
        ).thenReturn(
                new Transaction()
        );

        String body =
                """
                {
                  "accountNumber": "ACC123",
                  "amount": 300
                }
                """;

        mvc.perform(
                        post(
                                "/api/transactions/withdraw"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                transactionService
        ).withdraw(
                "ACC123",
                new BigDecimal("300")
        );
    }

    @Test
    void testWithdraw_withAccountId_branch()
            throws Exception {

        when(
                transactionService.withdraw(
                        anyLong(),
                        any(BigDecimal.class)
                )
        ).thenReturn(
                new Transaction()
        );

        String body =
                """
                {
                  "accountId": 22,
                  "amount": 300
                }
                """;

        mvc.perform(
                        post(
                                "/api/transactions/withdraw"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                transactionService
        ).withdraw(
                22L,
                new BigDecimal("300")
        );
    }

    // =====================================================
    // SECURE AND IDEMPOTENT TRANSFER
    // =====================================================

    @Test
    void testSecureTransfer_withAccountNumbers()
            throws Exception {

        Transfer transfer =
                new Transfer();

        when(
                transactionService.secureTransfer(
                        any(TransferRequest.class)
                )
        ).thenReturn(transfer);

        String body =
                """
                {
                  "userId": 19,
                  "fromAccountNumber": "A1",
                  "toAccountNumber": "A2",
                  "amount": 100,
                  "password": "test-password",
                  "idempotencyKey": "TRANSFER-TEST-001"
                }
                """;

        mvc.perform(
                        post(
                                "/api/transactions/transfer"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                transactionService
        ).secureTransfer(
                any(TransferRequest.class)
        );
    }

    @Test
    void testSecureTransfer_withAccountIds()
            throws Exception {

        Transfer transfer =
                new Transfer();

        when(
                transactionService.secureTransfer(
                        any(TransferRequest.class)
                )
        ).thenReturn(transfer);

        String body =
                """
                {
                  "userId": 19,
                  "fromAccountId": 1,
                  "toAccountId": 2,
                  "amount": 250,
                  "password": "test-password",
                  "idempotencyKey": "TRANSFER-TEST-002"
                }
                """;

        mvc.perform(
                        post(
                                "/api/transactions/transfer"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                transactionService
        ).secureTransfer(
                any(TransferRequest.class)
        );
    }

    // =====================================================
    // STATEMENTS
    // =====================================================

    @Test
    void testGetStatement_withAccountNumber_branch()
            throws Exception {

        when(
                transactionService
                        .getStatementByAccountNumber(
                                anyString(),
                                any(),
                                any()
                        )
        ).thenReturn(
                List.of()
        );

        mvc.perform(
                        get(
                                "/api/transactions/statement"
                        )
                                .param(
                                        "accountNumber",
                                        "ACC1"
                                )
                                .param(
                                        "from",
                                        "2024-01-01T00:00:00"
                                )
                                .param(
                                        "to",
                                        "2024-01-02T00:00:00"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                );

        verify(
                transactionService
        ).getStatementByAccountNumber(
                eq("ACC1"),
                any(),
                any()
        );

        verify(
                transactionService,
                never()
        ).getStatementByAccountId(
                any(),
                any(),
                any()
        );
    }

    @Test
    void testGetStatement_withAccountId_branch()
            throws Exception {

        when(
                transactionService
                        .getStatementByAccountId(
                                anyLong(),
                                any(),
                                any()
                        )
        ).thenReturn(
                List.of()
        );

        mvc.perform(
                        get(
                                "/api/transactions/statement"
                        )
                                .param(
                                        "accountId",
                                        "5"
                                )
                                .param(
                                        "from",
                                        "2024-01-01T00:00:00"
                                )
                                .param(
                                        "to",
                                        "2024-01-02T00:00:00"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                );

        verify(
                transactionService
        ).getStatementByAccountId(
                eq(5L),
                any(),
                any()
        );

        verify(
                transactionService,
                never()
        ).getStatementByAccountNumber(
                anyString(),
                any(),
                any()
        );
    }
}