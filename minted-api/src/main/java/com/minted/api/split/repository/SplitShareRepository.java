package com.minted.api.split.repository;

import com.minted.api.split.entity.SplitShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SplitShareRepository extends JpaRepository<SplitShare, Long> {

    @Query("SELECT ss FROM SplitShare ss " +
           "JOIN ss.splitTransaction st " +
           "WHERE st.user.id = :userId AND ss.friend.id = :friendId " +
           "ORDER BY st.transactionDate DESC")
    List<SplitShare> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT ss FROM SplitShare ss " +
           "JOIN ss.splitTransaction st " +
           "WHERE st.user.id = :userId AND ss.friend.id = :friendId AND ss.isSettled = false " +
           "ORDER BY st.transactionDate DESC")
    List<SplitShare> findUnsettledByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT ss.friend.id, ss.friend.name, ss.friend.avatarColor, " +
           "SUM(CASE WHEN ss.isPayer = false THEN ss.shareAmount ELSE 0 END) - " +
           "SUM(CASE WHEN ss.isPayer = true THEN ss.shareAmount ELSE 0 END) " +
           "FROM SplitShare ss " +
           "JOIN ss.splitTransaction st " +
           "WHERE st.user.id = :userId AND ss.friend IS NOT NULL AND ss.isSettled = false " +
           "GROUP BY ss.friend.id, ss.friend.name, ss.friend.avatarColor")
    List<Object[]> findUnsettledBalancesByUserId(@Param("userId") Long userId);
}
