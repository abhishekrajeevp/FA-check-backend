package com.bp.middleware.erroridentifier;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ErrorIdentifierRepository extends JpaRepository<ErrorIdentifier, Integer>{

	ErrorIdentifier findByErrorReferenceNumber(String errorReferenceNumber);

	@Query(value = "SELECT * FROM error_identifier order by erroridentifier_id desc",nativeQuery = true)
	List<ErrorIdentifier> allErrors();

}
