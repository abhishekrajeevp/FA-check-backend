package com.bp.middleware.prepaidpostpaid;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.user.EntityModel;

public interface PrepaidRepository extends JpaRepository<PrepaidPayment, Integer>{

	List<PrepaidPayment> findByEntityModel(EntityModel entityModel);

	
	@Query(value="SELECT * FROM prepaid_payment WHERE paid_date BETWEEN :fromDate AND :toDate",nativeQuery = true)
	List<PrepaidPayment> findByPaidDate(@Param("fromDate") String fromDate,@Param("toDate") String toDate);


	PrepaidPayment findByEntityModelAndPaymentProcess(EntityModel entity, String string);


	PrepaidPayment findByEntityModelAndPayId(EntityModel entity, int payid);


	@Query(value ="SELECT * FROM prepaid_payment where paid_date between :startDate and :endDate",nativeQuery = true)
	List<PrepaidPayment> getByPaidDate(@Param("startDate")String startDate, @Param("endDate")String endDate);

	@Query(value ="SELECT * FROM prepaid_payment where paid_date between :startDate and :endDate and user_id=:userId",nativeQuery = true)
	List<PrepaidPayment> getByPaidDateAndEntityModel(@Param("startDate")String startDate, @Param("endDate")String endDate,@Param("userId") int userId);


	Optional<PrepaidPayment> findByInvoice(String invoice);


	List<PrepaidPayment> findByRemarkAndMonth(String string, String string2);

	@Query(value ="select * from prepaid_payment where user_id=:userId AND remark=:remark And month=:month AND prepaid_id!=:prepaidId",nativeQuery = true)// ;
	List<PrepaidPayment> getByPrepaidIdAndMonth(@Param("remark")String remark, @Param("month")String month, @Param("userId")int userId,@Param("prepaidId") int prepaidId);//, 


	List<PrepaidPayment> findByEntityModelAndMonth(EntityModel entityModel, String month);


	PrepaidPayment findByPrepaidId(int prepaidId);


	Optional<PrepaidPayment> findByUniqueId(String uniqueId);

}
