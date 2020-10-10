package com.palgeymaim.client.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bacsoft.ArrayOfTimeValue;
import com.bacsoft.ControllerFieldsHistory;
import com.bacsoft.ResultOfPartialResultOfHistoryRegion;
import com.bacsoft.Status;
import com.bacsoft.TimeValue;
import com.palgeymaim.client.entity.IdModel;
import com.palgeymaim.client.entity.TimeValueWithColumnIndex;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class DataTableUtils {

	public static void resolveTableRowsFromResult(ResultOfPartialResultOfHistoryRegion result,
		
		Map<IdModel, Boolean> checkedFields, TableView<ObservableList<String>> dataTable) {

		if (result.getStatus().equals(Status.OK)) {

			Map<Integer, ArrayOfTimeValue> columnList = new HashMap<>();
			List<ControllerFieldsHistory> history = result.getItem().getData().getData().getControllerFieldsHistory();

			if (history.size() > 0) {
				history.get(0).getFieldsHistory().getFieldHistory().forEach(fieldHistory -> {
					columnList.put(fieldHistory.getFieldID(), fieldHistory.getHistory());
				});

				int numOfRows = Collections.max(
						columnList.values().stream().map(e -> e.getTimeValue().size()).collect(Collectors.toList()));

				List<List<TimeValueWithColumnIndex>> rowsList = new ArrayList<>();

				for (int i = 0; i < numOfRows; i++) {

					List<TimeValueWithColumnIndex> row = new ArrayList<>();

					Iterator<IdModel> it = checkedFields.keySet().iterator();

					int colIndex = 0;

					while (it.hasNext()) {
						List<TimeValue> current = Optional
								.ofNullable(columnList.get(Integer.valueOf(it.next().id.split(":")[1])))
								.map(e -> e.getTimeValue()).orElse(null);
						if (current != null && current.size() - 1 >= i) {
							row.add(new TimeValueWithColumnIndex(current.get(i), colIndex));
						}
						colIndex++;
					}

					rowsList.add(row);

				}

				List<List<String>> rows = new ArrayList<>();
				int numOfCols = checkedFields.size();

				DecimalFormat decimalFormat = new DecimalFormat("###,###.###");
				
				for (int i = 0; i < numOfRows; i++) {

					List<TimeValueWithColumnIndex> currentList = rowsList.get(i);

					for (int j = 0; j < numOfCols; j++) {

						if (currentList.size() - 1 >= j) {
							List<String> row = new ArrayList<>();
							TimeValueWithColumnIndex columnValue = currentList.get(j);
							int colIndex = columnValue.getColIndex();
							Object fieldValue = columnValue.getValue().getValue();
							
							for (int rowInd = 0; rowInd < numOfCols; rowInd++) {
								if (colIndex == rowInd) {
									row.add(rowInd, decimalFormat.format(fieldValue));
								} else {
									row.add(rowInd, "");
								}
							}
							row.add(new SimpleDateFormat("dd/MM/yy HH:mm.s")
									.format(columnValue.getValue().getTime().toGregorianCalendar().getTime()));
							rows.add(row);

						}
					}

				}

				Collections.sort(rows, (a, b) -> {
					List<String> aList = (List<String>) a;
					List<String> bList = (List<String>) b;
					try {
						return new SimpleDateFormat("dd/MM/yy HH:mm.s").parse(aList.get(aList.size() -1))
								.compareTo(new SimpleDateFormat("dd/MM/yy HH:mm.s").parse(bList.get(bList.size() - 1)));
					} catch (ParseException e1) {
						return 0;
					}
				});
				
				rows.forEach(row -> dataTable.getItems().add(FXCollections.observableArrayList(row)));
			}

		}
	}

}
