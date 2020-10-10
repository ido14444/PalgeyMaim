package com.palgeymaim.client.main;

import java.sql.SQLException;
import java.util.Locale;

import com.palgeymaim.client.cache.DataCache;
import com.palgeymaim.client.concurrency.TaskThreadPool;
import com.palgeymaim.client.controller.Controller;
import com.palgeymaim.client.repository.AccessRepository;
import com.palgeymaim.client.utils.ScreenDimensionsUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class App extends Application
{
    public static void main( String[] args ) throws SQLException {
    	Locale.setDefault(new Locale("he"));
    	launch(args);
    }
    

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("login_layout.fxml"));
		Parent root = loader.load();
	    Controller controller = loader.getController();
	    TaskThreadPool taskThreadPool = new TaskThreadPool();
	    AccessRepository accessJdbcService = new AccessRepository();
	    DataCache dataCache = new DataCache(taskThreadPool);
	    controller.initTaskThreadPool(taskThreadPool);
	    controller.initDataCache(dataCache);
	    controller.initAccessRepository(accessJdbcService);
        
        
        Scene scene = new Scene(root, ScreenDimensionsUtil.getWidth(), ScreenDimensionsUtil.getHeight());
		
	    Scale scale = new Scale(ScreenDimensionsUtil.getWidthFactor(), ScreenDimensionsUtil.getHeightFactor(), 0, 0);
	    root.getTransforms().add(scale);
        primaryStage.setTitle("מערכת לניהול מדי מים");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        
		
	}
    
}
