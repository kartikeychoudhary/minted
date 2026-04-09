package com.minted.api.integration.repository;

import com.minted.api.integration.entity.FriendSplitwiseLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendSplitwiseLinkRepository extends JpaRepository<FriendSplitwiseLink, Long> {

    Optional<FriendSplitwiseLink> findByFriendId(Long friendId);

    void deleteByFriendId(Long friendId);

    List<FriendSplitwiseLink> findByFriendIdIn(List<Long> friendIds);
}
