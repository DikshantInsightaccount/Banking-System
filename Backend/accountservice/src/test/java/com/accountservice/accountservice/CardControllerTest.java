package com.accountservice.accountservice;


import com.accountservice.accountservice.dto.CardControlsRequest;
import com.accountservice.accountservice.dto.LockRequest;
import com.accountservice.accountservice.dto.StatusRequest;
import com.accountservice.accountservice.entity.Card;
import com.accountservice.accountservice.services.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.accountservice.accountservice.entity.Status.INACTIVE;
import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCard_success() throws Exception {
        when(cardService.getOrCreate(eq(1L), any()))
                .thenReturn(new Card());

        mvc.perform(get("/cards/1")
                        .param("holderName", "John Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void updateControls_success() throws Exception {
        CardControlsRequest req = new CardControlsRequest();
        when(cardService.updateControls(eq(1L), any()))
                .thenReturn(new Card());

        mvc.perform(put("/cards/1/controls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void lockUnlock_success() throws Exception {
        LockRequest req = new LockRequest();
        req.setLocked(true);

        when(cardService.setLocked(1L, true)).thenReturn(new Card());

        mvc.perform(put("/cards/1/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void activateDeactivate_success() throws Exception {
        StatusRequest req = new StatusRequest();
        req.setStatus(INACTIVE);

        when(cardService.setStatus(eq(1L), any()))
                .thenReturn(new Card());

        mvc.perform(put("/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}