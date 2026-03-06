package com.minted.api.account.repository;

import com.minted.api.account.entity.AccountType;
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
class AccountTypeRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired AccountTypeRepository accountTypeRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        em.flush();
    }

    @Test
    void findByUserIdAndIsActiveTrue_returnsOnlyActive() {
        AccountType active = em.persist(buildAccountType("Bank", user1, true));
        AccountType inactive = em.persist(buildAccountType("OldType", user1, false));
        em.flush();

        List<AccountType> result = accountTypeRepository.findByUserIdAndIsActiveTrue(user1.getId());

        assertThat(result).containsExactly(active);
        assertThat(result).doesNotContain(inactive);
    }

    @Test
    void findByUserId_returnsBothActiveAndInactive() {
        AccountType active = em.persist(buildAccountType("Bank", user1, true));
        AccountType inactive = em.persist(buildAccountType("OldType", user1, false));
        em.flush();

        List<AccountType> result = accountTypeRepository.findByUserId(user1.getId());

        assertThat(result).containsExactlyInAnyOrder(active, inactive);
    }

    @Test
    void findByIdAndUserId_rightOwner_found() {
        AccountType at = em.persist(buildAccountType("Bank", user1, true));
        em.flush();

        Optional<AccountType> result = accountTypeRepository.findByIdAndUserId(at.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bank");
    }

    @Test
    void findByIdAndUserId_wrongOwner_empty() {
        AccountType at = em.persist(buildAccountType("Bank", user1, true));
        em.flush();

        Optional<AccountType> result = accountTypeRepository.findByIdAndUserId(at.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void existsByNameAndUserId_existingName_true() {
        em.persist(buildAccountType("Wallet", user1, true));
        em.flush();

        assertThat(accountTypeRepository.existsByNameAndUserId("Wallet", user1.getId())).isTrue();
        assertThat(accountTypeRepository.existsByNameAndUserId("Wallet", user2.getId())).isFalse();
    }

    private User buildUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        return u;
    }

    private AccountType buildAccountType(String name, User user, boolean active) {
        AccountType at = new AccountType();
        at.setName(name);
        at.setUser(user);
        at.setIsActive(active);
        at.setIsDefault(false);
        return at;
    }
}
