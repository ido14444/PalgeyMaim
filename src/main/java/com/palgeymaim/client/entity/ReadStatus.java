package com.palgeymaim.client.entity;

public enum ReadStatus {
	
	READY_FOR_UPDATE("מוכן לעדכון"),UPDATED("מעודכן"),ERROR("תקלה"),NO_READ("קריאה לא זמינה"),REPLACED("מד התאפס");
	
	private String message;
	
	ReadStatus(String message){
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	

}
