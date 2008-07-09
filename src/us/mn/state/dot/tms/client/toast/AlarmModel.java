/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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

import java.rmi.RemoteException;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.TMSException;

/**
 * Table model for controller alarms
 *
 * @author Douglas Lau
 */
public class AlarmModel extends AbstractTableModel {

	/** Pin for first alarm input */
	static protected final int ALARM_PIN = 70;

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Controller input pin column number */
	static protected final int COL_PIN = 0;

	/** Alarm text column number */
	static protected final int COL_NOTES = 1;

	/** Create a new table column */
	static protected TableColumn createColumn(int col, String name,
		int width)
	{
		TableColumn c = new TableColumn(col, width);
		c.setHeaderValue(name);
		return c;
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_PIN, "Pin", 30));
		m.addColumn(createColumn(COL_NOTES, "Notes", 250));
		return m;
	}

	/** Alarms list */
	protected final Alarm[] alarms = new Alarm[10];

	/** Controller whose alarms this table represent */
	protected final Controller controller;

	/** Administrator flag */
	protected final boolean admin;

	/** List of all rows (one for each alarm input) */
	protected final LinkedList<Object []> rows =
		new LinkedList<Object []>();

	/** Create a new alarm table model */
	public AlarmModel(Controller c, boolean a) throws RemoteException {
		controller = c;
		ControllerIO[] io_pins = c.getIO();
		for(int i = 0; i < 10; i++) {
			int pin = ALARM_PIN + i;
			Alarm alarm = (Alarm)io_pins[pin];
			alarms[i] = alarm;
			String notes = null;
			if(alarm != null)
				notes = alarm.getNotes();
			rows.add(fillRow(createRow(), pin, notes));
		}
		admin = a;
	}

	/** Create a new table row */
	protected Object[] createRow() {
		return new Object[COLUMN_COUNT];
	}

	/** Fill a row with an alarm */
	protected Object[] fillRow(Object[] row, int i, String notes) {
		row[COL_PIN] = new Integer(i);
		row[COL_NOTES] = notes;
		return row;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return rows.size();
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_PIN)
			return Integer.class;
		else
			return String.class;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Object[] r = rows.get(row);
		return r[column];
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
