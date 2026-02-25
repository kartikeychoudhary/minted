package com.minted.api.account.repository;

import com.minted.api.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserIdAndIsActiveTrue(Long userId);

    List<Account> findByUserId(Long userId);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

    Optional<Account> findByNameAndUserId(String name, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

    Optional<Account> findByNameAndUserIdAndIsActiveFalse(String name, Long userId);

    List<Account> findByAccountTypeIdAndUserId(Long accountTypeId, Long userId);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.isActive = true ORDER BY a.balance DESC")
    List<Account> findActiveAccountsByUserIdOrderByBalanceDesc(@Param("userId") Long userId);
}
