package com.authservice.authservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "passkey_credentials",
        indexes = {

                // ✅ Must match DB column name
                @Index(name = "idx_passkey_user", columnList = "user_id"),

                // ✅ Must match DB column name
                @Index(name = "idx_passkey_credential", columnList = "credential_id", unique = true)
        })
public class PasskeyCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ FIXED (match DB column name)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ✅ FIXED (explicit column name)
    @Column(name = "credential_id", nullable = false, unique = true, length = 600)
    private String credentialId;

    // ✅ FINAL FIX (important for your error)
    @Lob
    @Column(name = "credential_record_json", nullable = false, columnDefinition = "LONGTEXT")
    private String credentialRecordJson;

    // ✅ naming consistency (optional but good)
    @Column(name = "sign_count")
    private long signCount;

    @Column(name = "active")
    private boolean active = true;

    // ✅ Explicit column names (prevents mismatch)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
