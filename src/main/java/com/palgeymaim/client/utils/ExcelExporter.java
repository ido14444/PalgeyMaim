package com.palgeymaim.client.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.palgeymaim.client.entity.ExportableData;

public class ExcelExporter {
	
	public static void exportToExcel(ExportableData data , String filePath)  {
		  
		try(FileOutputStream outputStream = new FileOutputStream(filePath)) {
			  XSSFWorkbook workbook = new XSSFWorkbook();
	          XSSFSheet sheet = workbook.createSheet("נתונים");

	          writeHeaderLine(data, sheet);

	          writeRows(data, sheet);
	          
	          workbook.write(outputStream);
	          workbook.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}



	private static void writeHeaderLine(ExportableData data , XSSFSheet sheet) {
		
		Row headerRow = sheet.createRow(0);
		Iterator<String> columnsIt = data.headers().iterator();
		int columnCount = 0;
		while(columnsIt.hasNext()) {
			 Cell headerCell = headerRow.createCell(columnCount++);
		     headerCell.setCellValue(columnsIt.next());
		}
	}
	
	
	private static void writeRows(ExportableData data , XSSFSheet sheet) {
		
		Iterator<List<String>> rowsIt = data.data().iterator();
		int rowCount = 1;
		while(rowsIt.hasNext()) {
			
			Row row = sheet.createRow(rowCount++);
			
			int columnCount = 0;
			Iterator<String> columnsIt = rowsIt.next().iterator();
			while(columnsIt.hasNext()) {
				Cell cell = row.createCell(columnCount++);
				cell.setCellValue(columnsIt.next());
			}
		}
		
	}
	

}
