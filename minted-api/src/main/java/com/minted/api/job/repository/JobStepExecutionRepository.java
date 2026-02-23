package com.minted.api.job.repository;

import com.minted.api.job.entity.JobStepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStepExecutionRepository extends JpaRepository<JobStepExecution, Long> {

    List<JobStepExecution> findByJobExecutionIdOrderByStepOrderAsc(Long jobExecutionId);
}
