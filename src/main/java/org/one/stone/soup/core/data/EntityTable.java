package org.one.stone.soup.core.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class EntityTable {
	
	public interface EntityComparator extends Comparator<Entity> {
		
	}
	
	private class DateComparator implements EntityComparator {
		private int columnIndex;
		private SimpleDateFormat format;
		public DateComparator(String dateFormat,String columnName) {
			this.format = new SimpleDateFormat(dateFormat);
			this.columnIndex = getColumnIndex(columnName);
		}
		public int compare(Entity row1, Entity row2) {
			try{
				Date date1 = format.parse(row1.getColumn(columnIndex));
				Date date2 = format.parse(row2.getColumn(columnIndex));
				return date1.compareTo(date2);
			} catch(ParseException pe) {
				return -1;
			}
		}
		
	}
	
	public class Entity {
		private String[] columns;
		
		private Entity(int columns) {
			this.columns = new String[columns];
		}
		public int size() {
			return columns.length;
		}
		public String getColumn(int columnIndex) {
			return columns[columnIndex];
		}
		public void setColumn(int columnIndex,String columnValue) {
			columns[columnIndex]=columnValue;
		}
	}
	
	private Entity columnDefinition;
	private List<Entity> rows;
	
	public EntityTable(String[] columnNames) {
		columnDefinition = new Entity(columnNames.length);
		for(int columnIndex=0;columnIndex<columnNames.length;columnIndex++) {
			columnDefinition.setColumn(columnIndex, columnNames[columnIndex]);
		}
		rows = new ArrayList<Entity>();
	}
	
	public int columns() {
		return columnDefinition.columns.length;
	}
	
	public int size() {
		return rows.size();
	}
	
	public Entity addRow(String[] data) {
		Entity row = new Entity(columnDefinition.columns.length);
		for(int columnIndex=0;columnIndex<data.length;columnIndex++) {
			row.setColumn(columnIndex, data[columnIndex]);
		}	
		rows.add(row);
		return row;
	}
	
	public Entity getColumnDefinitions() {
		return columnDefinition;
	}
	
	public int getColumnIndex(String columnName) {
		for(int columnIndex=0;columnIndex<columnDefinition.columns.length;columnIndex++) {
			if(columnDefinition.columns[columnIndex].equals(columnName)) {
				return columnIndex;
			}
		}
		return -1;
	}
	
	public Entity getRow(int rowIndex) {
		return rows.get(rowIndex);
	}
	
	public String getValue(int rowIndex,String columnName) {
		int columnIndex = getColumnIndex(columnName);
		return rows.get(rowIndex).getColumn(columnIndex);
	}
	
	public void sortByDate(String dateFormat,String columnName) {
		DateComparator comparator = new DateComparator(dateFormat,columnName);
		
		Collections.sort(rows,comparator);
	}
}
