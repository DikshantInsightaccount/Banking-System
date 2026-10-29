package com.authservice.authservice.repository;

import com.authservice.authservice.entity.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {

    List<PasskeyCredential> findByUserIdAndActiveTrue(Long userId);

    Optional<PasskeyCredential> findByCredentialIdAndActiveTrue(String credentialId);

    boolean existsByCredentialId(String credentialId);
}