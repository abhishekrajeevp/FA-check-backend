package com.bp.middleware.signerpositiontracker;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.signers.SignerModel;

public interface SignerPositionTrackerRepository extends JpaRepository<SignerPositionTracker, Integer>{

	List<SignerPositionTracker> findBySigner(SignerModel signer);

}
