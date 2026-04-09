package com.minted.api.integration.entity;

import com.minted.api.friend.entity.Friend;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_splitwise_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendSplitwiseLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false, unique = true)
    private Friend friend;

    @Column(name = "splitwise_friend_id", nullable = false)
    private Long splitwiseFriendId;

    @Column(name = "splitwise_friend_name", length = 200)
    private String splitwiseFriendName;

    @Column(name = "splitwise_friend_email", length = 255)
    private String splitwiseFriendEmail;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt = LocalDateTime.now();
}
