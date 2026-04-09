package com.minted.api.integration.repository;

import com.minted.api.integration.entity.SplitSplitwisePush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitSplitwisePushRepository extends JpaRepository<SplitSplitwisePush, Long> {

    Optional<SplitSplitwisePush> findBySplitTransactionIdAndUserId(Long splitTransactionId, Long userId);

    boolean existsBySplitTransactionIdAndUserId(Long splitTransactionId, Long userId);

    List<SplitSplitwisePush> findBySplitTransactionIdInAndUserId(List<Long> splitTransactionIds, Long userId);
}
