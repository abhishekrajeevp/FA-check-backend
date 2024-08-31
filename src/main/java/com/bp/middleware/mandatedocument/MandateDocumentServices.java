package com.bp.middleware.mandatedocument;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface MandateDocumentServices {

	ResponseStructure addMandateDocDetails(RequestModel model);

	ResponseStructure viewMandateDocDetailsById(long id);

	ResponseStructure viewAllMandateDoc();

	ResponseStructure updateMandateDoc(long id, RequestModel model);

	ResponseStructure deleteMandateDoc(long id);

	ResponseStructure viewAllMandateDocByBusinessCategory(int businessCategoryId);

	ResponseStructure updateMandateDocStatus(long id, RequestModel model);

}
