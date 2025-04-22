package com.techeazy.aws.batch2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techeazy.aws.batch2.entity.FileUploadRecordEntity;

@Repository
public interface FileUploadRecordRepository extends JpaRepository<FileUploadRecordEntity, Long> {
	
	 @Query("SELECT f.fileName FROM FileUploadRecordEntity f WHERE f.userName = :userName")
	    List<String> findFileNamesByUserName(@Param("userName") String userName);	
	
}
