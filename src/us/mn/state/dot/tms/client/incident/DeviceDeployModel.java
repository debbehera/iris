/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2019  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.IncAdviceHelper;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.IncDescriptorHelper;
import us.mn.state.dot.tms.IncLocator;
import us.mn.state.dot.tms.IncLocatorHelper;
import us.mn.state.dot.tms.IncRange;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * DeviceDeployModel is a model of devices to deploy for an incident.
 *
 * @author Douglas Lau
 */
public class DeviceDeployModel extends DefaultListModel<Device> {

	/** Get the r_node type checker */
	static private R_NodeType.Checker getChecker(short lto) {
		final LaneType lt = LaneType.fromOrdinal(lto);
		return new R_NodeType.Checker() {
			public boolean check(R_NodeType nt) {
				switch (lt) {
				case EXIT:
					return R_NodeType.EXIT == nt;
				case MERGE:
					return R_NodeType.ENTRANCE == nt;
				case MAINLINE:
					return R_NodeType.STATION == nt
					    || R_NodeType.INTERSECTION == nt;
				default:
					return false;
				}
			}
		};
	}

	/** Upstream device finder */
	private final UpstreamDeviceFinder finder;

	/** LCS indication builder */
	private final LcsIndicationBuilder ind_builder;

	/** Mapping of LCS array names to proposed indications */
	private final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** Get the proposed indications for an LCS array */
	public Integer[] getIndications(String lcs_a) {
		return indications.get(lcs_a);
	}

	/** Mapping of DMS names to proposed MULTI strings */
	private final HashMap<String, MultiString> messages =
		new HashMap<String, MultiString>();

	/** Get the proposed MULTI for a DMS */
	public MultiString getMulti(String dms) {
		return messages.get(dms);
	}

	/** Mapping of DMS names to proposed page one graphics */
	private final HashMap<String, RasterGraphic> graphics =
		new HashMap<String, RasterGraphic>();

	/** Get the proposed graphics for a DMS */
	public RasterGraphic getGraphic(String dms) {
		return graphics.get(dms);
	}

	/** Create a new device deploy model */
	public DeviceDeployModel(IncidentManager man, Incident inc) {
		finder = new UpstreamDeviceFinder(man, inc);
		ind_builder = new LcsIndicationBuilder(man, inc);
		IncidentLoc iloc = new IncidentLoc(inc);
		String name = GeoLocHelper.getCorridorName(iloc);
		CorridorBase cb = man.lookupCorridor(name);
		if (cb != null) {
			Float mp = cb.calculateMilePoint(iloc);
			if (mp != null) {
				R_Node n = pickNode(inc, cb, mp);
				GeoLoc loc = (n != null) ? n.getGeoLoc() : iloc;
				populateList(man, inc, loc, n != null);
			}
		}
	}

	/** Pick a node within 1 mile of incident */
	private R_Node pickNode(Incident inc, CorridorBase cb, float mp) {
		Position pos = new Position(inc.getLat(), inc.getLon());
		R_NodeType.Checker checker = getChecker(inc.getLaneType());
		R_Node n = cb.findNearest(pos, checker, true);
		if (n != null) {
			Float lp = cb.calculateMilePoint(n.getGeoLoc());
			if (lp != null && Math.abs(lp - mp) < 1)
				return n;
		}
		return null;
	}

	/** Populate list model with device deployments.
	 * @param man Incident manager.
	 * @param inc Incident.
	 * @param loc Location of incident.
	 * @param picked True if r_node was picked. */
	private void populateList(IncidentManager man, Incident inc, GeoLoc loc,
		boolean picked)
	{
		finder.findDevices();
		Iterator<UpstreamDevice> it = finder.iterator();
		while (it.hasNext()) {
			UpstreamDevice ud = it.next();
			Device dev = ud.device;
			if (dev instanceof LCSArray)
				addUpstreamLCS((LCSArray) dev, ud);
			if (dev instanceof DMS)
				addUpstreamDMS((DMS) dev, ud, inc, loc, picked);
		}
	}

	/** Add an upstream LCS array */
	private void addUpstreamLCS(LCSArray lcs_array, UpstreamDevice ud) {
		Integer[] ind = ind_builder.createIndications(lcs_array,
			ud.distance);
		if (ind != null) {
			addElement(lcs_array);
			indications.put(lcs_array.getName(), ind);
		}
	}

	/** Add an upstream DMS */
	private void addUpstreamDMS(DMS dms, UpstreamDevice ud, Incident inc,
		GeoLoc loc, boolean picked)
	{
		MultiString ms = createMulti(inc, dms, ud, loc, picked);
		if (ms != null) {
			RasterGraphic rg = createGraphic(dms, ms);
			if (rg != null) {
				addElement(dms);
				messages.put(dms.getName(), ms);
				graphics.put(dms.getName(), rg);
			}
		}
	}

	/** Create the MULTI string for one DMS.
	 * @param inc Incident.
	 * @param dms Possible sign to deploy.
	 * @param ud Upstream device.
	 * @param loc Location of incident.
	 * @param picked True if r_node was picked.
	 * @return MULTI string for DMS, or null. */
	private MultiString createMulti(Incident inc, DMS dms, UpstreamDevice ud,
		GeoLoc loc, boolean picked)
	{
		Distance up = ud.distance;
		IncRange rng = ud.range();
		if (null == rng)
			return null;
		IncDescriptor dsc = IncDescriptorHelper.match(inc);
		if (null == dsc)
			return null;
		IncLocator iloc = IncLocatorHelper.match(rng, false, picked);
		if (null == iloc)
			return null;
		IncAdvice adv = IncAdviceHelper.match(rng, inc);
		if (null == adv)
			return null;
		String mdsc = checkMulti(dms, dsc.getMulti(), dsc.getAbbrev(),
			up, loc);
		if (null == mdsc)
			return null;
		String mloc = checkMulti(dms, iloc.getMulti(), iloc.getAbbrev(),
			up, loc);
		if (null == mloc)
			return null;
		String madv = checkMulti(dms, adv.getMulti(), adv.getAbbrev(),
			up, loc);
		if (null == madv)
			return null;
		LocMultiBuilder lmb = new LocMultiBuilder(loc, up);
		new MultiString(mdsc).parse(lmb);
		lmb.addLine(null);
		new MultiString(mloc).parse(lmb);
		lmb.addLine(null);
		new MultiString(madv).parse(lmb);
		return lmb.toMultiString();
	}

	/** Check if MULTI string or abbreviation will fit on a DMS */
	private String checkMulti(DMS dms, String ms, String abbrev,
		Distance up, GeoLoc loc)
	{
		String res = checkMulti(dms, ms, up, loc);
		return (res != null) ? res : checkMulti(dms, abbrev, up, loc);
	}

	/** Check if MULTI string will fit on a DMS */
	private String checkMulti(DMS dms, String ms, Distance up, GeoLoc loc) {
		if (null == ms)
			return null;
		LocMultiBuilder lmb = new LocMultiBuilder(loc, up);
		new MultiString(ms).parse(lmb);
		MultiString multi = lmb.toMultiString();
		if (createGraphic(dms, multi) != null)
			return ms;
		else
			return null;
	}

	/** Create the page one graphic for a MULTI string */
	private RasterGraphic createGraphic(DMS dms, MultiString ms) {
		try {
			RasterGraphic[] pixmaps = DMSHelper.createPixmaps(dms,
				ms);
			return pixmaps[0];
		}
		catch (Exception e) {
			// could be IndexOutOfBounds or InvalidMessage
			return null;
		}
	}
}
