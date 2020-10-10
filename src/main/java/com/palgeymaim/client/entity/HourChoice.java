package com.palgeymaim.client.entity;

public class HourChoice {
	
	private int hour;
	
	 public HourChoice(int hour) {
		this.hour = hour;
	}
	
	@Override
	public String toString() {
		return hour < 10 ? "0" + hour + ":00" : hour + ":00";
	}

	public int getHour() {
		return hour;
	}

}
