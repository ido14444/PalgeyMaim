package com.palgeymaim.client.controller;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bacsoft.ResultOfPartialResultOfHistoryRegion;
import com.bacsoft.Status;
import com.palgeymaim.client.entity.ExportableData;
import com.palgeymaim.client.entity.HourChoice;
import com.palgeymaim.client.entity.IdModel;
import com.palgeymaim.client.exception.SessionExpiredException;
import com.palgeymaim.client.gui.WorkIndicatorDialog;
import com.palgeymaim.client.service.APIService;
import com.palgeymaim.client.utils.AlertUtils;
import com.palgeymaim.client.utils.DataTableUtils;
import com.palgeymaim.client.utils.ExcelExporter;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tornadofx.control.DateTimePicker;

public class DataViewController extends Controller implements Initializable {

	@FXML
	private ListView<IdModel> listView;

	@FXML
	private Button displayDataBtn;

	private WorkIndicatorDialog wDialog;

	@FXML
	private Button exportToExcelBtn;

	@FXML
	private TableView<ObservableList<String>> dataTable;

	@FXML
	private HBox fromHbox;

	@FXML
	private HBox toHbox;

	@FXML
	private AnchorPane pane;

	private Map<String, List<IdModel>> controllersFieldsCache;

	private Map<IdModel, Boolean> checkedFields;

	@FXML
	private ComboBox<IdModel> unitComboBox;

	private DateTimePicker fromDateTimePicker;

	private DateTimePicker toDateTimePicker;

	@FXML
	private ComboBox<HourChoice> fromTime;

	@FXML
	private ComboBox<HourChoice> toTime;

	protected int currentPosition = 0;

	public DataViewController() {
		checkedFields = new HashMap<>();
		fromDateTimePicker = new DateTimePicker();
		fromDateTimePicker.setFormat("dd/MM/yy");
		toDateTimePicker = new DateTimePicker();
		toDateTimePicker.setFormat("dd/MM/yy");
	}

	@FXML
	private void OnDisplayDataBtnClicked(ActionEvent event) throws DatatypeConfigurationException {
		
		Optional<Task<ResultOfPartialResultOfHistoryRegion>> task = fetchDataFromAPI();
		
		if(task.isPresent()) {
			wDialog = new WorkIndicatorDialog("טוען נתונים...");
			wDialog.show();
			exportToExcelBtn.setDisable(true);
			currentPosition = 0;
			dataTable.getColumns().clear();
			dataTable.getItems().clear();

			addTableColumns();

			task.get().setOnSucceeded(value -> {

				processTaskValue(task.get());
			});

			task.get().setOnFailed(value -> {
				wDialog.close();
				if (task.get().getException() instanceof SessionExpiredException) {
					AlertUtils.createSessionNotValidAlert().ifPresent(e -> {
						backToLogin((Stage) pane.getScene().getWindow());
					});
				} else {
					AlertUtils.createErrorAlert("שגיאה לא צפויה", task.get().getException().getMessage());
				}

			});
		}
		
			
	}

