package com.bp.middleware.mcccode;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface MCCRepository extends JpaRepository<MCCCodesModel, Integer> {

	MCCCodesModel findByMccId(int mccId);

	Optional<MCCCodesModel> findByMccCode(String mccCode);

	MCCCodesModel getByMccCode(String mccCode);

	List<MCCCodesModel> findByStatus(boolean b);

}
