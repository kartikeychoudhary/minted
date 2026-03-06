package com.minted.api.dashboardconfig.repository;

import com.minted.api.dashboardconfig.entity.DashboardConfiguration;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DashboardConfigurationRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired DashboardConfigurationRepository dashboardConfigurationRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        em.flush();
    }

    @Test
    void findByUserId_existingConfig_returnsConfig() {
        DashboardConfiguration config = new DashboardConfiguration();
        config.setUser(user1);
        config.setExcludedCategoryIds("1,2,3");
        em.persist(config);
        em.flush();

        Optional<DashboardConfiguration> result = dashboardConfigurationRepository.findByUserId(user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getExcludedCategoryIds()).isEqualTo("1,2,3");
    }

    @Test
    void findByUserId_noConfig_returnsEmpty() {
        Optional<DashboardConfiguration> result = dashboardConfigurationRepository.findByUserId(user1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_differentUser_returnsEmpty() {
        DashboardConfiguration config = new DashboardConfiguration();
        config.setUser(user1);
        config.setExcludedCategoryIds("5,6");
        em.persist(config);
        em.flush();

        Optional<DashboardConfiguration> result = dashboardConfigurationRepository.findByUserId(user2.getId());

        assertThat(result).isEmpty();
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
}
