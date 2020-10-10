package com.palgeymaim.client.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.palgeymaim.client.config.MeterConfig;
import com.palgeymaim.client.utils.AlertUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddNewMeterDialogController extends Controller implements Initializable {

	@FXML
	private TextField symbnum;
	
	@FXML
	private TextField bacsoftControllerId;
	
	@FXML
	private TextField bacsoftFieldId;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		bacsoftControllerId.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	bacsoftControllerId.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		
		bacsoftFieldId.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	bacsoftFieldId.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
	}
	
	@FXML
	private void create(ActionEvent event) {
		
		Node  source = (Node)  event.getSource(); 
		
		if(this.symbnum.getText().trim().isEmpty() || this.bacsoftControllerId.getText().trim().isEmpty()
				 || this.bacsoftFieldId.getText().trim().isEmpty()) {
			AlertUtils.createErrorAlert("שגיאה", "יש צורך למלא את כל שדות הטופס");
			
		} else {
			String controllerId = "Controller:" + this.bacsoftControllerId.getText().trim();
			Map<String, MeterConfig> currentConfig = this.dataCache.getMetersConfig();
			MeterConfig meterConfig = new MeterConfig();
			meterConfig.setControllerId(controllerId);
			meterConfig.setFieldId(Integer.valueOf(bacsoftFieldId.getText().trim()));
			currentConfig.put(symbnum.getText().trim(), meterConfig);

			try (Writer writer = new FileWriter("update_data_config.json")) {
			    Gson gson = new GsonBuilder().setPrettyPrinting().create();
			    gson.toJson(currentConfig, writer);
			    AlertUtils.createInfoAlert("", "המד נוסף בהצלחה. יש להכנס מחדש למסך הקריאות על מנת לעדכן אותו. ").ifPresent(val -> {
			    	Stage stage  = (Stage) source.getScene().getWindow();
			        stage.close();
			    });
			} catch (IOException e) {
				//Show Error to user
				e.printStackTrace();
			}
		}
	}
	
	
	public void fetchData() {}
		
}
