package com.techeazy.aws.batch2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.techeazy.aws.batch2.entity.FileUploadRecordEntity;

@Repository
public interface FileUploadRecordRepository extends JpaRepository<FileUploadRecordEntity, Long> {
}
