package com.palgeymaim.client.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.palgeymaim.client.controller.UpdateMetersDataController.OnUpdateActionListener;
import com.palgeymaim.client.entity.MeterTableRow;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class UpdateStatusController extends Controller implements Initializable {

	@FXML
	private VBox vBox;
	
	@FXML
	private Button updateAll;
	
	private HashMap<String,ProgressIndicator> progressMap;
	
	private HashMap<String,Label> totalMap;
	
	private HashMap<String,Button> buttons;
	
	private HashMap<String,Label> successLabels;
	
	private HashMap<String,Label> errorLabels;
	
	private HashMap<String,Boolean> statusMap;

	private OnUpdateActionListener actionEvent;
	
	public UpdateStatusController() {
		progressMap = new HashMap<>();
		totalMap = new HashMap<>();
		buttons = new HashMap<>();
		successLabels = new HashMap<>();
		errorLabels = new HashMap<>();
		statusMap = new HashMap<>();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		vBox.setSpacing(10.0);
	}

	public void addMeter(MeterTableRow meter) {
		
		statusMap.put(meter.getMeter(), false);
		HBox hbox = new HBox();
		hbox.setBackground(new Background(new BackgroundFill(Color.WHITE,CornerRadii.EMPTY, Insets.EMPTY)));
		hbox.setStyle("-fx-font: 14px \"Arial\";");
		hbox.setMinWidth(560);
		hbox.setMinHeight(108);
		hbox.setPrefHeight(108);
		hbox.setPrefWidth(560);
		hbox.setLayoutX(10);
		hbox.setLayoutY(118);
		
		GridPane gridPane = new GridPane();
		gridPane.setPrefWidth(560);
		gridPane.setPrefHeight(108);

		for (int i = 0; i < 2; i++) {
			ColumnConstraints colCons = new ColumnConstraints();
			colCons.setHgrow(Priority.SOMETIMES);
			colCons.setMinWidth(10);
			colCons.setPrefWidth(100);
			gridPane.getColumnConstraints().add(colCons);
		}
		
		for (int i = 0; i < 3; i++) {
			RowConstraints rowCons = new RowConstraints();
			rowCons.setVgrow(Priority.SOMETIMES);
			rowCons.setMinHeight(10);
			rowCons.setPrefHeight(30);
			gridPane.getRowConstraints().add(rowCons);
		}
		
		Label meterLabel = new Label(meter.getMeter());
		Label totalLabel = new Label("סה״כ קריאה מצטברת:");
		Label totalReadLabel = new Label("");
		Label successRead = new Label("עודכן בהצלחה!");
		Label errorLabel = new Label("אירעה שגיאה !");
		successRead.setVisible(false);
		errorLabel.setVisible(false);
		errorLabel.setTextFill(Color.RED);
		successRead.setTextFill(Color.DARKGREEN);
		successLabels.put(meter.getMeter(),successRead);
		errorLabels.put(meter.getMeter(),errorLabel);
		
		Button confirmButton = new Button("עדכן קריאה");
		confirmButton.setOnAction(this.actionEvent);
		this.updateAll.setId("ALL");
		this.updateAll.setOnAction(this.actionEvent);
		this.actionEvent.initStatusController(this);
		confirmButton.setDisable(true);
		confirmButton.setId(meter.getMeter());
		buttons.put(meter.getMeter(),confirmButton);
		
		ProgressIndicator pIndicator = new ProgressIndicator(-1);
		progressMap.put(meter.getMeter(), pIndicator);
		totalMap.put(meter.getMeter(), totalReadLabel);
		
		gridPane.add(meterLabel, 1, 1);
		gridPane.add(totalLabel, 1, 2);
		gridPane.add(pIndicator, 0,1);
		gridPane.add(totalReadLabel, 0,2);
		gridPane.add(confirmButton, 0, 2);
		gridPane.add(successRead, 0, 2);
		gridPane.add(errorLabel, 0, 2);
		
		GridPane.setMargin(confirmButton, new Insets(0,0,0,10));
		
		GridPane.setMargin(successRead, new Insets(0,0,0,20));
		GridPane.setMargin(errorLabel, new Insets(0,0,0,20));
		GridPane.setHalignment(meterLabel, HPos.CENTER);
		GridPane.setValignment(meterLabel, VPos.CENTER);
		GridPane.setHalignment(totalLabel, HPos.CENTER);
		GridPane.setValignment(totalLabel, VPos.CENTER);
		GridPane.setHalignment(totalReadLabel, HPos.CENTER);
		GridPane.setValignment(totalReadLabel, VPos.CENTER);

		hbox.getChildren().add(gridPane);
		this.vBox.getChildren().add(hbox);

	}
	
	public void updateProgress(String meter, String read) {
		progressMap.get(meter).setProgress(1.0);
		totalMap.get(meter).setText(read);
		buttons.get(meter).setDisable(false);
	}
	
	public void calcReadError(String meter) {
		progressMap.get(meter).setOpacity(0);
		buttons.get(meter).setDisable(true);
	}
	
	public void updateAllBtnEnable() {
		this.updateAll.setDisable(false);
	}
	
	public void initListener(OnUpdateActionListener actionEvent) {
		this.actionEvent = actionEvent;
	}

	@Override
	public void fetchData() {}

	public void successUpdate(String id) {
		buttons.get(id).setVisible(false);
		successLabels.get(id).setVisible(true);
		statusMap.put(id, true);
	}

	public void updateError(String id) {
		buttons.get(id).setVisible(false);
		errorLabels.get(id).setVisible(true);
		statusMap.put(id, false);
	}
	
	public void checkStatus() {
		if(this.statusMap.values().stream().allMatch(e -> e.booleanValue() == true)) {
			this.updateAll.setDisable(true);
		}
	}
	
	public HashMap<String, Boolean> getStatusMap() {
		return this.statusMap;
	}

}
