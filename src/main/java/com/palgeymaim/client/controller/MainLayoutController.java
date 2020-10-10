package com.palgeymaim.client.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

import com.palgeymaim.client.gui.WorkIndicatorDialog;
import com.palgeymaim.client.utils.AlertUtils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainLayoutController extends Controller implements Initializable {
	
	@FXML
	private Button dataButton;
	
	@FXML
	private Button updateDataBtn;
	
	@FXML
	private Button graphBtn;
	
	@FXML
	private Label title;
	
	@FXML
	private ImageView imageView;
	
	private WorkIndicatorDialog wDialog;
	
	@FXML
	private AnchorPane pane;



	@FXML
	private void handleButtonAction(ActionEvent event) {

		String id = ((Node) event.getSource()).getId();
		String resource = null;
		
		switch(id) {
		case "graph":
			resource = "charts_layout.fxml";
			break;
		case "data":
			 resource = "data_layout.fxml";
			break;
		case "updateData":
			 resource = "update_data_layout.fxml";
			break;
		}
		
	    Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
	    
	    loadLayoutScreen(resource, stage, true);
	}



	@Override
	public void initialize(URL location, ResourceBundle resources) {
      
		dataButton.setId("data");
		graphBtn.setId("graph");	
		updateDataBtn.setId("updateData");
	
	}
	



	public void fetchData() {
		
		
		
		Platform.runLater(() -> {
			
			Task<Boolean> task = new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
						Optional<Future<?>> optionalFuture = dataCache.fetchData(wDialog);
						if(optionalFuture.isPresent()) {
							optionalFuture.get().get();
						}
						return true;
				}
			};
			
			
			wDialog = new WorkIndicatorDialog("טוען נתונים...");
			wDialog.show();

			task.setOnFailed((error) -> {
				wDialog.close();
				AlertUtils.createErrorAlert("שגיאה לא צפויה", task.getException().getMessage());
			});

			task.setOnSucceeded((value) -> {
				wDialog.close();
			});
		

			threadPool.submitTask(task);

		});
	}
		
}
