package com.bp.middleware.conveniencefee;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;


public interface ConveniencePercentageRepository extends JpaRepository<ConveniencePercentageEntity, Integer>{
 
	ConveniencePercentageEntity findByConveniencePercentage(Double conveniencePercentage);

	@Query(value = " select * from convenience_percentage where mode_id=:modeId  and convenience_id !=:convenienceId", nativeQuery = true)
	List<ConveniencePercentageEntity> findByModeOfPaymentPgAndOthers(@Param("modeId")int modeId,@Param("convenienceId")int convenienceId);

	
	List<ConveniencePercentageEntity> findByModeOfPaymentPg(ModeOfPaymentPg modeOfPaymentPg);
	
	List<ConveniencePercentageEntity> findByActiveStatus(int status);
	
	
	Optional<ConveniencePercentageEntity> findByThresholdAmountAndModeOfPaymentPg(Double thresholdAmount,
			ModeOfPaymentPg modeOfPaymentPg);

	
	@Query(value = "SELECT * FROM convenience_percentage where threshold_amount<=:totalAmount AND mode_id=:modeId and active_status=1 order by threshold_amount DESC", nativeQuery = true)
	List<ConveniencePercentageEntity> getAllActiveLessThresholdAmountIDesc(@Param("totalAmount")double totalAmount,@Param("modeId") int modeId);
	
	
	@Query(value = "select * from convenience_percentage where mode_id=:modeId and active_status=1 and threshold_amount = (select max(threshold_amount) from  convenience_percentage where mode_id=1 and active_status=1);", nativeQuery = true)
	ConveniencePercentageEntity getMaxAmount(@Param("modeId")int modeId);

	@Query(value = "select * from convenience_percentage where mode_id=:modeId and active_status=1 and threshold_amount = (select min(threshold_amount) from  convenience_percentage where mode_id=1 and active_status=1);", nativeQuery = true)
	ConveniencePercentageEntity getLowestActiveThresholdByMode(@Param("modeId")int modeId);

	@Query(value = "SELECT * FROM convenience_percentage where active_status=1 AND mode_id=:modeId order by threshold_amount ASC",nativeQuery = true)
	List<ConveniencePercentageEntity> getByModeIdAndActive(@Param("modeId")int modeId);
}
