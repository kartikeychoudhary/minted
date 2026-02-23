package com.minted.api.repository;

import com.minted.api.entity.MerchantCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantCategoryMappingRepository extends JpaRepository<MerchantCategoryMapping, Long> {

    List<MerchantCategoryMapping> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<MerchantCategoryMapping> findByIdAndUserId(Long id, Long userId);
}
