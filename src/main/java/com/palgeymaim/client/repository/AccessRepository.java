package com.palgeymaim.client.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.palgeymaim.client.config.PropertiesLoader;
import com.palgeymaim.client.entity.MeterDataFromDB;

public class AccessRepository {

	public Map<String, MeterDataFromDB> getAllPreviousReads(Set<String> symbnums) throws IOException {

		Map<String, MeterDataFromDB> result = new HashMap<>();

		Database db = DatabaseBuilder.open(new File(PropertiesLoader.getProperties().getProperty("access.file.path")));

		Table madReadTable = db.getTable("MAD_READ");

		Table madNatunTable = db.getTable("MAD_NATUN");
		Index index;

		try {
			index = madReadTable.getIndex("DATE_INDEX");
		} catch (IllegalArgumentException exception) {
			index = new IndexBuilder("DATE_INDEX").addColumns(false, "DATE").addToTable(madReadTable);
		}

		for (String symbnum : symbnums) {

			Row madReadRow = CursorBuilder.findRow(index, Collections.singletonMap("SYMBNUM", symbnum));
			Row placeRow = CursorBuilder.findRow(madNatunTable, Collections.singletonMap("SYMBNUM", symbnum));

			if (madReadRow != null) {
				MeterDataFromDB meterDataFromDB = new MeterDataFromDB(BigDecimal.valueOf(madReadRow.getDouble("READ")),
						placeRow != null ? placeRow.getString("PLACE") : "", madReadRow.getLocalDateTime("DATE"));
				result.put(madReadRow.getString("SYMBNUM"), meterDataFromDB);
			}

		}

		db.close();
		return result;

	}

	public void insertNewMeterRead(String symbnum, BigDecimal balance, LocalDateTime lastBacsoftDate) throws FileNotFoundException, IOException {
		
		Database db = DatabaseBuilder.open(new File(PropertiesLoader.getProperties().getProperty("access.file.path")));
		
		Table madReadTable = db.getTable("MAD_READ");
		
		Map<String,Object> newRow = new HashMap<>();
		
		Index index =  madReadTable.getIndex("DATE_INDEX");
		
		Row lastReadRow = CursorBuilder.findRow(index, Collections.singletonMap("SYMBNUM", symbnum));
		
		if(lastBacsoftDate.compareTo(lastReadRow.getLocalDateTime("DATE")) != 0) {
			newRow.put("SYMBNUM", symbnum);
			newRow.put("DATE", lastBacsoftDate);
			newRow.put("Year", lastBacsoftDate.getYear());
			newRow.put("Month", lastBacsoftDate.getMonthValue() < 10 ? "0" + lastBacsoftDate.getMonthValue() : lastBacsoftDate.getMonthValue());
			newRow.put("YearWork", lastBacsoftDate.getYear());
			newRow.put("ReadPrev", lastReadRow.getDouble("READ"));
			newRow.put("DatePrev", lastReadRow.getLocalDateTime("DATE"));
			newRow.put("Balance", balance);
			newRow.put("MAD_INDEX", lastReadRow.getInt("MAD_INDEX") + 1);
			newRow.put("READ", balance.add(BigDecimal.valueOf(lastReadRow.getDouble("READ"))));
			madReadTable.addRowFromMap(newRow);
		}
		
		db.close();
		
	}

}
