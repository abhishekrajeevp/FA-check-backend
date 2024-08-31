package com.bp.middleware.mandatedocument;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;

public interface MerchantKYCRepository extends JpaRepository<MerchantKYCModel,Long>{

	MerchantKYCModel getByMandateDocumentModelAndEntityModel(MandateDocumentModel mandateDoc, EntityModel entity);

	List<MerchantKYCModel> findByEntityModel(EntityModel entity);

	Optional<MerchantKYCModel> findByMandateDocumentModelAndEntityModel(MandateDocumentModel mandateDoc,EntityModel model);


}
