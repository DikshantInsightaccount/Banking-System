package com.accountservice.accountservice;

import com.accountservice.accountservice.dto.CardControlsRequest;
import com.accountservice.accountservice.entity.Card;
import com.accountservice.accountservice.entity.Status;
import com.accountservice.accountservice.repositories.CardRepository;
import com.accountservice.accountservice.services.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void getOrCreate_newCard() {

        Card existing = new Card();
        existing.setCardnumber("1234567812345678");

        when(cardRepository.findByAccountid(1L))
                .thenReturn(Optional.of(existing));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(i -> i.getArgument(0));

        Card card = cardService.getOrCreate(1L, "John Doe");

        assertNotNull(card.getCardnumber());
    }



    @Test
    void updateControls_success() {
        Card card = new Card();
        card.setLocked(false);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        CardControlsRequest req = new CardControlsRequest();
        req.setAtmEnabled(true);

        Card updated = cardService.updateControls(1L, req);
        assertTrue(updated.isAtmEnabled());
    }

    @Test
    void setLocked_success() {
        Card card = new Card();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        Card updated = cardService.setLocked(1L, true);
        assertTrue(updated.isLocked());
    }

    @Test
    void setStatus_success() {
        Card card = new Card();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        Card updated = cardService.setStatus(1L, Status.INACTIVE);
        assertEquals(Status.INACTIVE, updated.getStatus());
    }
}
