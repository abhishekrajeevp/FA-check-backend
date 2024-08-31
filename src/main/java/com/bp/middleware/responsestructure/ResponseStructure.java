package com.bp.middleware.responsestructure;

import com.bp.middleware.user.RequestModel;

public class ResponseStructure {

	private int statusCode;
	private String message;
	private int flag;
	private Object data;
	private String fileName;
	private long count;
	private String ErrorDiscription;
	private String errorReferenceId;
	
	private RequestModel modelData;

	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getErrorDiscription() {
		return ErrorDiscription;
	}
	public void setErrorDiscription(String errorDiscription) {
		ErrorDiscription = errorDiscription;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public String getErrorReferenceId() {
		return errorReferenceId;
	}
	public void setErrorReferenceId(String errorReferenceId) {
		this.errorReferenceId = errorReferenceId;
	}
	public RequestModel getModelData() {
		return modelData;
	}
	public void setModelData(RequestModel modelData) {
		this.modelData = modelData;
	}







}
