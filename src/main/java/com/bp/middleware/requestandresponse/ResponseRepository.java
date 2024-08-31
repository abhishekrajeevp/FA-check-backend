package com.bp.middleware.requestandresponse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;

public interface ResponseRepository extends JpaRepository<Response, Integer> {

	List<Response> findBySource(String source);

	Response findByResponseId(int responseId);

	@Query(value="select * from response_table where response_at between ?1 and ?2",nativeQuery = true)
	List<Response> findByResponseDateAndTime(String dateTime1, String dateTime2);

	List<Response> findByUser(Optional<EntityModel> model);

	List<Response> findByRequest(Optional<Request> req);

	List<Response> findByVendorModel(VendorModel vendorModel);

	@Query(value="select * from response_table where vendor_id =:status",nativeQuery = true)
	List<Response> findByVendorModel(@Param("status")int vendorId);

	Response findByReferenceId(String referenceId);

	Response findByClientId(String source);

	List<Response> findBySourceAndStatus(String fileNumber, String string);


	@Query(value="SELECT * FROM response_table WHERE request_at BETWEEN :fromDate AND :toDate",nativeQuery =true)
	List<Response> getByRequestDateAndTime(@Param("fromDate") String fromDate,@Param("toDate") String toDate);

	List<Response> findBySourceAndVerificationModel(String source, VendorVerificationModel vendorVerify);

	@Query(value="SELECT * FROM response_table WHERE request_at BETWEEN :fromDate AND :toDate AND vendor_id=:vendorId",nativeQuery =true)
	List<Response> getByRequestDateAndTimeAndVendor(@Param("fromDate") String fromDate,@Param("toDate") String toDate,@Param("vendorId") int vendorId);

	@Query(value="SELECT * FROM response_table WHERE request_at BETWEEN :fromDate AND :toDate AND vendor_id=:vendorId AND vendor_verification_id=:vendorVerificationId",nativeQuery =true)
	List<Response> getByRequestDateAndTimeAndVendorAndVendorVerify(@Param("fromDate") String fromDate,@Param("toDate") String toDate,@Param("vendorId") int vendorId,
			@Param("vendorVerificationId")int vendorVerificationId);

	@Query(value="SELECT * FROM response_table WHERE status <> 'expired'",nativeQuery =true)
	List<Response> getNotDeletedStatus();

	List<Response> findByVendorModelAndVerificationModel(VendorModel vendorModel,
			VendorVerificationModel vendorVerificationModel);


}
