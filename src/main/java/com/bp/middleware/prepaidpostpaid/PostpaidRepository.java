package com.bp.middleware.prepaidpostpaid;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.user.EntityModel;

public interface PostpaidRepository extends JpaRepository<PostpaidPayment, Integer>{

	List<PostpaidPayment> findByEntityModel(EntityModel entityModel);

	PostpaidPayment findByEntityModelAndPaymentFlag(EntityModel user, boolean b);

	@Query(value = "SELECT * FROM postpaid_payment WHERE paid_date BETWEEN :fromDate AND :toDate",nativeQuery = true)
	List<PostpaidPayment> findByPaidDate(@Param("fromDate") String fromDate,@Param("toDate") String toDate);

	PostpaidPayment findByEntityModelAndPayId(EntityModel entity, int payId);

	PostpaidPayment findByEntityModelAndPaymentFlagAndPayId(EntityModel entity, boolean b, int payId);

	@Query(value ="SELECT * FROM postpaid_payment where start_date between :startDate and :endDate",nativeQuery = true)
	List<PostpaidPayment> getByStartDate(@Param("startDate")String startDate,@Param("endDate") String endDate);

	@Query(value ="SELECT * FROM postpaid_payment where start_date between :startDate and :endDate and user_id=:userId",nativeQuery = true)
	List<PostpaidPayment> getByStartDateAndEntityModel(String startDate, String endDate, int userId);

	PostpaidPayment findByPayId(int payid);

	List<PostpaidPayment> findByPaymentFlag(boolean paymentFlag);

	List<PostpaidPayment> findByPaymentFlagAndEntityModel(boolean b, EntityModel entityModel);
	
	PostpaidPayment getByPaymentFlagAndEntityModel(boolean b, EntityModel entityModel);

	PostpaidPayment findByPostpaidId(int postpaidId);

	Optional<PostpaidPayment> findByUniqueId(String uniqueId);

}
