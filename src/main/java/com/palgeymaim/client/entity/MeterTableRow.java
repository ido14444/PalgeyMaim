package com.palgeymaim.client.entity;

import java.math.BigDecimal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MeterTableRow {
	
	private final StringProperty currentRead;
	private final BigDecimal currentReadDecimal;
	private final StringProperty place;
	private final StringProperty previousRead;
	private final BigDecimal previousReadDecimal;
    private final BooleanProperty chooseMeter;
    private final StringProperty status;
    private final ReadStatus statusEnum;
    private final StringProperty meter;
    private final StringProperty lastDate;
    
    public MeterTableRow(String currentRead, BigDecimal currentDecimal, String previousRead, BigDecimal previousDecimal, String meter, String status, ReadStatus statusEnum, String place, String lastDate) {
    	this.currentRead = new SimpleStringProperty(currentRead);
    	this.previousRead = new SimpleStringProperty(previousRead);
    	this.chooseMeter = new SimpleBooleanProperty(false);
    	this.status = new SimpleStringProperty(status);
    	this.meter = new SimpleStringProperty(meter);
    	this.place = new SimpleStringProperty(place);
    	this.lastDate = new SimpleStringProperty(lastDate);
    	this.statusEnum = statusEnum;
    	this.previousReadDecimal = previousDecimal;
    	this.currentReadDecimal = currentDecimal;
    }

	public BooleanProperty chooseMeterProperty() {
		return chooseMeter;
	}
	
	
	public String getCurrentRead() {
		return currentRead.get();
	}
	

	public String getPreviousRead() {
		return previousRead.get();
	}

	
	public boolean getChooseMeter() {
		return chooseMeter.get();
	}
	
	public void setChooseMeter(boolean meter) {
		chooseMeter.set(meter);
	}

	public String getMeter() {
		return meter.get();
	}

	public String getStatus() {
		return status.get();
	}

	public String getPlace() {
		return place.get();
	}

	public String getLastDate() {
		return lastDate.get();
	}

	public ReadStatus getStatusEnum() {
		return statusEnum;
	}

	public BigDecimal getCurrentReadDecimal() {
		return currentReadDecimal;
	}

	public BigDecimal getPreviousReadDecimal() {
		return previousReadDecimal;
	}
}
