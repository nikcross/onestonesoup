package org.onestonesoup.core.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CSVHelper {

	public static EntityTable loadCSV(File file) throws FileNotFoundException, IOException {
		return loadCSV(new FileInputStream(file));
	}
	public static EntityTable loadTSV(File file) throws FileNotFoundException, IOException {
		return loadTSV(new FileInputStream(file));
		
	}
	public static EntityTable loadTSV(InputStream in) throws IOException {
		return load(in,"\t");
	}
	public static EntityTable loadCSV(InputStream in) throws IOException {
		return load(in,",");
	}

	public static EntityTable load(InputStream in,String separator) throws IOException {
		BufferedReader reader =  new BufferedReader(new InputStreamReader(in));
		String columnsLine = reader.readLine();
		if(columnsLine==null) {
			throw new IOException("No column definitions found");
		}
		EntityTable table = new EntityTable(columnsLine.split(separator));
		
		String row = reader.readLine();
		while(row!=null) {
			table.addRow(row.split(separator));
			row = reader.readLine();
		}
		
		return table;
	}
	public static String toTSV(EntityTable table) {
		StringBuffer data = new StringBuffer();
		int columnCount = table.getColumnDefinitions().size();
		
		for(int columnIndex=0;columnIndex<columnCount;columnIndex++) {
			if(columnIndex>0) {
				data.append("\t");
			}
			data.append( table.getColumnDefinitions().getColumn(columnIndex) );
		}
		data.append("\n");
		for(int rowIndex=0;rowIndex<table.size();rowIndex++) {
			for(int columnIndex=0;columnIndex<columnCount;columnIndex++) {
				if(columnIndex>0) {
					data.append("\t");
				}
				data.append( table.getRow(rowIndex).getColumn(columnIndex) );
			}
			data.append("\n");
		}
		
		return data.toString();
	}
	public static String toCSV(EntityTable table) {
		StringBuffer data = new StringBuffer();
		int columnCount = table.getColumnDefinitions().size();
		
		for(int columnIndex=0;columnIndex<columnCount;columnIndex++) {
			if(columnIndex>0) {
				data.append(",");
			}
			data.append( table.getColumnDefinitions().getColumn(columnIndex) );
		}
		data.append("\n");
		for(int rowIndex=0;rowIndex<table.size();rowIndex++) {
			for(int columnIndex=0;columnIndex<columnCount;columnIndex++) {
				if(columnIndex>0) {
					data.append(",");
				}
				data.append( table.getRow(rowIndex).getColumn(columnIndex) );
			}
			data.append("\n");
		}
		
		return data.toString();
	}
	
}
