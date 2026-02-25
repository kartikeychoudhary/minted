package com.minted.api.split.repository;

import com.minted.api.split.entity.SplitTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SplitTransactionRepository extends JpaRepository<SplitTransaction, Long> {

    List<SplitTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    Optional<SplitTransaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(ss.shareAmount), 0) FROM SplitShare ss " +
           "JOIN ss.splitTransaction st " +
           "WHERE st.user.id = :userId AND ss.friend IS NOT NULL AND ss.isPayer = false AND ss.isSettled = false")
    BigDecimal sumOwedToUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ss.shareAmount), 0) FROM SplitShare ss " +
           "JOIN ss.splitTransaction st " +
           "WHERE st.user.id = :userId AND ss.friend IS NULL AND ss.isPayer = false AND ss.isSettled = false")
    BigDecimal sumUserOwes(@Param("userId") Long userId);
}
