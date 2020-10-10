package com.palgeymaim.client.controller;

import java.io.IOException;

import com.palgeymaim.client.cache.DataCache;
import com.palgeymaim.client.concurrency.TaskThreadPool;
import com.palgeymaim.client.repository.AccessRepository;
import com.palgeymaim.client.utils.AlertUtils;
import com.palgeymaim.client.utils.ScreenDimensionsUtil;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public abstract class Controller {
	
	protected TaskThreadPool threadPool;
	
	protected DataCache dataCache;

	protected AccessRepository accessRepository;

	public void initTaskThreadPool(TaskThreadPool threadPool) {
		this.threadPool = threadPool;
	}

	public void initDataCache(DataCache dataCache) {
		this.dataCache = dataCache;
	}
	
	public void initAccessRepository(AccessRepository accessRepository) {
		this.accessRepository = accessRepository;
	}
	
	@FXML
	protected void backToHome(Event event) {

		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		loadLayoutScreen("main_layout.fxml", stage, false);
	}
	
	
	protected void backToLogin(Stage stage) {
		loadLayoutScreen("login_layout.fxml", stage, false);
	}
	
	
	protected void loadLayoutScreen(String layoutFileName, Stage stage, boolean fetchingInitialData) {
		
		Pane root = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(layoutFileName));
			root = loader.load();
			Controller controller = loader.getController();
			controller.initDataCache(dataCache);
			controller.initTaskThreadPool(threadPool);
			controller.initAccessRepository(accessRepository);
			
		    Scale scale = new Scale(ScreenDimensionsUtil.getWidthFactor(), ScreenDimensionsUtil.getHeightFactor(), 0, 0);
		    root.getTransforms().add(scale);
			if(fetchingInitialData) {
				controller.fetchData();
			}
			
		} catch (IOException e) {
			AlertUtils.createErrorAlert("שגיאה לא צפויה", e.getMessage());
		}
		
	
        stage.getScene().setRoot(root);
        
	}
	
	public abstract void fetchData();
}
