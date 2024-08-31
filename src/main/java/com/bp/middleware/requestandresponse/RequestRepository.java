package com.bp.middleware.requestandresponse;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorVerificationModel;

public interface RequestRepository extends JpaRepository<Request, Integer>{

	Request findByRequestId(int requestId);

	@Query(value="select * from request_table where user_id =:status",nativeQuery = true)
	List<Request> findByUser(@Param("status")int userId);

	
	@Query(value="select * from request_table where request_at between ?1 and ?2",nativeQuery = true)
	List<Request> findByRequestDateAndTime(Date dateTime1, Date dateTime2);

	List<Request> findByUser(EntityModel entity);

	List<Request> findByUserAndVerificationModel(EntityModel user, VendorVerificationModel vendorVerifyModel);

	Request findByReferenceId(String referenceId);

	Request findByClientId(String source);

	@Query(value="SELECT * FROM request_table WHERE request_at BETWEEN :fromDate AND :toDate",nativeQuery =true)
	List<Request> getByRequestDateAndTime(@Param("fromDate") String fromDate,@Param("toDate") String toDate);

	@Query(value="SELECT * FROM request_table WHERE request_at BETWEEN :fromDate AND :toDate AND user_id =:userId AND free_hit=false",nativeQuery =true)
	List<Request> getBetween(@Param("userId")int userId,@Param("fromDate") String fromDate,@Param("toDate") String toDate);

	@Query(value="SELECT * FROM request_table WHERE request_at BETWEEN :fromDate AND :toDate AND user_id =:userId AND vendor_verification_id =:verificationId AND free_hit=false",nativeQuery =true)
	List<Request> getBetweenVerificationtype(@Param("userId")int userId,@Param("verificationId")int verificationId,@Param("fromDate") String fromDate,@Param("toDate") String toDate);
	
	List<Request> findByUserAndFreeHit(EntityModel entity, boolean b);

	
	@Query(value="SELECT * FROM request_table WHERE request_at BETWEEN :fromDate AND :toDate AND user_id =:userId AND free_hit=true",nativeQuery =true)
	List<Request> getBetweenFreeHits(@Param("userId")int userId,@Param("fromDate") String fromDate,@Param("toDate") String toDate);

	List<Request> findByMerchant(MerchantModel merchantModel);

	@Query(value="SELECT sum(price) FROM request_table WHERE request_at BETWEEN :startDate AND :endDate AND user_id =:userId AND free_hit=false",nativeQuery =true)
	String getSummedAmount(@Param("userId")int userId,@Param("startDate") String startDate,@Param("endDate") String endDate);

	List<Request> findByUserAndVerificationModelAndFreeHit(EntityModel entityModel,
			VendorVerificationModel vendorVerificationModel, boolean b);

	Request findByMerchantAndOtp(MerchantModel merchantModel, String otpcode);

	@Query(value="SELECT sum(price) FROM request_table WHERE request_at BETWEEN :startDate AND :endDate AND user_id =:userId AND vendor_verification_id =:verificationId AND free_hit=false",nativeQuery =true)
	String getSummedAmountWithVerification(@Param("userId")int userId,@Param("verificationId")int verificationId,@Param("startDate") String startDate,@Param("endDate") String endDate);

	
	
	
	//FREE HITS
	
	
	List<Request> findByFreeHit(boolean b);
	
}

