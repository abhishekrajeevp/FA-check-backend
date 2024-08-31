package com.bp.middleware.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.payment.PaymentMethod;

public interface UserRepository extends JpaRepository<EntityModel, Integer>{

	EntityModel findByEmail(String email);
	EntityModel findByMobileNumber(String number);
	
	@Query(value="select * from user_model where account_status=:status",nativeQuery = true)
	List<EntityModel> getByAccountStatus(@Param("status") boolean accountStatus);
	
	EntityModel findByUserId(int userId);
	
	List<EntityModel> findByPaymentMethod(PaymentMethod paymentMethod);
	
	List<EntityModel> findByPostpaidFlag(boolean flagStatus);
	EntityModel findByApiKeyAndApplicationId(String apiKey, String applicationId);
	
	List<EntityModel> findBySigningRequired(boolean b);
	
	List<EntityModel> findByVerificationRequired(boolean b);
	EntityModel findByApiSandboxKeyAndApplicationId(String apiKey, String applicationId);
	
	List<EntityModel> getByUserId(int userId);
	
	Optional<EntityModel> findBySaltKey(String saltKey);
	
}