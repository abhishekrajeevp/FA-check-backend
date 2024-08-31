package com.bp.middleware.technical;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnicalRepository extends JpaRepository<TechnicalModel, Integer> {

	TechnicalModel findByTechnicalId(int i);

}
