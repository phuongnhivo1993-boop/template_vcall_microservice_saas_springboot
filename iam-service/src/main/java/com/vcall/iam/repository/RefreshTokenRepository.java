package com.vcall.iam.repository;

import com.vcall.iam.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(UUID userId);

    List<RefreshToken> findByFamily(String family);

    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);

    void deleteByUserId(UUID userId);
}
