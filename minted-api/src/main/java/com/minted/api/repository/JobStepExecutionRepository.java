package com.minted.api.repository;

import com.minted.api.entity.JobStepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStepExecutionRepository extends JpaRepository<JobStepExecution, Long> {

    List<JobStepExecution> findByJobExecutionIdOrderByStepOrderAsc(Long jobExecutionId);
}
