package com.bp.middleware.pgmode;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
public interface PGModeRepository extends JpaRepository<PGModeModel, Integer> {

	Optional<PGModeModel> findByPgOnoffStatus(int i);
	
	PGModeModel getById(int i);
	
	List<PGModeModel> getByPgOnoffStatus(int i);
}
