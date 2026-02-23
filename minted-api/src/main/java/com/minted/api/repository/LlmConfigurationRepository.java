package com.minted.api.repository;

import com.minted.api.entity.LlmConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LlmConfigurationRepository extends JpaRepository<LlmConfiguration, Long> {

    Optional<LlmConfiguration> findByUserId(Long userId);
}
