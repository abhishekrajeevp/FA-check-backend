package com.bp.middleware.locations;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<StateModel, Integer>{
	
	List<StateModel> findByCountryModel(CountryModel model);


	StateModel findByStateId(int stateId);



}
