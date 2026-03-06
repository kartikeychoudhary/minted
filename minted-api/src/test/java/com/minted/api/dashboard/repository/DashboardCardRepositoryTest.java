package com.minted.api.dashboard.repository;

import com.minted.api.dashboard.entity.DashboardCard;
import com.minted.api.dashboard.enums.CardWidth;
import com.minted.api.dashboard.enums.ChartType;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DashboardCardRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired DashboardCardRepository dashboardCardRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        em.flush();
    }

    @Test
    void findByUserIdOrderByPositionOrderAsc_returnsAllCardsOrdered() {
        em.persist(buildCard("Card B", user1, 2, true));
        em.persist(buildCard("Card A", user1, 1, true));
        em.persist(buildCard("Other User", user2, 1, true));
        em.flush();

        List<DashboardCard> result = dashboardCardRepository.findByUserIdOrderByPositionOrderAsc(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Card A");
        assertThat(result.get(1).getTitle()).isEqualTo("Card B");
    }

    @Test
    void findByUserIdAndIsActiveTrueOrderByPositionOrderAsc_returnsOnlyActive() {
        em.persist(buildCard("Active", user1, 1, true));
        em.persist(buildCard("Inactive", user1, 2, false));
        em.flush();

        List<DashboardCard> result = dashboardCardRepository.findByUserIdAndIsActiveTrueOrderByPositionOrderAsc(user1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Active");
    }

    @Test
    void findByIdAndUserId_ownerFound() {
        DashboardCard card = em.persist(buildCard("My Card", user1, 1, true));
        em.flush();

        Optional<DashboardCard> result = dashboardCardRepository.findByIdAndUserId(card.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("My Card");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        DashboardCard card = em.persist(buildCard("My Card", user1, 1, true));
        em.flush();

        Optional<DashboardCard> result = dashboardCardRepository.findByIdAndUserId(card.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByIdAndUserId_deletesCorrectCard() {
        DashboardCard card = em.persist(buildCard("ToDelete", user1, 1, true));
        em.flush();

        dashboardCardRepository.deleteByIdAndUserId(card.getId(), user1.getId());
        em.flush();

        assertThat(dashboardCardRepository.findById(card.getId())).isEmpty();
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

    private DashboardCard buildCard(String title, User user, int position, boolean active) {
        DashboardCard c = new DashboardCard();
        c.setTitle(title);
        c.setUser(user);
        c.setChartType(ChartType.LINE);
        c.setXAxisMeasure("month");
        c.setYAxisMeasure("amount");
        c.setPositionOrder(position);
        c.setWidth(CardWidth.HALF);
        c.setIsActive(active);
        return c;
    }
}
