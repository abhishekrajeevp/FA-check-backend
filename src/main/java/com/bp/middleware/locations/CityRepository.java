package com.bp.middleware.locations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface CityRepository extends JpaRepository<CityModel, Integer>{
	List<CityModel> findByStateModel(StateModel model);

	Optional<CityModel> findById(int id);

	CityModel findByCityId(int cityId);
}
