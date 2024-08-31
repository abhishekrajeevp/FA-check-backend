package com.bp.middleware.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.payment.PaymentModel;
import com.bp.middleware.user.EntityModel;



public interface TransactionRepository extends JpaRepository<TransactionDto, Integer>{

	@Query(value="select * from transaction_table where transaction_reference=:status",nativeQuery = true)
	TransactionDto findByTransactionReference(@Param("status") String transactionReference);

	TransactionDto findFirstByPayidOrderByCreatedDatetimeDesc(PaymentModel pay);

	Optional<TransactionDto> findByTrancsactionId(int transactionId);

	Optional<TransactionDto> findByTrackId(String merchantOrder);
	
    @Query(value ="SELECT * FROM transaction_table where created_date_time between :fromDateString and :toDateString",nativeQuery = true)
	List<TransactionDto> findByPaymentDatetime(@Param("fromDateString")String fromDateString,@Param("toDateString") String toDateString);

	TransactionDto findByPayid(PaymentModel pay);

    
    @Query(value ="SELECT * FROM transaction_table where created_date_time between :startDate and :endDate",nativeQuery = true)
	List<TransactionDto> getByCreatedDateTime(@Param("startDate")String startDate,@Param("endDate") String endDate);

    @Query(value ="SELECT * FROM transaction_table where created_date_time between :startDate and :endDate and user_id =:userId",nativeQuery = true)
	List<TransactionDto> getByCreatedDateTimeAndEntity(@Param("startDate")String startDate, @Param("endDate")String endDate,@Param("userId") int userId);

	Optional<TransactionDto> findByInvoice(String invoice);

	TransactionDto findByPrepaidId(int prepaidId);
	
//	TransactionDto findByPostpaidId(int postpaidId);
//	 @Query(value ="SELECT * FROM transaction_table where track_id=:ticketId",nativeQuery = true)
//	List<TransactionDto> getByTransactionId(int ticketId);

	TransactionDto findByInvoiceNumber(String invoiceNumber);

	TransactionDto findByPostpaidId(int prepaidId);

	

}
