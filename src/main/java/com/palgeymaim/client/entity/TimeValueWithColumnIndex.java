package com.palgeymaim.client.entity;

import com.bacsoft.TimeValue;

public class TimeValueWithColumnIndex {
	
	private TimeValue value;
	private int colIndex;
	
	public TimeValueWithColumnIndex(TimeValue value, int colIndex) {
		this.setValue(value);
		this.setColIndex(colIndex);
	}

	public TimeValue getValue() {
		return value;
	}

	public void setValue(TimeValue value) {
		this.value = value;
	}

	public int getColIndex() {
		return colIndex;
	}

	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}
	

}
