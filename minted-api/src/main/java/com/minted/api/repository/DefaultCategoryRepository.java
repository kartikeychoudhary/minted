package com.minted.api.repository;

import com.minted.api.entity.DefaultCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefaultCategoryRepository extends JpaRepository<DefaultCategory, Long> {

    Optional<DefaultCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
