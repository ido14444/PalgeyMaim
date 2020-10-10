package com.palgeymaim.client.service;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.bacsoft.ArrayOfControllerFieldIDs;
import com.bacsoft.ArrayOfInt;
import com.bacsoft.ControllerFieldIDs;
import com.bacsoft.Details;
import com.bacsoft.NamedItemIdent;
import com.bacsoft.ResultOfArrayOfFieldValue;
import com.bacsoft.ResultOfItem;
import com.bacsoft.ResultOfPartialResultOfHistoryRegion;
import com.bacsoft.ResultOfString;
import com.bacsoft.ResultOfUser;
import com.bacsoft.Service;
import com.bacsoft.ServiceSoap;
import com.bacsoft.Status;
import com.palgeymaim.client.entity.IdModel;

public class APIService {
	
	private ServiceSoap service;
	private String token;
	private static final QName SERVICE_NAME = new QName("http://www.bacsoft.com/", "Service");
	private static APIService _instance;
	
	public APIService() {
        Service service = new Service(Service.WSDL_LOCATION, SERVICE_NAME);
        
        this.service = service.getServiceSoap();
	}
	
	public static APIService getInstance() {
		if(_instance == null) {
			_instance = new APIService();
		}
		
		return _instance;
	}
	
	public boolean login(String username, String password) {

		ResultOfString result = service.login("Palgey Maim" ,username , password);
		
		if(result.getStatus().equals(Status.OK)) {
			this.token = result.getItem();
			return true;
		} else {
			return false;
		}
	}
	
	public Optional<List<IdModel>> getAllUnits() {
		 ResultOfItem unitsInfo = service.browseV3(token, null,null);
		 return extractListOfItems(unitsInfo).map(e -> e.stream()
				 							.map(item -> new IdModel(item.getID(),item.getName()))
				 							.collect(Collectors.toList()));
	}
	
	public Optional<List<IdModel>> getAllUnitsForLevel(String levelId) {
		 ResultOfItem unitsInfo = service.browseV3(token, levelId,null);
		 return extractListOfItems(unitsInfo).map(e -> e.stream()
				 							.map(item -> new IdModel(item.getID(),item.getName()))
				 							.collect(Collectors.toList()));
	}
	
	public Optional<List<IdModel>> getControllersOfUnit(String unitId) {
		 ResultOfItem controllers = service.browseV3(token, unitId,Details.CONTROLLERS);
		 return extractListOfItems(controllers).map(e -> e.stream()
					.filter(item -> item.getID().startsWith("Controller"))
					.map(item -> new IdModel(item.getID(),item.getName()))
					.collect(Collectors.toList()));
	}
	
	
	public Optional<ResultOfArrayOfFieldValue> getFieldsValues(String controllerId, int fieldId) {
		ArrayOfInt arrOfInt = new ArrayOfInt();
		arrOfInt.getInt().add(fieldId);
		ResultOfArrayOfFieldValue res =  service.getControllerFields(token, controllerId, arrOfInt);
		if(res.getStatus().equals(Status.OK)) {
			 return Optional.of(res);
		 } else {
			 return Optional.empty();
		 }
	}
	
	
	public Optional<List<IdModel>> getAllFieldsOfController(String controllerId) {
		 ResultOfItem fields = service.browseV3(token, controllerId, Details.FIELDS);
		 return extractListOfItems(fields).map(e -> e.stream()
					.map(item -> new IdModel(item.getID(),item.getName()))
					.collect(Collectors.toList()));
	}
	
	
	
	private Optional<List<NamedItemIdent>> extractListOfItems(ResultOfItem items) {
		if(items.getStatus().equals(Status.OK)) {
			 return Optional.ofNullable(items.getItem().getChildNodes()).map(e -> e.getNamedItemIdent());
		 } else {
			 return Optional.empty();
		 }
	}
	
	
	public Optional<ResultOfPartialResultOfHistoryRegion> getPartialHistoryOfField(Set<IdModel> fieldIds, 
																		String controllerId, 
																		XMLGregorianCalendar from, 
																		XMLGregorianCalendar to, 
																		int position) throws DatatypeConfigurationException, ParseException {
		
		ArrayOfControllerFieldIDs arr = new ArrayOfControllerFieldIDs();
		ControllerFieldIDs controlFieldIds = new ControllerFieldIDs();
	    controlFieldIds.setControllerID(controllerId);
	    ArrayOfInt arrOfInt = new ArrayOfInt();
	    fieldIds.forEach(fieldId -> arrOfInt.getInt().add(Integer.valueOf(fieldId.id.split(":")[1])));
	    controlFieldIds.setFieldIDs(arrOfInt);
	    arr.getControllerFieldIDs().add(controlFieldIds);
	    ResultOfPartialResultOfHistoryRegion res =  service.getFieldsPartialHistory2(token, from, to, arr, null, 1000, position);
	    return res.getStatus().equals(Status.OK) ? Optional.of(res) : Optional.empty();
	}

	public ResultOfUser getUser() {
		return this.service.getLoggedInUser(this.token);
	}
	
	

}
