package com.bp.middleware.manualpayment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;

public interface ManualPaymentRepository extends JpaRepository<ManualPayment, Integer>{

	List<ManualPayment> findByEntity(EntityModel entity);

	Optional<ManualPayment> findByTransaction(TransactionDto transactionDto);

	@Query(value ="SELECT * FROM manual_payment where created_at between :startDate and :endDate",nativeQuery = true)
	List<ManualPayment> getByCreatedAt(@Param("startDate")String startDate,@Param("endDate")String endDate);

	@Query(value ="SELECT * FROM manual_payment where created_at between :startDate and :endDate and user_id=:userId",nativeQuery = true)
	List<ManualPayment> getByCreatedAtAndEntity(@Param("startDate")String startDate,@Param("endDate") String endDate, @Param("userId")int userId);

}
