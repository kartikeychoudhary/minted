package com.minted.api.integration.repository;

import com.minted.api.integration.entity.UserIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserIntegrationRepository extends JpaRepository<UserIntegration, Long> {

    Optional<UserIntegration> findByUserIdAndProvider(Long userId, String provider);

    boolean existsByUserIdAndProvider(Long userId, String provider);

    void deleteByUserIdAndProvider(Long userId, String provider);
}
