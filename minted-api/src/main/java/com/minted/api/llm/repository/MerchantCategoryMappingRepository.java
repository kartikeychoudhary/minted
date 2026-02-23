package com.minted.api.llm.repository;

import com.minted.api.llm.entity.MerchantCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantCategoryMappingRepository extends JpaRepository<MerchantCategoryMapping, Long> {

    List<MerchantCategoryMapping> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<MerchantCategoryMapping> findByIdAndUserId(Long id, Long userId);
}
