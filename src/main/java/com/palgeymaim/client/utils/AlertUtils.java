package com.palgeymaim.client.utils;

import java.util.Optional;

import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

public class AlertUtils {
	
	
	public static Optional<ButtonType> createSessionNotValidAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().setStyle("-fx-font: 14px \"Arial\";");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setTitle("שגיאה");
		alert.setHeaderText("תוקף ההתחברות שלך פג");
		alert.setContentText("יש להתחבר מחדש");
		
		ButtonType buttonTypeOne = new ButtonType("התחבר");
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(buttonTypeOne);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
	}

	public static Optional<ButtonType> createNetworkErrorAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().setStyle("-fx-font: 14px \"Arial\";");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setTitle("שגיאה");
		alert.setHeaderText("נראה כי החיבור לשרת לא הצליח");
		alert.setContentText("יש לבדוק את החיבור לאינטרנט");
		
		ButtonType buttonTypeOne = new ButtonType("OK");
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(buttonTypeOne);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
		
	}
	
	public static Optional<ButtonType> createDatabaseAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().setStyle("-fx-font: 14px \"Arial\";");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setTitle("שגיאה");
		alert.setHeaderText("שגיאה בחיבור למסד הנתונים");
		
		ButtonType buttonTypeOne = new ButtonType("OK");
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(buttonTypeOne);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
		
	}
	
	public static Optional<ButtonType> createInvalidCredentialsAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().setStyle("-fx-font: 14px \"Arial\";");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setTitle("שגיאה");
		alert.setHeaderText("שם משתמש או סיסמה לא נכונים");
		alert.setContentText("יש לנסות שוב");
		
		ButtonType buttonTypeOne = new ButtonType("נסה שוב");
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(buttonTypeOne);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
	}
	
	public static Optional<ButtonType> createInfoAlert(String header, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.getDialogPane().setStyle("-fx-font: 14px \"Arial\";");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setTitle("");
		alert.setHeaderText(header);
		alert.setContentText(content);
		
		ButtonType buttonTypeOne = new ButtonType("OK");
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(buttonTypeOne);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
	}
	
	public static Optional<ButtonType> createErrorAlert(String header, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().setStyle("-fx-font: 14px \"Arial\";");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setTitle("");
		alert.setHeaderText(header);
		alert.setContentText(content);
		
		ButtonType buttonTypeOne = new ButtonType("OK");
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(buttonTypeOne);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
	}

}
