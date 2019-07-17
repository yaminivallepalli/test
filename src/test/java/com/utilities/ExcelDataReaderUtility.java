package com.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelDataReaderUtility {
	XSSFWorkbook wb = null;
	static XSSFSheet sheet1 = null;
	// String file_path;
	public ExcelDataReaderUtility(String filePath, int sheetIndex) {
		File src = new File(filePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(src);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			wb = new XSSFWorkbook(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sheet1 = wb.getSheetAt(sheetIndex);
	}
	// Getting the rows count
	public int getRows() {
		return sheet1.getLastRowNum();
	}
	public String getStringCelldata(int rowIndex, int columnIndex) {
		// String val =
		// sheet1.getRow(rowIndex).getCell(columnIndex).getStringCellValue();
		if (sheet1.getRow(rowIndex).getCell(columnIndex) != null) {
			return sheet1.getRow(rowIndex).getCell(columnIndex).getStringCellValue();
		} else {
			return "";
		}
	}
	public double getNumericCelldata(int rowIndex, int columnIndex) {
		if (sheet1.getRow(rowIndex).getCell(columnIndex) != null) {
			return sheet1.getRow(rowIndex).getCell(columnIndex).getNumericCellValue();
		} else {
			return Integer.parseInt("");
		}
	}
	public boolean getBooleanCelldata(int rowIndex, int columnIndex) {
		return sheet1.getRow(rowIndex).getCell(columnIndex).getBooleanCellValue();
	}
}
