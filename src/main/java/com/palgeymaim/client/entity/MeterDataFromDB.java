package com.palgeymaim.client.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MeterDataFromDB {
	
	private String place;
	private BigDecimal read;
	private LocalDateTime lastDate;
	
	public MeterDataFromDB(BigDecimal read,String place, LocalDateTime lastDate) {
		this.place = place;
		this.read = read;
		this.setLastDate(lastDate);
	}

	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public BigDecimal getRead() {
		return read;
	}
	public void setRead(BigDecimal read) {
		this.read = read;
	}

	public LocalDateTime getLastDate() {
		return lastDate;
	}

	public void setLastDate(LocalDateTime lastDate) {
		this.lastDate = lastDate;
	}

}
