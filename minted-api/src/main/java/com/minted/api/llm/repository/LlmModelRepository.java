package com.minted.api.llm.repository;

import com.minted.api.llm.entity.LlmModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LlmModelRepository extends JpaRepository<LlmModel, Long> {

    List<LlmModel> findByIsActiveTrueOrderByIsDefaultDescNameAsc();

    List<LlmModel> findByProviderAndIsActiveTrue(String provider);

    List<LlmModel> findAllByOrderByIsDefaultDescNameAsc();
}
