package com.minted.api.friend.repository;

import com.minted.api.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Friend> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndUserIdAndIsActiveTrue(String name, Long userId);

    Optional<Friend> findByNameAndUserIdAndIsActiveFalse(String name, Long userId);
}
