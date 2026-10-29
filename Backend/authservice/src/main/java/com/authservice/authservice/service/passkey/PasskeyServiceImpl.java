package com.authservice.authservice.service.passkey;

import com.authservice.authservice.config.WebAuthnProperties;
import com.authservice.authservice.dto.LoginResponse;
import com.authservice.authservice.dto.PasskeyOptionsResponse;
import com.authservice.authservice.entity.PasskeyCredential;
import com.authservice.authservice.entity.Status;
import com.authservice.authservice.entity.User;
import com.authservice.authservice.exceptions.AuthenticationFailedException;
import com.authservice.authservice.exceptions.ResourceNotFoundException;
import com.authservice.authservice.repository.PasskeyCredentialRepository;
import com.authservice.authservice.repository.UserRepository;
import com.authservice.authservice.security.JwtUtil;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.Authenticator;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PasskeyServiceImpl implements PasskeyService {

    private final WebAuthnManager webAuthnManager;
    private final WebAuthnProperties props;
    private final UserRepository userRepository;
    private final PasskeyCredentialRepository passkeyRepo;
    private final PasskeyChallengeStore challengeStore;
    private final JwtUtil jwtUtil;

    private static final List<Integer> PUBKEY_ALGS = List.of(-7, -257);

    // =========================
    // BEGIN REGISTRATION
    // =========================
    @Override
    public PasskeyOptionsResponse beginRegistration(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        byte[] challengeBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(challengeBytes);

        Challenge challenge = new DefaultChallenge(challengeBytes);

        String requestId = challengeStore.put(new PasskeyChallengeStore.ChallengeRecord(
                PasskeyChallengeStore.CeremonyType.REGISTRATION,
                user.getUsername(),
                user.getUserid(),
                challenge,
                PUBKEY_ALGS,
                Instant.now().toEpochMilli() + 300000
        ));

        // exclude old credentials
        List<PasskeyCredential> existing = passkeyRepo.findByUserIdAndActiveTrue(userId);

        List<Map<String, Object>> exclude = new ArrayList<>();
        for (PasskeyCredential c : existing) {
            exclude.add(Map.of("type", "public-key", "id", c.getCredentialId()));
        }

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", base64Url(challengeBytes));
        options.put("timeout", props.getTimeoutMs());

        options.put("rp", Map.of(
                "name", props.getRpName(),
                "id", props.getRpId()
        ));

        options.put("user", Map.of(
                "id", base64Url(String.valueOf(user.getUserid()).getBytes(StandardCharsets.UTF_8)),
                "name", user.getUsername(),
                "displayName", user.getUsername()
        ));

        List<Map<String, Object>> pubKeyCredParams = new ArrayList<>();
        for (Integer alg : PUBKEY_ALGS) {
            pubKeyCredParams.add(Map.of("type", "public-key", "alg", alg));
        }

        options.put("pubKeyCredParams", pubKeyCredParams);

        options.put("attestation", "none");
        options.put("authenticatorSelection", Map.of(
                "userVerification", "preferred",
                "residentKey", "preferred"
        ));

        if (!exclude.isEmpty()) {
            options.put("excludeCredentials", exclude);
        }

        return new PasskeyOptionsResponse(requestId, options);
    }


    // FINISH REGISTRATION FIXED

    @Override
    public void finishRegistration(Long userId, String requestId, String registrationResponseJson) {

        var record = challengeStore.consume(requestId);

        if (record == null || record.type() != PasskeyChallengeStore.CeremonyType.REGISTRATION) {
            throw new AuthenticationFailedException("Invalid registration request");
        }

        try {
            ServerProperty serverProperty = new ServerProperty(
                    new Origin(props.getOrigin()),
                    props.getRpId(),
                    record.challenge(),
                    null
            );

            RegistrationParameters params = new RegistrationParameters(
                    serverProperty,
                    toParams(record.pubKeyAlgs()),
                    false,
                    true
            );

            // verify registration
            RegistrationData registrationData =
                    webAuthnManager.verifyRegistrationResponseJSON(registrationResponseJson, params);

            Authenticator authenticator =
                    AuthenticatorImpl.createFromRegistrationData(registrationData);

            String credentialId =
                    base64Url(authenticator.getAttestedCredentialData().getCredentialId());

            if (passkeyRepo.existsByCredentialId(credentialId)) {
                throw new AuthenticationFailedException("Passkey already registered");
            }

            // SAVE (IMPORTANT FIX: store JSON not Base64)
            PasskeyCredential entity = new PasskeyCredential();
            entity.setUserId(userId);
            entity.setCredentialId(credentialId);
            entity.setActive(true);

            // THIS IS THE KEY FIX
            entity.setCredentialRecordJson(registrationResponseJson);

            entity.setSignCount(authenticator.getCounter());
            entity.setLastUsedAt(LocalDateTime.now());

            passkeyRepo.save(entity);

        } catch (Exception e) {
            throw new AuthenticationFailedException("Passkey registration failed: " + e.getMessage());
        }
    }

    // =========================
    // BEGIN LOGIN
    // =========================
    @Override
    public PasskeyOptionsResponse beginAuthentication(String username) {

        // DEBUG LOGS
        System.out.println("🔥 beginAuthentication called");
        System.out.println("🔥 Received username: " + username);

        // VALIDATE INPUT EARLY
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Username is NULL or EMPTY");
            throw new AuthenticationFailedException("Username is required");
        }

        // FETCH USER
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> {
                    System.out.println("❌ USER NOT FOUND in DB for: " + username);
                    return new AuthenticationFailedException("Invalid username");
                });

        System.out.println(" USER FOUND: " + user.getUsername());

        // FETCH PASSKEYS
        List<PasskeyCredential> creds =
                passkeyRepo.findByUserIdAndActiveTrue(user.getUserid());

        if (creds.isEmpty()) {
            System.out.println("❌ NO PASSKEYS FOUND for user: " + username);
            throw new AuthenticationFailedException("No passkeys registered");
        }

        System.out.println("PASSKEY COUNT: " + creds.size());

        // GENERATE CHALLENGE
        byte[] challengeBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(challengeBytes);

        Challenge challenge = new DefaultChallenge(challengeBytes);

        String requestId = challengeStore.put(
                new PasskeyChallengeStore.ChallengeRecord(
                        PasskeyChallengeStore.CeremonyType.AUTHENTICATION,
                        username,
                        user.getUserid(),
                        challenge,
                        PUBKEY_ALGS,
                        Instant.now().toEpochMilli() + 300000
                )
        );

        //  BUILD ALLOW LIST
        List<Map<String, Object>> allow = new ArrayList<>();

        for (PasskeyCredential c : creds) {
            allow.add(Map.of(
                    "type", "public-key",
                    "id", c.getCredentialId()
            ));
        }

        //  BUILD RESPONSE OPTIONS
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", base64Url(challengeBytes));
        options.put("rpId", props.getRpId());
        options.put("allowCredentials", allow);
        options.put("userVerification", "preferred");

        System.out.println(" Returning passkey options successfully");

        return new PasskeyOptionsResponse(requestId, options);
    }


    // FINISH LOGIN FIXED

    @Override
    public LoginResponse finishAuthentication(
            String username,
            String requestId,
            String authenticationResponseJson
    ) {

        var record = challengeStore.consume(requestId);

        if (record == null) {
            throw new AuthenticationFailedException("Invalid request");
        }

        try {
            AuthenticationData authData =
                    webAuthnManager.parseAuthenticationResponseJSON(authenticationResponseJson);

            String credentialId = base64Url(authData.getCredentialId());

            PasskeyCredential stored =
                    passkeyRepo.findByCredentialIdAndActiveTrue(credentialId)
                            .orElseThrow(() -> new AuthenticationFailedException("Credential not found"));

            //  reconstruct authenticator from stored JSON
            RegistrationData registrationData =
                    webAuthnManager.parseRegistrationResponseJSON(
                            stored.getCredentialRecordJson()
                    );

            Authenticator authenticator =
                    AuthenticatorImpl.createFromRegistrationData(registrationData);

            ServerProperty serverProperty = new ServerProperty(
                    new Origin(props.getOrigin()),
                    props.getRpId(),
                    record.challenge(),
                    null
            );

            AuthenticationParameters params =
                    new AuthenticationParameters(serverProperty, authenticator, null, false);

            webAuthnManager.verifyAuthenticationResponseJSON(authenticationResponseJson, params);

            User user = userRepository.findByUsername(username)
                    .orElseThrow();

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            return new LoginResponse(
                    token,
                    user.getUserid(),
                    user.getUsername(),
                    user.getRole().name()
            );

        } catch (Exception e) {
            throw new AuthenticationFailedException("Passkey login failed: " + e.getMessage());
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private List<PublicKeyCredentialParameters> toParams(List<Integer> algs) {
        List<PublicKeyCredentialParameters> list = new ArrayList<>();

        for (Integer alg : algs) {
            COSEAlgorithmIdentifier c =
                    (alg == -7) ? COSEAlgorithmIdentifier.ES256 : COSEAlgorithmIdentifier.RS256;

            list.add(new PublicKeyCredentialParameters(
                    PublicKeyCredentialType.PUBLIC_KEY,
                    c
            ));
        }
        return list;
    }
}
