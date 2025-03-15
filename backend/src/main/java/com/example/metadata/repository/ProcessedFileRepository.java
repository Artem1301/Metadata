package com.example.metadata.repository;

import com.example.metadata.model.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Long> {
}
