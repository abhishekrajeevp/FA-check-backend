package com.bp.middleware.prepaidstatement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.user.EntityModel;

public interface PrepaidUserStatementRepository extends JpaRepository<PrepaidUserStatement, Integer>{

	List<PrepaidUserStatement> findByEntityModel(EntityModel entityModel);

	List<PrepaidUserStatement> findByEntityModelAndMonth(EntityModel entityModel, String month);

	List<PrepaidUserStatement> findByEntityModelAndRemark(EntityModel entityModel, String remark);
	
	@Query(value="SELECT * FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	List<PrepaidUserStatement> betweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	
	@Query(value="SELECT sum(consumed_balance) from prepaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getSummedConsumeBalance(@Param("userId")int userId);

	@Query(value="SELECT sum(credit) from prepaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getCreditedBalance(int userId);

	@Query(value="SELECT sum(debit) from prepaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getDebitedBalance(int userId);
	
	@Query(value="SELECT sum(debit_gst) from prepaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getDebitGstBalance(int userId);

	
	
	@Query(value="SELECT sum(consumed_balance) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getSummedConsumeBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(credit) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getCreditedBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(debit) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getDebitedBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(debit_gst) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getDebitGstBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	
	@Query(value="SELECT * FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	List<PrepaidUserStatement> getUsingCurrentYearMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	
	@Query(value="SELECT sum(consumed_balance) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getSummedConsumeBalanceMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	@Query(value="SELECT sum(credit) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getCreditedBalanceBetweenMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	@Query(value="SELECT sum(debit) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getDebitedBalanceBetweenMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	@Query(value="SELECT sum(debit_gst) FROM prepaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getDebitGstBalanceBetweenMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
}
