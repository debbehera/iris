/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.client.toast;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for controllers
 *
 * @author Douglas Lau
 */
public class ControllerModel extends ProxyTableModel<Controller> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 6;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Drop address column number */
	static protected final int COL_DROP = 1;

	/** Active column number */
	static protected final int COL_ACTIVE = 2;

	/** Communication status */
	static protected final int COL_STATUS = 3;

	/** Firmware version */
	static protected final int COL_VERSION = 4;

	/** Error detail */
	static protected final int COL_ERROR = 5;

	/** Create a new controller table model */
	public ControllerModel(TypeCache<Controller> c) {
		super(c, true);
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_ACTIVE)
			return Boolean.class;
		else
			return String.class;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Controller c = getProxy(row);
		if(c == null)
			return null;
		switch(column) {
			case COL_NAME:
				return c.getName();
			case COL_DROP:
				return c.getDrop();
			case COL_ACTIVE:
				return c.getActive();
			case COL_STATUS:
				return c.getStatus();
			case COL_ERROR:
				return c.getError();
			case COL_VERSION:
				return c.getVersion();
			default:
				return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		else
			return column == COL_DROP || column == COL_ACTIVE;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Controller c = getProxy(row);
		switch(column) {
			case COL_NAME:
//				String v = value.toString().trim();
//				if(v.length() > 0)
//					cache.createObject(v);
				break;
			case COL_DROP:
//				c.setDrop(v);
				break;
			case COL_ACTIVE:
				c.setActive((Boolean)value);
				break;
		}
	}

	/** Create the status column */
	protected TableColumn createStatusColumn() {
		TableColumn c = new TableColumn(COL_STATUS, 44);
		c.setHeaderValue("Status");
		c.setCellRenderer(new StatusCellRenderer());
		return c;
	}

	/** Renderer for link status in a table cell */
	public class StatusCellRenderer extends DefaultTableCellRenderer {
		protected final Icon ok = new ControllerIcon(Color.BLUE);
		protected final Icon fail = new ControllerIcon(Color.GRAY);
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label =
				(JLabel)super.getTableCellRendererComponent(
				table, "", isSelected, hasFocus, row,
				column);
			if(value == null)
				label.setIcon(null);
			else if("".equals(value))
				label.setIcon(ok);
			else
				label.setIcon(fail);
			return label;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 90, "Controller"));
		m.addColumn(createColumn(COL_DROP, 60, "Drop"));
		m.addColumn(createColumn(COL_ACTIVE, 50, "Active"));
		m.addColumn(createStatusColumn());
		m.addColumn(createColumn(COL_VERSION, 120, "Version"));
		m.addColumn(createColumn(COL_ERROR, 240, "Error Detail"));
		return m;
	}
}
