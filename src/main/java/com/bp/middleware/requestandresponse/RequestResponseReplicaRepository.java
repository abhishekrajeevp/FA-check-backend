package com.bp.middleware.requestandresponse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.user.EntityModel;

public interface RequestResponseReplicaRepository extends JpaRepository<RequestResponseReplica, Integer>{

	List<RequestResponseReplica> findByUser(EntityModel entity);

	@Query(value="SELECT * FROM req_resp_replica WHERE request_at BETWEEN :fromDate AND :toDate",nativeQuery =true)
    List<RequestResponseReplica> getByRequestDateAndTime(@Param("fromDate") String startDate,@Param("toDate") String endDate);

	@Query(value="SELECT * FROM req_resp_replica WHERE request_at BETWEEN :fromDate AND :toDate AND user_id =:userId",nativeQuery =true)
	List<RequestResponseReplica> getBetween(@Param("userId")int userId, @Param("fromDate")String fromDate, @Param("toDate")String toDate);

}
