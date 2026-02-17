package com.minted.api.repository;

import com.minted.api.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndMonthAndYearAndCategoryId(
            Long userId, Integer month, Integer year, Long categoryId
    );

    List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
           "AND b.year = :year ORDER BY b.month ASC")
    List<Budget> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") Integer year);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
           "AND b.year = :year AND b.month BETWEEN :startMonth AND :endMonth " +
           "ORDER BY b.month ASC")
    List<Budget> findByUserIdAndYearAndMonthBetween(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("startMonth") Integer startMonth,
            @Param("endMonth") Integer endMonth
    );

    boolean existsByUserIdAndMonthAndYearAndCategoryId(
            Long userId, Integer month, Integer year, Long categoryId
    );
}
