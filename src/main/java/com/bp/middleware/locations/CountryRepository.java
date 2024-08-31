package com.bp.middleware.locations;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<CountryModel, Integer> {
	

	CountryModel findByCountryId(int countryId);

	

}
