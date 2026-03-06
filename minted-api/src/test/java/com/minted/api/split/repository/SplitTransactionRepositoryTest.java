package com.minted.api.split.repository;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SplitTransactionRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired SplitTransactionRepository splitTransactionRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        em.flush();
    }

    @Test
    void findByUserIdOrderByTransactionDateDesc_returnsUserSplitsOrdered() {
        SplitTransaction st1 = em.persist(buildSplit("Dinner", user1, LocalDate.of(2025, 1, 10)));
        SplitTransaction st2 = em.persist(buildSplit("Lunch", user1, LocalDate.of(2025, 1, 15)));
        em.persist(buildSplit("Other User", user2, LocalDate.of(2025, 1, 12)));
        em.flush();

        List<SplitTransaction> result = splitTransactionRepository.findByUserIdOrderByTransactionDateDesc(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Lunch");
        assertThat(result.get(1).getDescription()).isEqualTo("Dinner");
    }

    @Test
    void findByIdAndUserId_ownerFound() {
        SplitTransaction st = em.persist(buildSplit("Dinner", user1, LocalDate.of(2025, 1, 10)));
        em.flush();

        Optional<SplitTransaction> result = splitTransactionRepository.findByIdAndUserId(st.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Dinner");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        SplitTransaction st = em.persist(buildSplit("Dinner", user1, LocalDate.of(2025, 1, 10)));
        em.flush();

        Optional<SplitTransaction> result = splitTransactionRepository.findByIdAndUserId(st.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void sumOwedToUser_noShares_returnsZero() {
        BigDecimal result = splitTransactionRepository.sumOwedToUser(user1.getId());

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumUserOwes_noShares_returnsZero() {
        BigDecimal result = splitTransactionRepository.sumUserOwes(user1.getId());

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
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

    private SplitTransaction buildSplit(String description, User user, LocalDate date) {
        SplitTransaction st = new SplitTransaction();
        st.setDescription(description);
        st.setUser(user);
        st.setCategoryName("Food");
        st.setTotalAmount(BigDecimal.valueOf(200));
        st.setSplitType(SplitType.EQUAL);
        st.setTransactionDate(date);
        st.setIsSettled(false);
        return st;
    }
}
