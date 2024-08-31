package com.bp.middleware.datacomplaint;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;

public interface DataComplaintRepository extends JpaRepository<DataComplaint, Integer>{

	List<DataComplaint> findByUser(EntityModel entity);

	List<DataComplaint> findByComplaintActive(boolean complaintActive);

}
