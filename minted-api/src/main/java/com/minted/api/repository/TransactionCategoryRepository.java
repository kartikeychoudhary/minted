package com.minted.api.repository;

import com.minted.api.entity.TransactionCategory;
import com.minted.api.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {

    List<TransactionCategory> findByUserIdAndIsActiveTrue(Long userId);

    List<TransactionCategory> findByUserId(Long userId);

    Optional<TransactionCategory> findByIdAndUserId(Long id, Long userId);

    List<TransactionCategory> findByUserIdAndType(Long userId, TransactionType type);

    List<TransactionCategory> findByUserIdAndTypeAndIsActiveTrue(Long userId, TransactionType type);

    List<TransactionCategory> findByUserIdAndParentIdIsNull(Long userId);

    List<TransactionCategory> findByUserIdAndParentId(Long userId, Long parentId);

    Optional<TransactionCategory> findByNameAndTypeAndUserId(String name, TransactionType type, Long userId);

    boolean existsByNameAndTypeAndUserId(String name, TransactionType type, Long userId);
}
