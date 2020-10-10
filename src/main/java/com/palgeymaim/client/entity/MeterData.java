package com.palgeymaim.client.entity;

import java.math.BigDecimal;

public class MeterData {
	
	private BigDecimal currentRead;
	private BigDecimal previousRead;
	private String place;
	private String lastDate;
	
	public MeterData(BigDecimal currentRead,BigDecimal previousRead, String place, String lastDate) {
		this.setCurrentRead(currentRead);
		this.setPreviousRead(previousRead);
		this.setPlace(place);
		this.setLastDate(lastDate);
	}

	public BigDecimal getCurrentRead() {
		return currentRead;
	}

	public void setCurrentRead(BigDecimal currentRead) {
		this.currentRead = currentRead;
	}

	public BigDecimal getPreviousRead() {
		return previousRead;
	}

	public void setPreviousRead(BigDecimal previousRead) {
		this.previousRead = previousRead;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getLastDate() {
		return lastDate;
	}

	public void setLastDate(String lastDate) {
		this.lastDate = lastDate;
	}
	

}
