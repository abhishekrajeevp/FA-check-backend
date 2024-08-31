package com.bp.middleware.signmerchant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.bond.MerchantBond;
import com.bp.middleware.user.EntityModel;

public interface MerchantRepository extends JpaRepository<MerchantModel, Integer> {

	MerchantModel findByMerchantId(int merchantId);

	List<MerchantModel> findByEntity(EntityModel entity);

	List<MerchantModel> findByMerchantBond(MerchantBond bond);

	@Query(value = "SELECT count(*) FROM merchant_details where bond_id > 0", nativeQuery = true)
	long getByBondPresent();

	@Query(value="SELECT * FROM merchant_details WHERE created_at BETWEEN :fromDate AND :toDate",nativeQuery =true)
	List<MerchantModel> getBetweencreatedAt(@Param("fromDate") String fromDate,@Param("toDate") String toDate);

	@Query(value="SELECT * FROM merchant_details WHERE created_at BETWEEN :fromDate AND :toDate AND user_id=:userId",nativeQuery =true)
	List<MerchantModel> getBetweencreatedAtAndEntityId(@Param("fromDate") String fromDate,@Param("toDate") String toDate,@Param("userId") int userId);

	@Query(value="SELECT count(*) FROM merchant_details WHERE created_at BETWEEN :fromDate AND :toDate AND bond_id > 0",nativeQuery =true)
	long getByBetweencreatedAtAndBondPresent(@Param("fromDate") String fromDate,@Param("toDate") String toDate);

	@Query(value="SELECT count(*) FROM merchant_details WHERE created_at BETWEEN :fromDate AND :toDate AND user_id=:userId AND bond_id > 0",nativeQuery =true)
	long getByBetweencreatedAtAndBondPresentAndEntity(@Param("fromDate") String fromDate,@Param("toDate") String toDate,@Param("userId") int userId);

	@Query(value="SELECT count(*) FROM merchant_details WHERE user_id=:userId AND bond_id > 0",nativeQuery =true)
	long getByBondPresentAndEntity(@Param("userId") int userId);

	List<MerchantModel> findByExpired(boolean expired);


}
