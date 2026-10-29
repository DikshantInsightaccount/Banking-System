package com.authservice.authservice.service.passkey;

import com.webauthn4j.data.client.challenge.Challenge;

import java.util.List;

public interface PasskeyChallengeStore {

    enum CeremonyType { REGISTRATION, AUTHENTICATION }

    record ChallengeRecord(
            CeremonyType type,
            String username,
            Long userId,
            Challenge challenge,
            List<Integer> pubKeyAlgs,
            long expiresAtEpochMs
    ) {}

    String put(ChallengeRecord record);

    ChallengeRecord consume(String requestId);
}