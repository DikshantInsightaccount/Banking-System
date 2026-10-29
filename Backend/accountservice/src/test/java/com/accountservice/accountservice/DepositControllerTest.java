package com.accountservice.accountservice;


import com.accountservice.accountservice.entity.FixedDeposit;
import com.accountservice.accountservice.entity.RecurringDeposit;
import com.accountservice.accountservice.services.DepositService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DepositControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DepositService depositService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFixedDeposit_success() throws Exception {
        FixedDeposit fd = new FixedDeposit();
        when(depositService.createFixedDeposit(eq(1L), any()))
                .thenReturn(fd);

        mvc.perform(post("/deposits/fixed/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fd)))
                .andExpect(status().isOk());
    }

    @Test
    void getFixedDeposits_success() throws Exception {
        when(depositService.getFixedDeposits(1L))
                .thenReturn(List.of(new FixedDeposit()));

        mvc.perform(get("/deposits/fixed/account/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getFixedDeposit_success() throws Exception {
        when(depositService.getFixedDeposit(10L))
                .thenReturn(new FixedDeposit());

        mvc.perform(get("/deposits/fixed/10"))
                .andExpect(status().isOk());
    }

    @Test
    void createRecurringDeposit_success() throws Exception {
        RecurringDeposit rd = new RecurringDeposit();
        when(depositService.createRecurringDeposit(eq(1L), any()))
                .thenReturn(rd);

        mvc.perform(post("/deposits/recurring/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rd)))
                .andExpect(status().isOk());
    }

    @Test
    void getRecurringDeposits_success() throws Exception {
        when(depositService.getRecurringDeposits(1L))
                .thenReturn(List.of(new RecurringDeposit()));

        mvc.perform(get("/deposits/recurring/account/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecurringDeposit_success() throws Exception {
        when(depositService.getRecurringDeposit(5L))
                .thenReturn(new RecurringDeposit());

        mvc.perform(get("/deposits/recurring/5"))
                .andExpect(status().isOk());
    }
}