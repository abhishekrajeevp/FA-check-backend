package com.bp.middleware.sms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.user.EntityModel;

public interface SMSRepository extends JpaRepository<SMSEntity, Integer>{


	List<SMSEntity> getByEntityModel(EntityModel entity);
	
	List<SMSEntity> getByEntityModelAndSmsTempStatus(EntityModel entity,boolean status);

	List<SMSEntity> findBySmsTempCode(String smsTempCode);

	SMSEntity findBySmsTempId(int i);
	
	List<SMSEntity> getByAdmin(AdminDto admin);

	
	@Query(value = "select * from smstemplates where sms_temp_status=true and admin_id <>0",nativeQuery = true)
	List<SMSEntity> allAdminSms();

	//Optional<SMSEntity> findByEntityModel(EntityModel user);

	//SMSEntity getByEntity(EntityModel entityModel);


}
