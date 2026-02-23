package com.minted.api.account.repository;

import com.minted.api.account.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {

    List<AccountType> findByUserIdAndIsActiveTrue(Long userId);

    List<AccountType> findByUserId(Long userId);

    Optional<AccountType> findByIdAndUserId(Long id, Long userId);

    Optional<AccountType> findByNameAndUserId(String name, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);
}
