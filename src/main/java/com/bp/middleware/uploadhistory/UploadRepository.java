package com.bp.middleware.uploadhistory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadRepository extends JpaRepository<UploadModel, Integer> {

//	List<UploadModel> findByLibraryModel(LibraryModel libraryModel);

	UploadModel findByUploadId(int uploadId);

}
