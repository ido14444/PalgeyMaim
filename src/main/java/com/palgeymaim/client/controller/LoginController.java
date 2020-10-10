package com.palgeymaim.client.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.palgeymaim.client.gui.WorkIndicatorDialog;
import com.palgeymaim.client.service.APIService;
import com.palgeymaim.client.utils.AlertUtils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController extends Controller implements Initializable {

	
	private WorkIndicatorDialog wDialog;
	
	@FXML
	private TextField usernameTextField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private AnchorPane pane;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {}
	
	@FXML
	private void loginBtnClicked(ActionEvent event) {

		if(usernameTextField.getText().trim().length() > 0 && passwordField.getText().trim().length() > 0) {
			login(event);
		} else {
			AlertUtils.createErrorAlert("שגיאה", "נא מלא את כל השדות הנדרשים.");
		}	
	}

	private void login(ActionEvent event) {
		Task<Boolean> loginTask = new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {		
				return APIService.getInstance().login(usernameTextField.getText(),passwordField.getText());
			}
			
		};
		
		Platform.runLater(() -> {
			wDialog = new WorkIndicatorDialog("מתחבר...");
			wDialog.show();
		});
		
		this.threadPool.submitTask(loginTask);
		
		loginTask.setOnSucceeded(value -> {
			wDialog.close();
			if(loginTask.getValue()) {
				Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				loadLayoutScreen("main_layout.fxml",stage, true);
			} else {
				AlertUtils.createInvalidCredentialsAlert();
			}
			
		});
		
		loginTask.setOnFailed(value -> {
			wDialog.close();
			AlertUtils.createNetworkErrorAlert();
		});
	}
	
	
	public void fetchData() {}
		
}
