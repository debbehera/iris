/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.weather;

import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm2;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing weather sensors
 *
 * @author Douglas Lau
 */
public class WeatherSensorForm extends ProxyTableForm2<WeatherSensor> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(WeatherSensor.SONAR_TYPE);
	}

	/** Create a new weather sensor form */
	public WeatherSensorForm(Session s) {
		super(I18N.get("weather.sensors"), new WeatherSensorModel(s));
	}
}
