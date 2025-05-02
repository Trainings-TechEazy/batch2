package com.techeazy.aws.batch2.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	@Entity
	@Table(name = "users_files_details_records")
	public class UsersFilesDetailsRecordEntity {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String fileName;
	    private String userName;
	    private LocalDateTime uploadedAt;

	}