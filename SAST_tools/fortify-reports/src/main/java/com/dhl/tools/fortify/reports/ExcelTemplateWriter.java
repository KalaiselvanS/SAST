package com.dhl.tools.fortify.reports;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelTemplateWriter {

	public static void writeToExcel(String fileFpr, List<List<String>> data, String templatePath, boolean skipHeader)
			throws IOException, FileNotFoundException {

		try (InputStream fis = ExcelTemplateWriter.class.getClassLoader().getResourceAsStream(templatePath);
				XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
			Sheet sheet = workbook.getSheetAt(0);
//			Cell agreedActionCell = sheet.getRow(1).getCell(9);
//			String agreedActionCellValue = agreedActionCell.toString();
			int startInd = 0;
			if (skipHeader) {
				startInd = 1;
			}
			for (int i = startInd; i < data.size(); i++) {
				Row row = sheet.createRow(i);
				final List<String> rowData = data.get(i);
				for (int j = 0; j < rowData.size(); j++) {
					Cell cell = row.createCell(j);
					cell.setCellValue(rowData.get(j));
				}
//				final Cell cell = row.createCell(9);
//				cell.setCellValue(agreedActionCellValue);
			}
			try (FileOutputStream outputStream = new FileOutputStream(fileFpr + "_SAST_Report_" + System.currentTimeMillis() + ".xlsx")) {
				workbook.write(outputStream);
			}
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
