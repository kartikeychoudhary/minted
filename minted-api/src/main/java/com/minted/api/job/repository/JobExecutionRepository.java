package com.minted.api.job.repository;

import com.minted.api.job.entity.JobExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    Page<JobExecution> findAllByOrderByStartTimeDesc(Pageable pageable);

    List<JobExecution> findByJobNameOrderByStartTimeDesc(String jobName);
}
