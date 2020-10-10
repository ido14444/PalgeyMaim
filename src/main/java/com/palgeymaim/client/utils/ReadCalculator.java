package com.palgeymaim.client.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bacsoft.ResultOfPartialResultOfHistoryRegion;
import com.bacsoft.TimeValue;
import com.palgeymaim.client.entity.IdModel;
import com.palgeymaim.client.exception.SessionExpiredException;
import com.palgeymaim.client.service.APIService;

public class ReadCalculator {
	
	public static BigDecimal calculateBalance(String controllerId, int fieldId ,LocalDateTime from, LocalDateTime to) throws DatatypeConfigurationException, ParseException {
		
		HashSet<IdModel> a = new HashSet<>();
		
		a.add(new IdModel(":" + fieldId ,""));
		ZoneId zoneId = ZoneId.of("Asia/Jerusalem");
		
		XMLGregorianCalendar  fromXML = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(GregorianCalendar.from(from.atZone(zoneId)));
		XMLGregorianCalendar toXML = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(to.atZone(zoneId)));
		
		boolean continueToSend = true;
		
		List<TimeValue> historyValues = new ArrayList<TimeValue>();
		
		int last = 0;
		
		while(continueToSend) {
			
			Optional<ResultOfPartialResultOfHistoryRegion> res = APIService.getInstance().getPartialHistoryOfField( a, controllerId, fromXML, toXML, last);
			
			historyValues.addAll(Optional.ofNullable(res.orElseThrow(() -> new SessionExpiredException())
					.getItem()
					.getData()
					.getData())
					.orElseThrow(() -> new RuntimeException())
					.getControllerFieldsHistory()
					.get(0)
					.getFieldsHistory()
					.getFieldHistory()
					.get(0)
					.getHistory()
					.getTimeValue());
			
			historyValues.addAll(res.orElseThrow(() -> new SessionExpiredException()).getItem().getData().getData().getControllerFieldsHistory().get(0).getFieldsHistory().getFieldHistory().get(0).getHistory().getTimeValue());
			
			if(res.get().getItem().isGotAllData()) {
				continueToSend = false;
			}
			
			last+=1000;
		}
		
		BigDecimal current = (BigDecimal) historyValues.get(0).getValue();
		BigDecimal total = BigDecimal.valueOf(0);
		
		System.out.println("");
		
		for(int i =1; i<historyValues.size(); i++) {
			
			if(((BigDecimal)historyValues.get(i).getValue()).toBigInteger().compareTo(BigDecimal.valueOf(30.0).toBigInteger()) < 0) {
				BigDecimal before = (BigDecimal) historyValues.get(i - 1).getValue();
				BigDecimal balance = before.subtract(current);
				current = (BigDecimal) historyValues.get(i).getValue();
				total = total.add(balance);
			}
			
		}
		
		total = total.add(((BigDecimal)(historyValues.get(historyValues.size() - 1).getValue())).subtract(current));
		
		return total;
	}

}
