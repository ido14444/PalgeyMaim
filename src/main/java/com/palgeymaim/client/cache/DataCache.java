package com.palgeymaim.client.cache;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.palgeymaim.client.concurrency.TaskThreadPool;
import com.palgeymaim.client.config.ChartsConfig;
import com.palgeymaim.client.config.MeterConfig;
import com.palgeymaim.client.entity.IdModel;
import com.palgeymaim.client.gui.WorkIndicatorDialog;
import com.palgeymaim.client.service.APIService;

import javafx.application.Platform;

public class DataCache {

	private TaskThreadPool threadPool;

	private Map<String, List<IdModel>> controllersFieldsCache;

	private Map<String, Map<String, Boolean>> fieldsConfiguration;

	private List<IdModel> units;
	
	private int progressCounter = 0;
	
	private boolean isDataFetched;

	private Gson gson;

	private Map<String, ChartsConfig> chartsConfiguration;
	
	private Map<String, MeterConfig> metersConfig;

	public DataCache(TaskThreadPool threadPool) throws FileNotFoundException {
		
		isDataFetched = false;
		this.threadPool = threadPool;
		controllersFieldsCache = new HashMap<>();
		this.gson = new Gson();
		JsonReader fieldsJsonReader = new JsonReader(new FileReader("fields-config.json"));
		Type fieldsType = new TypeToken<Map<String, Map<String, Boolean>>>() {
		}.getType();
		this.fieldsConfiguration = this.gson.fromJson(fieldsJsonReader, fieldsType);
		JsonReader chartsJsonReader = new JsonReader(new FileReader("charts-config.json"));
		Type chartsType = new TypeToken<Map<String, ChartsConfig>>() {
		}.getType();
		this.chartsConfiguration = this.gson.fromJson(chartsJsonReader, chartsType);
		JsonReader metersJsonConfigReader = new JsonReader(new FileReader("update_data_config.json"));
		Type meterConfigType = new TypeToken<Map<String, MeterConfig>>() {
		}.getType();
		this.metersConfig = this.gson.fromJson(metersJsonConfigReader, meterConfigType);
	}

	public Map<String, List<IdModel>> getControllersCache() {
		return controllersFieldsCache;
	}

	public List<IdModel> getUnits() {
		return units;
	}


	public Map<String, Boolean> getAllFieldsOfController(String controller) {
		return this.fieldsConfiguration.get(controller);
	}
	
	public Map<String, MeterConfig> getMetersConfig() {
		return this.metersConfig;
	}

	public Optional<Future<?>> fetchData(WorkIndicatorDialog wDialog) {
		
		if(!isDataFetched) {
			
			return Optional.of(threadPool.submit(() -> {

				List<IdModel> list = new ArrayList<>();
				
				
				List<IdModel> l = APIService.getInstance().getAllUnits().get();
				
				l.forEach(e -> {
					progressCounter++;
					Platform.runLater(() -> {
						wDialog.setProgress((double)progressCounter / l.size());
					});
					
					if (e.id.startsWith("Level")) {
						APIService.getInstance().getAllUnitsForLevel(e.id).ifPresent(value -> {
							value.forEach(unit -> {
								Optional<List<IdModel>> controllers = APIService.getInstance()
										.getControllersOfUnit(unit.id);
								if (controllers.isPresent()) {
									String controllerId = controllers.get().get(0).id;
									list.add(new IdModel(controllerId,unit.name));
									APIService.getInstance().getAllFieldsOfController(controllerId).ifPresent(fields -> {
										controllersFieldsCache.put(controllerId, fields);
									});
								}
							});
						});
					} else {

						Optional<List<IdModel>> controllers = APIService.getInstance().getControllersOfUnit(e.id);

						if (controllers.isPresent()) {
							
							String controllerId = controllers.get().get(0).id;
							list.add(new IdModel(controllerId,e.name));
							APIService.getInstance().getAllFieldsOfController(controllerId).ifPresent(value -> {
								controllersFieldsCache.put(controllerId, value);
							});
						}
					}
					
				});
				this.units = list;
				isDataFetched = true;
			}));
		}
		
		return Optional.empty();

		
	}

	public Map<String, ChartsConfig> getChartsConfiguration() {
		return chartsConfiguration;
	}

}
