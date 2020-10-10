package com.palgeymaim.client.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WorkIndicatorDialog {
	 
 
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Stage dialog = new Stage(StageStyle.UNDECORATED);
    private final Label label = new Label();
    private final Group root = new Group();
    private final Scene scene = new Scene(root, 200, 120, Color.WHITE);
    private final BorderPane mainPane = new BorderPane();
    private final VBox vbox = new VBox();
 
    /**
     *
     */
    public WorkIndicatorDialog(String label) {
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(null);
        dialog.setResizable(false);
        this.label.setText(label);
        this.label.setStyle("-fx-font: 14px \"Arial\";");
    }
    
    public void setProgress(double progress) {
    	 this.progressIndicator.setProgress(progress);
    }

 
    public void setTitle(String title) {
    	Platform.runLater(() -> {
    		this.label.setText(title);
    	});
    }

    public void show() {
        setupDialog();
    }
    
    public void close() {
        dialog.close();
    }

    private void setupDialog() {
        root.getChildren().add(mainPane);
        vbox.setSpacing(5);
        vbox.setAlignment(Pos.CENTER);
        vbox.setMinSize(200, 120);
        vbox.getChildren().addAll(label,progressIndicator);
        mainPane.setTop(vbox);
        dialog.setScene(scene);
        dialog.show();
    }
 
 
}