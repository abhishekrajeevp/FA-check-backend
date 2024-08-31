package com.bp.middleware.mandatedocument;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.businesscategory.BusinessCategory;

public interface MandateDocumentRepository extends JpaRepository<MandateDocumentModel, Long>{

	Optional<MandateDocumentModel> findByKycDocNameAndBusinessCategory(String kycDocName,
			BusinessCategory findByBusinessCategoryId);

	List<MandateDocumentModel> findByBusinessCategory(BusinessCategory businessCateg);

	MandateDocumentModel findByMandateId(int mandateDocId);

}