	@FXML
	private void OnExport(ActionEvent event) {

		String exportFileName = "Export_" + System.currentTimeMillis() + ".xlsx";

		Task<Boolean> excelExportTask = new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				ExcelExporter.exportToExcel(new ExportableData() {

					@Override
					public List<String> headers() {

						List<String> headers = new ArrayList<>();
						headers.add("תאריך");
						checkedFields.keySet().stream().forEach(field -> headers.add(field.name));

						return headers;
					}

					@Override
					public List<List<String>> data() {
						List<List<String>> data = new ArrayList<>();
						data.addAll(dataTable.getItems());
						return data;
					}
				}, exportFileName);

				return true;
			}

		};

		Platform.runLater(() -> {
			wDialog = new WorkIndicatorDialog("מייצא לאקסל...");
			wDialog.show();
		});

		excelExportTask.setOnSucceeded(value -> {
			wDialog.close();
			exportToExcelBtn.setDisable(true);
			AlertUtils.createInfoAlert("הנתונים נשמרו בהצלחה לקובץ אקסל", "שם הקובץ : " + exportFileName);
		});

		excelExportTask.setOnFailed(value -> {
			AlertUtils.createErrorAlert("שגיאה לא צפויה", excelExportTask.getException().getMessage());
		});

		threadPool.submitTask(excelExportTask);

	}

	private void processTaskValue(Task<ResultOfPartialResultOfHistoryRegion> task) {
		ResultOfPartialResultOfHistoryRegion result = task.getValue();
		if (result.getStatus().equals(Status.OK) && !result.getItem().isGotAllData()) {
			DataTableUtils.resolveTableRowsFromResult(task.getValue(), checkedFields, dataTable);
			dataTable.refresh();
			currentPosition = result.getItem().getNextPosition();
			fetchDataFromAPI().ifPresent(nextTask -> {
				threadPool.submitTask(nextTask);
				nextTask.setOnSucceeded((event) -> processTaskValue(nextTask));
			});

		} else {
			currentPosition = 0;
			wDialog.close();
			exportToExcelBtn.setDisable(false);
			DataTableUtils.resolveTableRowsFromResult(task.getValue(), checkedFields, dataTable);
		}

	}

	private static class BacsoftHistoryFetcher extends Task<ResultOfPartialResultOfHistoryRegion> {

		private WeakReference<Map<IdModel, Boolean>> checkedFields;
		private int position;
		private XMLGregorianCalendar fromDate;
		private XMLGregorianCalendar toDate;

		public BacsoftHistoryFetcher(Map<IdModel, Boolean> checkedFields, int position, XMLGregorianCalendar fromDate,
				XMLGregorianCalendar toDate) {
			this.checkedFields = new WeakReference<>(checkedFields);
			this.position = position;
			this.fromDate = fromDate;
			this.toDate = toDate;

		}

		@Override
		protected ResultOfPartialResultOfHistoryRegion call() throws Exception {
			String controllerId = "Controller:" + checkedFields.get().keySet().iterator().next().id.split(":")[2];
			return APIService.getInstance()
					.getPartialHistoryOfField(checkedFields.get().keySet(), controllerId, fromDate, toDate, position)
					.orElseThrow(() -> new SessionExpiredException());
		}

	}

	private Optional<Task<ResultOfPartialResultOfHistoryRegion>> fetchDataFromAPI() {

		LocalDateTime from = fromDateTimePicker.getDateTimeValue();
		from = from.withHour(fromTime.getSelectionModel().getSelectedItem().getHour()).withMinute(0).withSecond(0)
				.withNano(0);
		LocalDateTime to = toDateTimePicker.getDateTimeValue();
		to = to.withHour(toTime.getSelectionModel().getSelectedItem().getHour()).withMinute(0).withSecond(0)
				.withNano(0);

		if (from.isAfter(to)) {
			AlertUtils.createErrorAlert("שגיאה", "תאריך הסיום לא יכול להיות לפני תאריך ההתחלה");
			return Optional.empty();
		}

		else {
			ZoneId zoneId = ZoneId.of("Asia/Jerusalem");
			XMLGregorianCalendar fromXML;
			XMLGregorianCalendar toXML;
			try {
				fromXML = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(GregorianCalendar.from(from.atZone(zoneId)));
				toXML = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(GregorianCalendar.from(to.atZone(zoneId)));
				Task<ResultOfPartialResultOfHistoryRegion> task = new BacsoftHistoryFetcher(checkedFields,
						currentPosition, fromXML, toXML);
				threadPool.submitTask(task);
				return Optional.of(task);

			} catch (Throwable e) {
				return Optional.empty();
			}
		}
	}

	private void addTableColumns() {

		TableColumn<ObservableList<String>, String> dateCol = new TableColumn<>("תאריך");

		List<String> columnNames = checkedFields.keySet().stream().map(e -> e.name).collect(Collectors.toList());

		for (int i = 0; i < columnNames.size(); i++) {
			final int finalIdx = i;
			TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames.get(i));
			column.setMaxWidth(300);
			column.setStyle("-fx-alignment: CENTER;");
			column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));

			dataTable.getColumns().add(column);
		}

		dateCol.setStyle("-fx-alignment: CENTER;");
		dateCol.setCellValueFactory(
				param -> new ReadOnlyObjectWrapper<>(param.getValue().get(param.getValue().size() - 1)));
		dateCol.setMaxWidth(300);
		dataTable.getColumns().add(dateCol);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		dataTable.setPlaceholder(new Label("אין נתונים להצגה"));
		dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		displayDataBtn.setDisable(true);
		exportToExcelBtn.setDisable(true);
		dataTable.setStyle("-fx-font: 14px \"Arial\";");
		fromHbox.getChildren().add(1, fromDateTimePicker);
		toHbox.getChildren().add(1, toDateTimePicker);
		fromDateTimePicker.setStyle("-fx-font: 14px \"Arial\";");
		toDateTimePicker.setStyle("-fx-font: 14px \"Arial\";");
		fromDateTimePicker.setDateTimeValue(LocalDateTime.now().minus(Period.ofDays(1)));
		toDateTimePicker.setDateTimeValue(LocalDateTime.now());
		dataTable.getColumns().clear();

		unitComboBox.setStyle("-fx-font: 14px \"Arial\";");
		listView.setStyle("-fx-font: 14px \"Arial\";");

		IntStream.range(0, 24).forEach(hour -> {
			fromTime.getItems().add(new HourChoice(hour));
			toTime.getItems().add(new HourChoice(hour));
		});

		fromTime.getSelectionModel().selectFirst();
		toTime.getSelectionModel().selectFirst();
		fromTime.setStyle("-fx-font: 14px \"Arial\";");
		toTime.setStyle("-fx-font: 14px \"Arial\";");


		listView.setCellFactory(CheckBoxListCell.forListView(new ListViewCallback(this)));

	}

	private static class ListViewCallback implements Callback<IdModel, ObservableValue<Boolean>> {

		private WeakReference<DataViewController> dataViewReference;
		
		public ListViewCallback(DataViewController dataViewController) {
			this.dataViewReference = new WeakReference<>(dataViewController);
		}

		@Override
		public ObservableValue<Boolean> call(IdModel item) {
			BooleanProperty observable = new SimpleBooleanProperty();
			observable.addListener((obs, wasSelected, isNowSelected) -> {
				if (isNowSelected) {
					dataViewReference.get().checkedFields.put(item, isNowSelected);
				} else {
					dataViewReference.get().checkedFields.remove(item);
				}

				if (dataViewReference.get().checkedFields.size() > 0) {
					dataViewReference.get().displayDataBtn.setDisable(false);
				} else {
					dataViewReference.get().displayDataBtn.setDisable(true);
				}

			});
			return observable;
		}
	}

	private static class Listener implements ChangeListener<IdModel> {

		private WeakReference<DataViewController> dataViewReference;

		public Listener(DataViewController dataViewController) {

			this.dataViewReference = new WeakReference<>(dataViewController);
		}

		@Override
		public void changed(ObservableValue<? extends IdModel> observable, IdModel oldValue, IdModel newValue) {
			dataViewReference.get().checkedFields.clear();
			dataViewReference.get().displayDataBtn.setDisable(true);
			if (newValue != null) {
				dataViewReference.get().dataTable.getItems().clear();
				dataViewReference.get().dataTable.getColumns().clear();
				dataViewReference.get().listView.getItems().clear();
				String controller = dataViewReference.get().unitComboBox.getSelectionModel().getSelectedItem().id;
				Map<String, Boolean> map = dataViewReference.get().dataCache.getAllFieldsOfController(controller);
				for (IdModel field : dataViewReference.get().controllersFieldsCache.get(newValue.id)) {
					if (map == null || map.getOrDefault(field.id, false))
						dataViewReference.get().listView.getItems().add(field);
				}

			}

		}

	}

	public void fetchData() {

		this.controllersFieldsCache = this.dataCache.getControllersCache();

		for (IdModel unit : this.dataCache.getUnits()) {
			unitComboBox.getItems().add(unit);
		}
		
		unitComboBox.getSelectionModel().selectedItemProperty().addListener(
				new Listener(this));

		unitComboBox.getSelectionModel().selectFirst();
	}

}
