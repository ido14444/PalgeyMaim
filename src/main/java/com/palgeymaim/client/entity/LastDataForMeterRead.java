package com.palgeymaim.client.entity;

import java.math.BigDecimal;
import java.sql.Date;

public class LastDataForMeterRead {
	
	private BigDecimal prevRead;
	private Long madIndex;
	private Date prevDate;
	
	public LastDataForMeterRead(BigDecimal prevRead,Long madIndex, Date prevDate) {
		setPrevDate(prevDate);
		setPrevRead(prevRead);
		setMadIndex(madIndex);
	}

	public BigDecimal getPrevRead() {
		return prevRead;
	}

	public void setPrevRead(BigDecimal prevRead) {
		this.prevRead = prevRead;
	}

	public Long getMadIndex() {
		return madIndex;
	}

	public void setMadIndex(Long madIndex) {
		this.madIndex = madIndex;
	}

	public Date getPrevDate() {
		return prevDate;
	}

	public void setPrevDate(Date prevDate) {
		this.prevDate = prevDate;
	}

}
