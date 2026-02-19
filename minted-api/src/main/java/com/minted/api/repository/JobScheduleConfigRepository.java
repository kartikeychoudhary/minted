package com.minted.api.repository;

import com.minted.api.entity.JobScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobScheduleConfigRepository extends JpaRepository<JobScheduleConfig, Long> {

    Optional<JobScheduleConfig> findByJobName(String jobName);
}
