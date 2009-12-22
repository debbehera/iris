/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;

/**
 * A form for displaying a table of lane-use MULTI strings.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiForm extends ProxyTableForm<LaneUseMulti> {

	/** Create a new graphic form */
	public LaneUseMultiForm(Session s) {
		super("Lane-Use MULTI", new LaneUseMultiModel(s));
	}

	/** Get the row height */
	protected int getRowHeight() {
		return 22;
	}

	/** Get the visible row count */
	protected int getVisibleRowCount() {
		return 10;
	}
}
