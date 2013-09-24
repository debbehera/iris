/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Tab for managing incidents.
 *
 * @author Douglas Lau
 */
public class IncidentTab extends MapTab<Incident> {

	/** Incident creator */
	private final IncidentCreator creator;

	/** Incident dispatcher */
	private final IncidentDispatcher dispatcher;

	/** Summary of incidents of each status */
	private final StyleSummary<Incident> summary;

	/** Create a new incident tab */
  	public IncidentTab(Session session, IncidentManager m) {
		super("incident", m);
		creator = new IncidentCreator(session, m.getTheme(),
			m.getSelectionModel());
		dispatcher = new IncidentDispatcher(session, m, creator);
		summary = m.createStyleSummary();
		add(createNorthPanel(), BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Create the north panel */
	private JPanel createNorthPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(creator, BorderLayout.NORTH);
		panel.add(dispatcher, BorderLayout.CENTER);
		return panel;
	}

	/** Dispose of the incident tab */
	@Override public void dispose() {
		super.dispose();
		manager.getSelectionModel().clearSelection();
		summary.dispose();
		dispatcher.dispose();
		creator.dispose();
	}

	/** Set the map for this tab */
	@Override public void setMap(MapBean m) {
		super.setMap(m);
		creator.setEnabled(canAdd() && m != null);
	}

	/** Check if the user can add an incident */
	private boolean canAdd() {
		return dispatcher.canAdd("oname");
	}
}
