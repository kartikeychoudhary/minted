package com.minted.api.split.repository;

import com.minted.api.friend.entity.Friend;
import com.minted.api.split.entity.SplitShare;
import com.minted.api.split.entity.SplitTransaction;
import com.minted.api.split.enums.SplitType;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SplitShareRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired SplitShareRepository splitShareRepository;

    private User user1;
    private Friend friend1;
    private SplitTransaction splitTx;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        friend1 = em.persist(buildFriend("Bob", user1));
        splitTx = em.persist(buildSplit("Dinner", user1));
        em.flush();
    }

    @Test
    void findByUserIdAndFriendId_returnsMatchingShares() {
        SplitShare share = buildShare(splitTx, friend1, BigDecimal.valueOf(100), false);
        em.persist(share);
        em.flush();

        List<SplitShare> result = splitShareRepository.findByUserIdAndFriendId(user1.getId(), friend1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShareAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void findUnsettledByUserIdAndFriendId_returnsOnlyUnsettled() {
        SplitShare unsettled = buildShare(splitTx, friend1, BigDecimal.valueOf(100), false);
        SplitShare settled = buildShare(splitTx, friend1, BigDecimal.valueOf(50), false);
        settled.setIsSettled(true);
        em.persist(unsettled);
        em.persist(settled);
        em.flush();

        List<SplitShare> result = splitShareRepository.findUnsettledByUserIdAndFriendId(user1.getId(), friend1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsSettled()).isFalse();
    }

    @Test
    void findUnsettledBalancesByUserId_returnsBalanceRows() {
        SplitShare share = buildShare(splitTx, friend1, BigDecimal.valueOf(100), false);
        em.persist(share);
        em.flush();

        List<Object[]> result = splitShareRepository.findUnsettledBalancesByUserId(user1.getId());

        assertThat(result).isNotEmpty();
    }

    // helpers

    private User buildUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        return u;
    }

    private Friend buildFriend(String name, User user) {
        Friend f = new Friend();
        f.setName(name);
        f.setUser(user);
        f.setIsActive(true);
        f.setAvatarColor("#6366f1");
        return f;
    }

    private SplitTransaction buildSplit(String description, User user) {
        SplitTransaction st = new SplitTransaction();
        st.setDescription(description);
        st.setUser(user);
        st.setCategoryName("Food");
        st.setTotalAmount(BigDecimal.valueOf(200));
        st.setSplitType(SplitType.EQUAL);
        st.setTransactionDate(LocalDate.of(2025, 1, 15));
        st.setIsSettled(false);
        return st;
    }

    private SplitShare buildShare(SplitTransaction st, Friend friend, BigDecimal amount, boolean isPayer) {
        SplitShare s = new SplitShare();
        s.setSplitTransaction(st);
        s.setFriend(friend);
        s.setShareAmount(amount);
        s.setIsPayer(isPayer);
        s.setIsSettled(false);
        return s;
    }
}
