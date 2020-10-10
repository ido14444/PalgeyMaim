package com.palgeymaim.client.config;

import com.google.gson.annotations.SerializedName;

public class ChartsConfig {
	
	@SerializedName("throughputFieldId")
	private String throughputFieldId;
	@SerializedName("lastDayOverallFieldId")
	private String lastDayOverallFieldId;
	@SerializedName("currentDayOverallFieldId")
	private String currentDayOverallFieldId;
	
	public String getThroughputFieldId() {
		return throughputFieldId;
	}
	
	public String getLastDayOverallFieldId() {
		return lastDayOverallFieldId;
	}

	public String getCurrentDayOverallFieldId() {
		return currentDayOverallFieldId;
	}
	

}
