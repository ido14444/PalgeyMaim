package com.palgeymaim.client.controller;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.bacsoft.ResultOfArrayOfFieldValue;
import com.bacsoft.ResultOfUser;
import com.bacsoft.Status;
import com.maimart.fx.tablefilter.TableFilter;
import com.palgeymaim.client.cache.DataCache;
import com.palgeymaim.client.concurrency.TaskThreadPool;
import com.palgeymaim.client.entity.MeterData;
import com.palgeymaim.client.entity.MeterDataFromDB;
import com.palgeymaim.client.entity.MeterTableRow;
import com.palgeymaim.client.entity.ReadStatus;
import com.palgeymaim.client.exception.SessionExpiredException;
import com.palgeymaim.client.gui.WorkIndicatorDialog;
import com.palgeymaim.client.repository.AccessRepository;
import com.palgeymaim.client.service.APIService;
import com.palgeymaim.client.utils.AlertUtils;
import com.palgeymaim.client.utils.ReadCalculator;
import com.palgeymaim.client.utils.ScreenDimensionsUtil;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class UpdateMetersDataController extends Controller implements Initializable {

	@FXML
	private TableView<MeterTableRow> dataTable;

	TableColumn<MeterTableRow, String> status;

	@FXML
	private AnchorPane pane;

	private WorkIndicatorDialog wDialog;

	private Map<String, ChangeListener<Boolean>> listeners;

	private Map<String, MeterTableRow> choosedMeters;

	public UpdateMetersDataController() {
		listeners = new HashMap<>();
		choosedMeters = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		dataTable.setStyle("-fx-font: 12px \"Arial\";");
		dataTable.setPlaceholder(new Label("אין נתונים להצגה"));
		status = new TableColumn<>("מצב");
		TableColumn<MeterTableRow, String> currentRead = new TableColumn<>("קריאה נוכחית (Bacsoft)");
		TableColumn<MeterTableRow, String> prevRead = new TableColumn<>("קריאה קודמת");
		TableColumn<MeterTableRow, String> lastDate = new TableColumn<>("תאריך עדכון אחרון");
		TableColumn<MeterTableRow, String> place = new TableColumn<>("מקום");
		TableColumn<MeterTableRow, String> meter = new TableColumn<>("מד מים");
		TableColumn<MeterTableRow, Boolean> chooseMeter = new TableColumn<>("בחר הכל");
		String css = this.getClass().getClassLoader().getResource("style.css").toExternalForm(); 
		
		Platform.runLater(() -> {
			pane.getScene().getStylesheets().add(css);
			pane.getStylesheets().add("style.css");
		});
		
		CheckBox selectAll = new CheckBox();
		selectAll.selectedProperty().addListener(new CustomChangeListener(choosedMeters,dataTable));

		chooseMeter.setGraphic(selectAll);
		
		status.setCellValueFactory(new PropertyValueFactory<>("status"));
		currentRead.setCellValueFactory(new PropertyValueFactory<>("currentRead"));
		prevRead.setCellValueFactory(new PropertyValueFactory<>("previousRead"));
		place.setCellValueFactory(new PropertyValueFactory<>("place"));
		lastDate.setCellValueFactory(new PropertyValueFactory<>("lastDate"));
		meter.setCellValueFactory(new PropertyValueFactory<>("meter"));
		chooseMeter.setCellValueFactory(new PropertyValueFactory<>("chooseMeter"));
		chooseMeter.setCellFactory(CheckBoxTableCell.forTableColumn((param) -> {
			return chooseMeter.getCellObservableValue(param);
		}));
		chooseMeter.setVisible(true);
		chooseMeter.setEditable(true);
		dataTable.getColumns().addAll(status, currentRead, prevRead,lastDate, place, meter, chooseMeter);

		dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		dataTable.getColumns().forEach(col -> col.setStyle("-fx-alignment: CENTER;"));
		dataTable.setRowFactory(tv -> new RowFactory(this.listeners, this.choosedMeters, this.dataTable, selectAll));

		dataTable.setEditable(true);

	}
	
	private static class RowFactory extends TableRow<MeterTableRow>{
		
		private WeakReference<CheckBox> selectAll;
		private WeakReference<TableView<MeterTableRow>> dataTable;
		private WeakReference<Map<String, MeterTableRow>> choosedMeters;
		private WeakReference<Map<String, ChangeListener<Boolean>> >listeners;

		public RowFactory(Map<String, ChangeListener<Boolean>> listeners, Map<String, MeterTableRow> choosedMeters , TableView<MeterTableRow> dataTable, CheckBox selectAll) {
			this.selectAll = new WeakReference<>(selectAll);
			this.dataTable =  new WeakReference<>(dataTable);;
			this.choosedMeters =  new WeakReference<>(choosedMeters);;
			this.listeners =  new WeakReference<>(listeners);
		}
		
		@Override
		protected void updateItem(MeterTableRow meterTableRow, boolean empty) {
			
		
			if(!empty && !(meterTableRow.getStatusEnum().equals(ReadStatus.READY_FOR_UPDATE) || meterTableRow.getStatusEnum().equals(ReadStatus.REPLACED))){
				setEditable(false);
				setDisable(true);
			} else {
				setEditable(true);
				setDisable(false);
			}
			super.updateItem(meterTableRow, empty);
			
			if(!dataTable.get().getItems().isEmpty()) {
				
				dataTable.get().getItems().forEach(item -> item.chooseMeterProperty().setValue(false));
				selectAll.get().selectedProperty().setValue(false);
				choosedMeters.clear();
			}
			
			if (meterTableRow != null) {
				
				ChangeListener<Boolean> listener = (obs, prev, newVal) -> {
					if (newVal) {
						setStyle("-fx-background-color:#FFFFCC");
					} else {
						setStyle(null);
					}
				};

				if (listeners.get().get(meterTableRow.getMeter()) != null) {
					meterTableRow.chooseMeterProperty().removeListener(listeners.get().get(meterTableRow.getMeter()));
				}

				meterTableRow.chooseMeterProperty().addListener(listener);
				listeners.get().put(meterTableRow.getMeter(), listener);
			}

		}
	}
	
	private static class CustomChangeListener implements ChangeListener<Boolean>{

		private WeakReference<TableView<MeterTableRow>> dataTable;
		private WeakReference<Map<String, MeterTableRow>> choosedMeters;

		public CustomChangeListener(Map<String, MeterTableRow> choosedMeters , TableView<MeterTableRow> dataTable) {
			this.dataTable = new WeakReference<>(dataTable);
			this.choosedMeters = new WeakReference<>(choosedMeters);
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			choosedMeters.get().clear();
			if (!dataTable.get().getItems().isEmpty()) {
                for (int i = 0; i < dataTable.get().getItems().size(); i++) {
                	if(dataTable.get().getItems().get(i).getStatusEnum().equals(ReadStatus.REPLACED) || dataTable.get().getItems().get(i).getStatusEnum().equals(ReadStatus.READY_FOR_UPDATE)) {
                		dataTable.get().getItems().get(i).chooseMeterProperty().setValue(newValue);
                		if(newValue) {
                    		choosedMeters.get().put(dataTable.get().getItems().get(i).getMeter(), dataTable.get().getItems().get(i));
                    	} 
                	}
                }
			}
		}
			
	}
	
	
	private static class BacsoftFetcher extends Task<Map<String, MeterData>>{
		
		private WeakReference<DataCache> dataCache;
		private WeakReference<WorkIndicatorDialog> workIndicatorDialog;
		private WeakReference<TaskThreadPool> threadPool;
		private Map<String, MeterDataFromDB> accessData;
		
		public BacsoftFetcher(DataCache dataCache, WorkIndicatorDialog workIndicatorDialog,TaskThreadPool threadPool, Map<String, MeterDataFromDB> accessData) {
			this.dataCache = new WeakReference<DataCache>(dataCache);
			this.workIndicatorDialog = new WeakReference<>(workIndicatorDialog);
			this.threadPool = new WeakReference<>(threadPool);
			this.accessData = accessData;
		}

		@Override
		protected Map<String, MeterData> call() throws Exception {
			
			Platform.runLater(() -> {
				workIndicatorDialog.get().setTitle("קורא נתונים מ-Bacsoft...");
			});
			
			
			List<Callable<Optional<ResultOfArrayOfFieldValue>>> tasks = dataCache.get().getMetersConfig().entrySet().stream()
					.map(entry -> {

						return new Callable<Optional<ResultOfArrayOfFieldValue>>() {

							@Override
							public Optional<ResultOfArrayOfFieldValue> call() throws Exception {
								return APIService.getInstance().getFieldsValues(
										entry.getValue().getControllerId(), entry.getValue().getFieldId());
								
							}
						};
					}).collect(Collectors.toList());

			Map<String, BigDecimal> serverRes = new HashMap<>();

			Iterator<String> it = dataCache.get().getMetersConfig().keySet().iterator();

			for (Future<Optional<ResultOfArrayOfFieldValue>> task : threadPool.get().invokeAll(tasks)) {
				
				Optional<ResultOfArrayOfFieldValue> res = task.get();
				res.ifPresent(value -> {
						serverRes.put(it.next(), value.getItem().getFieldValue().get(0).getValue());
				});

			}

			Map<String, MeterData> finalRes = new HashMap<>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
			accessData.entrySet().stream().filter(entry -> serverRes.get(entry.getKey()) != null).forEach(entry -> {
				MeterData meterData = new MeterData(serverRes.get(entry.getKey()),
						accessData.get(entry.getKey()).getRead(), accessData.get(entry.getKey()).getPlace(),formatter.format(accessData.get(entry.getKey()).getLastDate()));
				finalRes.put(entry.getKey(), meterData);
			});

			return finalRes;
		}
		
	}
	
	
	
	private static class AccessFetcher extends Task<Map<String, MeterDataFromDB>>{
		
		private WeakReference<AccessRepository> accessRepository;
		private WeakReference<DataCache> dataCache;
		private WeakReference<WorkIndicatorDialog> workIndicatorDialog;
		
		public AccessFetcher(AccessRepository accessRepository, DataCache dataCache, WorkIndicatorDialog workIndicatorDialog) {
			this.accessRepository = new WeakReference<>(accessRepository);
			this.dataCache = new WeakReference<DataCache>(dataCache);
			this.workIndicatorDialog = new WeakReference<>(workIndicatorDialog);
		}

		@Override
		protected Map<String, MeterDataFromDB> call() throws Exception {
			
			ResultOfUser user = APIService.getInstance().getUser();
			
			Platform.runLater(() -> {
				this.workIndicatorDialog.get().setTitle("טוען נתונים מה Access...");
			});
			
			
			if(!user.getStatus().equals(Status.OK) || user.getItem() == null) {
				throw new SessionExpiredException();
			}
			
			return accessRepository.get().getAllPreviousReads(this.dataCache.get().getMetersConfig().keySet());
		}
		
	}
	
	@Override
	public void fetchData() {
		
		wDialog = new WorkIndicatorDialog("בודק סטטוס התחברות...");
		wDialog.show();
		AccessFetcher accessTask = new AccessFetcher(accessRepository, dataCache, wDialog);

		this.threadPool.submitTask(accessTask);

		accessTask.setOnSucceeded(value -> {
			getAllData(accessTask.getValue());
		});

		accessTask.setOnFailed(value -> {
			wDialog.close();
			if (accessTask.getException() instanceof SessionExpiredException || accessTask.getException().getCause() instanceof SessionExpiredException) {
				AlertUtils.createSessionNotValidAlert().ifPresent(e -> {
					backToLogin((Stage) pane.getScene().getWindow());
				});
				} else {
					AlertUtils.createErrorAlert("שגיאה לא צפויה", accessTask.getException().getMessage());
				}
		});

	}

	private void getAllData(Map<String,MeterDataFromDB> accessData) {

		BacsoftFetcher bacsoftFetcher = new BacsoftFetcher(dataCache, wDialog, threadPool, accessData);

		this.threadPool.submitTask(bacsoftFetcher);

		bacsoftFetcher.setOnSucceeded(value -> {

			Map<String, MeterData> result = bacsoftFetcher.getValue();
			DecimalFormat decimalFormat = new DecimalFormat("###,###.###");
 
			Platform.runLater(() -> {

				ObservableList<MeterTableRow> list = FXCollections.observableArrayList();

				result.entrySet().forEach(entry -> {
					
					ReadStatus readStatus;
					if(entry.getValue().getCurrentRead().compareTo(BigDecimal.valueOf(1)) < 0) {
						readStatus = ReadStatus.NO_READ;
					} else if(entry.getValue().getCurrentRead().compareTo(entry.getValue().getPreviousRead()) > 0) {
						readStatus = ReadStatus.READY_FOR_UPDATE;
					} else if(entry.getValue().getCurrentRead().compareTo(entry.getValue().getPreviousRead()) < 0){
						readStatus = ReadStatus.REPLACED;
					}  else {
						readStatus = ReadStatus.UPDATED;
					}
					
					MeterTableRow meterDataRow = new MeterTableRow(
							decimalFormat.format(entry.getValue().getCurrentRead()),
							entry.getValue().getCurrentRead(),
							decimalFormat.format(entry.getValue().getPreviousRead()),
							entry.getValue().getPreviousRead(),
							entry.getKey(),
							readStatus.getMessage(), readStatus, entry.getValue().getPlace(),entry.getValue().getLastDate());
					meterDataRow.chooseMeterProperty().addListener((obs, oldVal, newVal) -> {
						if (newVal) {
							choosedMeters.put(meterDataRow.getMeter(), meterDataRow);
						} else {
							choosedMeters.remove(meterDataRow.getMeter());
						}

					});

					list.add(meterDataRow);
				});
				dataTable.setItems(list);
				dataTable.refresh();
				TableFilter<MeterTableRow> tableFilter = new TableFilter<>(dataTable);
				tableFilter.filterColumn(status);
				wDialog.close();
			});

		});

		bacsoftFetcher.setOnFailed(value -> {

			wDialog.close();
			if (bacsoftFetcher.getException() instanceof SessionExpiredException || bacsoftFetcher.getException().getCause() instanceof SessionExpiredException) {
				AlertUtils.createSessionNotValidAlert().ifPresent(e -> {
					backToLogin((Stage) pane.getScene().getWindow());
				});
				
			} else {
					AlertUtils.createErrorAlert("שגיאה לא צפויה", bacsoftFetcher.getException().getMessage());
			}

		});
	}
	
	
	private static class CalcReadTask extends Task<Map<String,BigDecimal>>{
		
		private WeakReference<UpdateMetersDataController> controller;
		private UpdateStatusController modalController;
		private ConcurrentHashMap<String, Map<String,Object>> tempRes;
		
		public CalcReadTask(UpdateMetersDataController controller, ConcurrentHashMap<String, Map<String,Object>> hashMap) {
			this.controller = new WeakReference<>(controller);
			this.tempRes = hashMap;
		}

		@Override
		protected Map<String,BigDecimal> call() throws Exception {
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
			DecimalFormat decimalFormat = new DecimalFormat("###,###.###");
			
			ResultOfUser resultOfUser = APIService.getInstance().getUser();
			if(resultOfUser.getStatus() != Status.OK || resultOfUser.getItem() == null) {
				throw new SessionExpiredException();
			}
			
			List<Callable<BigDecimal>> callables = new ArrayList<>();
			
			for(Entry<String,MeterTableRow> entry : this.controller.get().choosedMeters.entrySet()) {
				
				callables.add(() -> {
					
					LocalDateTime from  = LocalDateTime.parse(entry.getValue().getLastDate(),formatter);
					String controllerId = this.controller.get().dataCache.getMetersConfig().get(entry.getKey()).getControllerId();
					int fieldId = this.controller.get().dataCache.getMetersConfig().get(entry.getKey()).getFieldId();
					LocalDateTime now = LocalDateTime.now();
					BigDecimal res;
					if(entry.getValue().getStatusEnum().equals(ReadStatus.READY_FOR_UPDATE) || entry.getValue().getStatusEnum().equals(ReadStatus.UPDATED)) {
						res =  entry.getValue().getCurrentReadDecimal().subtract(entry.getValue().getPreviousReadDecimal());
					} else {
						res = ReadCalculator.calculateBalance(controllerId, fieldId, from, now);
					}
					
					Platform.runLater(() -> {;
						this.modalController.updateProgress(entry.getKey(), decimalFormat.format(res));
					});
					
					Map<String,Object> resOfMeter = new HashMap<String,Object>();
					resOfMeter.put("BALANCE", res);
					resOfMeter.put("DATE", now);
					this.tempRes.put(entry.getKey(), resOfMeter);
					return res;
				});
			
			}
			
			Map<String,BigDecimal> result = new HashMap<String,BigDecimal>();
			Iterator<Entry<String,MeterTableRow>> it = this.controller.get().choosedMeters.entrySet().iterator();
			for(Future<BigDecimal> future : this.controller.get().threadPool.invokeAll(callables)) {
					String key = it.next().getKey();
					try {
						BigDecimal balance = future.get();
						result.put(key, balance);
					} catch(Throwable e) {
						Platform.runLater(() -> {;
						this.modalController.calcReadError(key);
						});
					}
					
			}
			
			return result;
		
		}

		public void initModalController(UpdateStatusController controller) {
			this.modalController = controller;
			
		}
		
	}
	
	@FXML
	private void updateMetersRead(ActionEvent event) throws Throwable {
		
		if(choosedMeters.size() > 0) {
			
			ConcurrentHashMap<String,Map<String,Object>> tempRes = new ConcurrentHashMap<>();
			CalcReadTask task = new CalcReadTask(this,tempRes);
			
			UpdateStatusController controller = openUpdateStatusAlert(event,task,tempRes);
			
			for(MeterTableRow meter : choosedMeters.values()) {
				controller.addMeter(meter);
			}
			
			task.initModalController(controller);
			
			this.threadPool.submitTask(task);
			
			task.setOnSucceeded(value -> {
				Map<String,BigDecimal> result = task.getValue();
				if(result.size() > 0) {
					controller.updateAllBtnEnable();
				}
			});
			
		}
		
	}

	@FXML
	private void createNewMeter(ActionEvent event) throws IOException {

		Stage stage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("add_new_meter_dialog.fxml"));
		Pane root = loader.load();
		Controller controller = loader.getController();
		controller.initDataCache(this.dataCache);
		controller.initAccessRepository(this.accessRepository);
		controller.initTaskThreadPool(this.threadPool);
		stage.setScene(new Scene(root));
		stage.setTitle("הוספת מד חדש");
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(((Node) event.getSource()).getScene().getWindow());
	    Scale scale = new Scale(ScreenDimensionsUtil.getWidthFactor(), ScreenDimensionsUtil.getHeightFactor(), 0, 0);
	    
		root.setPrefWidth(root.getPrefWidth() * ScreenDimensionsUtil.getWidthFactor());
		root.setPrefHeight(root.getPrefHeight() * ScreenDimensionsUtil.getHeightFactor());
		root.getTransforms().add(scale);
		
		stage.show();
	}
	
	
	private UpdateStatusController openUpdateStatusAlert(ActionEvent event, Task<?> task, ConcurrentHashMap<String, Map<String,Object>> tempRes) throws IOException {

		Stage stage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("update_status.fxml"));
		Pane root = loader.load();
		UpdateStatusController controller = loader.getController();
		controller.initListener(new OnUpdateActionListener(this.accessRepository, tempRes));
		controller.initAccessRepository(accessRepository);
		controller.initTaskThreadPool(threadPool);
		controller.initDataCache(dataCache);
		stage.setScene(new Scene(root));
		stage.setTitle("סטטוס עדכון קריאות");
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(((Node) event.getSource()).getScene().getWindow());
	    Scale scale = new Scale(ScreenDimensionsUtil.getWidthFactor(), ScreenDimensionsUtil.getHeightFactor(), 0, 0);
		root.setPrefWidth(root.getPrefWidth() * ScreenDimensionsUtil.getWidthFactor());
		root.setPrefHeight(root.getPrefHeight() * ScreenDimensionsUtil.getHeightFactor());
		root.getTransforms().add(scale);
		stage.setOnCloseRequest(new OnCloseEventHandler(task,this));
		stage.show();
		return controller;
	}
	
	public static class OnUpdateActionListener implements EventHandler<ActionEvent>{

		private ConcurrentHashMap<String, Map<String,Object>> tempRes;
		private AccessRepository accessRepo;
		private WeakReference<UpdateStatusController> updateStatusController;
		
		public OnUpdateActionListener(AccessRepository accessRepo , ConcurrentHashMap<String, Map<String,Object>> tempRes) {
			this.tempRes = tempRes;
			this.accessRepo = accessRepo;
		}
		
		public void initStatusController(UpdateStatusController controller) {
			this.updateStatusController = new WeakReference<>(controller);
		}
		
		@Override
		public void handle(ActionEvent event) {
			
			Button b = (Button) event.getSource();
			
			Task<Map<String,Boolean>> task = new Task<Map<String,Boolean>>(){

				@Override
				protected Map<String, Boolean> call() throws Exception {
					
					Map<String,Boolean> result = new HashMap<>();
					
					if(!b.getId().equals("ALL")) {
						try {
							accessRepo.insertNewMeterRead(b.getId(), (BigDecimal)tempRes.get(b.getId()).get("BALANCE"), (LocalDateTime)tempRes.get(b.getId()).get("DATE"));
							result.put(b.getId(),true);
							if(updateStatusController != null) {
								updateStatusController.get().successUpdate(b.getId());
							}
						} catch (Throwable e) {
							if(updateStatusController != null) {
								updateStatusController.get().updateError(b.getId());
								result.put(b.getId(),false);
							}
						}
					} else {
						for(Entry<String,Map<String,Object>> entry : tempRes.entrySet()) {
							if(!updateStatusController.get().getStatusMap().get(entry.getKey())) {
								try {
									accessRepo.insertNewMeterRead(entry.getKey(), (BigDecimal)tempRes.get(entry.getKey()).get("BALANCE"), (LocalDateTime)tempRes.get(entry.getKey()).get("DATE"));
									updateStatusController.get().successUpdate(entry.getKey());
									result.put(b.getId(),true);
								} catch (Throwable e) {
									updateStatusController.get().updateError(entry.getKey());
									result.put(b.getId(),false);
								}
							}
						}
					}
					
					return result;
				}
				
			};
			
			task.setOnFailed(value -> {
				AlertUtils.createErrorAlert("שגיאה לא צפויה", task.getException().getMessage());
			});
			
			task.setOnSucceeded(value -> {	
				if(b.getId().equals("ALL")){
					AlertUtils.createInfoAlert("התהליך הסתיים!", "תוכל לבדוק את הסטטוס ברשימה.");
				} else {
						AlertUtils.createInfoAlert("העדכון הצליח!", "תוכל לעדכן קריאות נוספות מהרשימה.");
					}
				
				this.updateStatusController.get().checkStatus();
			});
			
			this.updateStatusController.get().threadPool.submitTask(task);
			
		}
		
	}
	
	
	private static class OnCloseEventHandler implements EventHandler<WindowEvent>{
		
		private Task<?> task;
		private WeakReference<UpdateMetersDataController> outerController;
		
		public OnCloseEventHandler(Task<?> task, UpdateMetersDataController controller) {
			this.task = task;
			this.outerController = new WeakReference<>(controller);
		}
		
		@Override
		public void handle(WindowEvent event) {
			this.task.cancel();
			((Stage)(event.getSource())).close();
			this.outerController.get().choosedMeters.clear();
			this.outerController.get().fetchData();
		}
	}
}
