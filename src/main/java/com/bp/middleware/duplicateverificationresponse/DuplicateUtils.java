package com.bp.middleware.duplicateverificationresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bp.middleware.requestandresponse.RequestResponseReplica;
import com.bp.middleware.requestandresponse.RequestResponseReplicaRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.vendors.VendorVerificationModel;


@Component
public class DuplicateUtils {

	
	@Autowired 
	private RequestResponseReplicaRepository replicaRepository;
	
	public void setReqRespReplica(EntityModel entity,VendorVerificationModel vendorVerify , RequestModel model) throws Exception{
		
		
		RequestResponseReplica replica = new RequestResponseReplica();
		
		replica.setSource(model.getSource());
		replica.setSourceType(model.getSourceType());
		replica.setFilingStatus(model.isFilingStatus());
		replica.setRequestDateAndTime(model.getRequestDateAndTime());
		replica.setRequestBy(model.getRequestBy());
		replica.setOtp(model.getOtp());
		replica.setResponseDateAndTime(model.getResponseDateAndTime());
		replica.setMessage(model.getMessage());
		replica.setStatus(model.getStatus());
		replica.setDob(model.getDob());
		replica.setIfscCode(model.getIfscCode());
		replica.setCommonResponse(model.getCommonResponse());
		replica.setUser(entity);
		replica.setVerificationModel(vendorVerify);
		
		replicaRepository.save(replica);
		
	}
	
}
