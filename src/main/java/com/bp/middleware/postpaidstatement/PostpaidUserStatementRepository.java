package com.bp.middleware.postpaidstatement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.user.EntityModel;

public interface PostpaidUserStatementRepository extends JpaRepository<PostpaidUserStatement, Integer>{

	List<PostpaidUserStatement> findByEntityModel(EntityModel entityModel);

	List<PostpaidUserStatement> findByEntityModelAndRemark(EntityModel entityModel, String remark);

	List<PostpaidUserStatement> findByEntityModelAndMonth(EntityModel entityModel, String month);

	@Query(value="SELECT * FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	List<PostpaidUserStatement> betweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	
	@Query(value="SELECT sum(consumed_balance) FROM postpaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getSummedConsumeBalance(@Param("userId")int userId);
	@Query(value="SELECT sum(credit) FROM postpaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getCreditedBalance(@Param("userId")int userId);
	@Query(value="SELECT sum(debit) FROM postpaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getDebitedBalance(@Param("userId")int userId);
	@Query(value="SELECT sum(debit_gst) FROM postpaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getDebitGstBalance(@Param("userId")int userId);
	@Query(value="SELECT sum(credit_gst) FROM postpaid_user_statement WHERE user_id =:userId",nativeQuery =true)
	String getCreditGstBalance(@Param("userId")int userId);
	
	
	@Query(value="SELECT sum(consumed_balance) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getSummedConsumeBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(credit) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getCreditedBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(debit) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getDebitedBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(debit_gst) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getDebitGstBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(credit_gst) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId",nativeQuery =true)
	String getCreditGstBalanceBetweenDates(@Param("userId")int userId, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	
	
	@Query(value="SELECT sum(consumed_balance) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getSummedConsumeBalanceMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(credit) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getCreditedBalanceMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(debit) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getDebitedBalanceMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(debit_gst) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getDebitGstBalanceMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);
	@Query(value="SELECT sum(credit_gst) FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	String getCreditGstBalanceMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	@Query(value="SELECT * FROM postpaid_user_statement WHERE date BETWEEN :startDate AND :endDate AND user_id =:userId AND month=:month",nativeQuery =true)
	List<PostpaidUserStatement> getUsingCurrentYearMonth(@Param("userId")int userId, @Param("month")String month, @Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

	
	
	
	
}
