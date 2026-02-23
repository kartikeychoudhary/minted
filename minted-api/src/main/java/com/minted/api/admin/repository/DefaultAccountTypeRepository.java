package com.minted.api.admin.repository;

import com.minted.api.admin.entity.DefaultAccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefaultAccountTypeRepository extends JpaRepository<DefaultAccountType, Long> {

    Optional<DefaultAccountType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
