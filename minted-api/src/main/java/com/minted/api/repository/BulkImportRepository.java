package com.minted.api.repository;

import com.minted.api.entity.BulkImport;
import com.minted.api.enums.ImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BulkImportRepository extends JpaRepository<BulkImport, Long> {
    List<BulkImport> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<BulkImport> findByIdAndUserId(Long id, Long userId);
    List<BulkImport> findByStatus(ImportStatus status);
}
