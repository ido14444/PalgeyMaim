package com.palgeymaim.client.controller;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bacsoft.ArrayOfFieldHistory;
import com.bacsoft.ArrayOfTimeValue;
import com.bacsoft.ResultOfPartialResultOfHistoryRegion;
import com.bacsoft.Status;
import com.bacsoft.TimeValue;
import com.palgeymaim.client.concurrency.TaskThreadPool;
import com.palgeymaim.client.config.ChartsConfig;
import com.palgeymaim.client.entity.IdModel;
import com.palgeymaim.client.exception.SessionExpiredException;
import com.palgeymaim.client.gui.WorkIndicatorDialog;
import com.palgeymaim.client.service.APIService;
import com.palgeymaim.client.utils.AlertUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ChartsController extends Controller implements Initializable {

	@FXML
	private LineChart<String, BigDecimal> throughputChart;

	@FXML
	private AnchorPane pane;
	
	private WorkIndicatorDialog wDialog;

	@FXML
	private BarChart<String, BigDecimal> dailyOverallChart;

	@FXML
	private BarChart<String, BigDecimal> currentDayOverallChart;

	@FXML
	private ComboBox<IdModel> unitComboBox;
	
	private Integer counter = 0;

	public void initTaskThreadPool(TaskThreadPool threadPool) {
		this.threadPool = threadPool;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		unitComboBox.setStyle("-fx-font: 14px \"Arial\";");
		this.throughputChart.setCreateSymbols(false);
	}

	@Override
	public void fetchData() {

		Map<String, ChartsConfig> chartsConfig = this.dataCache.getChartsConfiguration();
		for (IdModel unit : this.dataCache.getUnits()) {
			if (chartsConfig.get(unit.id) != null) {
				unitComboBox.getItems().add(unit);
			}

		}
		
		unitComboBox.getSelectionModel().selectedItemProperty().addListener(new Listener(this));

		unitComboBox.getSelectionModel().selectFirst();
	}
	
	private static class Listener implements ChangeListener<IdModel>{
		
		private WeakReference<ChartsController> controller;
		
		public Listener(ChartsController controller) {
			this.controller = new WeakReference<>(controller);
		}

		@Override
		public void changed(ObservableValue<? extends IdModel> observable, IdModel oldValue, IdModel newValue) {
			
			if (newValue != null) {
				this.controller.get().fetchChartsData(newValue.id);
			}
			
		} 
		
	}

	private FetchFieldHistoryTask createAndSubmitTask(LocalDateTime from, LocalDateTime to, String fieldId,
			String controllerId, XYChart<String, BigDecimal> chart, SimpleDateFormat formatter, String title) {

		FetchFieldHistoryTask task = new FetchFieldHistoryTask(fieldId, controllerId, from, to);

		this.threadPool.submitTask(task);
		
		task.setOnSucceeded(value -> {
			try {
				ResultOfPartialResultOfHistoryRegion data = task.get();
				if(isValidData(data)) {
					drawChart(chart, data, formatter, title);
				}
				synchronized (counter) {
					counter++;
					if(counter == 3) {
						this.wDialog.close();
						counter = 0;
					}
				}
				
			} catch (Throwable e) {
				this.wDialog.close();
				AlertUtils.createErrorAlert("שגיאה לא צפויה", e.getMessage());
			}

		});
		
		task.setOnFailed(value -> {
			
			this.wDialog.close();
			
			if(task.getException() instanceof SessionExpiredException && counter++ == 0) {
				AlertUtils.createSessionNotValidAlert().ifPresent(e -> {
					backToLogin((Stage)pane.getScene().getWindow());
				});
			} else if(counter++ == 0) {
				AlertUtils.createErrorAlert("שגיאה לא צפויה", task.getException().getMessage());
			}
			
		});

		return task;

	}

	private boolean isValidData(ResultOfPartialResultOfHistoryRegion data) {
		
		return data.getStatus().equals(Status.OK) && 
				data.getItem().getData().getData().getControllerFieldsHistory().size() > 0;
	}

	private void fetchChartsData(String controllerId) {
		
		throughputChart.getData().clear();
		dailyOverallChart.getData().clear();
		currentDayOverallChart.getData().clear();
		
		wDialog = new WorkIndicatorDialog("טוען נתונים...");

		wDialog.show();
	
		ChartsConfig config = this.dataCache.getChartsConfiguration().get(controllerId);

		createAndSubmitTask(LocalDateTime.now().minus(1, ChronoUnit.DAYS), LocalDateTime.now(),
					config.getThroughputFieldId(), controllerId, throughputChart, new SimpleDateFormat("HH:mm"),
					"ספיקה");

		createAndSubmitTask(LocalDateTime.now().minus(5, ChronoUnit.DAYS), LocalDateTime.now(),
					config.getLastDayOverallFieldId(), controllerId, dailyOverallChart, new SimpleDateFormat("dd/MM"),
					"נפח יום קודם");

		createAndSubmitTask(LocalDateTime.now().minus(1, ChronoUnit.DAYS), LocalDateTime.now(),
					config.getCurrentDayOverallFieldId(), controllerId, currentDayOverallChart,
					new SimpleDateFormat("HH:mm"), "נפח מצטבר שעתי");

	}


	private void drawChart(XYChart<String, BigDecimal> chart, ResultOfPartialResultOfHistoryRegion res,
			SimpleDateFormat formatter, String name) {
		ArrayOfFieldHistory hist = res.getItem().getData().getData().getControllerFieldsHistory().get(0)
				.getFieldsHistory();
		ArrayOfTimeValue arr = hist.getFieldHistory().get(0).getHistory();
		XYChart.Series<String, BigDecimal> series = new XYChart.Series<>();

		series.setName(name);

		for (TimeValue val : arr.getTimeValue()) {
			series.getData().add(new XYChart.Data<>(formatter.format(val.getTime().toGregorianCalendar().getTime()),
					(BigDecimal) val.getValue()));
		}
		chart.getData().add(series);
	}

	private static class FetchFieldHistoryTask extends Task<ResultOfPartialResultOfHistoryRegion> {

		private String fieldId;
		private String controllerId;
		private LocalDateTime from;
		private LocalDateTime to;

		public FetchFieldHistoryTask(String fieldId, String controllerId, LocalDateTime from, LocalDateTime to) {
			this.fieldId = fieldId;
			this.from = from;
			this.to = to;
			this.controllerId = controllerId;
		}

		@Override
		protected ResultOfPartialResultOfHistoryRegion call() throws DatatypeConfigurationException, ParseException {

			ZoneId zoneId = ZoneId.of("Asia/Jerusalem");

			XMLGregorianCalendar fromXML = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(GregorianCalendar.from(from.atZone(zoneId)));
			XMLGregorianCalendar toXML = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(GregorianCalendar.from(to.atZone(zoneId)));

			Set<IdModel> ids = new HashSet<>();
			ids.add(new IdModel(this.fieldId, ""));

			return APIService.getInstance().getPartialHistoryOfField(ids, this.controllerId, fromXML, toXML, 0).orElseThrow(() -> new SessionExpiredException());
		}

	}

}
