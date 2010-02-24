/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.Theme;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.camera.CameraTab;
import us.mn.state.dot.tms.client.detector.DetectorManager;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.dms.DMSTab;
import us.mn.state.dot.tms.client.incident.IncidentManager;
import us.mn.state.dot.tms.client.incident.IncidentTab;
import us.mn.state.dot.tms.client.lcs.LcsTab;
import us.mn.state.dot.tms.client.lcs.LCSArrayManager;
import us.mn.state.dot.tms.client.lcs.LCSIManager;
import us.mn.state.dot.tms.client.marking.LaneMarkingManager;
import us.mn.state.dot.tms.client.meter.RampMeterTab;
import us.mn.state.dot.tms.client.meter.MeterManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.roads.FreewayTheme;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.client.roads.RoadwayTab;
import us.mn.state.dot.tms.client.roads.SegmentLayer;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.warning.WarningSignManager;

/**
 * A session is one IRIS login session.
 *
 * @author Douglas Lau
 */
public class Session {

	/** Hide the named layer */
	static protected void hideLayer(List<LayerState> lstates, String name) {
		for(LayerState ls: lstates) {
			if(ls.getLayer().getName().equals(name))
				ls.setVisible(false);
		}
	}

	/** Session User */
	protected final User user;

	/** Get the currently logged-in user */
	public User getUser() {
		return user;
	}

	/** SONAR state */
	protected final SonarState state;

	/** Get the SONAR state */
	public SonarState getSonarState() {
		return state;
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Desktop used by this session */
	protected final SmartDesktop desktop;

	/** Get the desktop */
	public SmartDesktop getDesktop() {
		return desktop;
	}

	/** Client properties */
	protected final Properties props;

	/** Get the client properties */
	public Properties getProperties() {
		return props;
	}

	/** Message logger */
	protected final Logger logger;

	/** Get the logger */
	public Logger getLogger() {
		return logger;
	}

	/** Base layers */
	protected final List<Layer> baseLayers;

	/** Segment layer */
	protected final SegmentLayer seg_layer;

	/** Location manager */
	protected final GeoLocManager loc_manager;

	/** Camera manager */
	protected final CameraManager cam_manager;

	/** Get the camera manager */
	public CameraManager getCameraManager() {
		return cam_manager;
	}

	/** DMS manager */
	protected final DMSManager dms_manager;

	/** Get the DMS manager */
	public DMSManager getDMSManager() {
		return dms_manager;
	}

	/** LCS array manager */
	protected final LCSArrayManager lcs_array_manager;

	/** LCS indication manager */
	protected final LCSIManager lcsi_manager;

	/** Get the LCS indication manager */
	public LCSIManager getLCSIManager() {
		return lcsi_manager;
	}

	/** Lane marking manager */
	protected final LaneMarkingManager lane_marking_manager;

	/** Get the lane marking manager */
	public LaneMarkingManager getLaneMarkingManager() {
		return lane_marking_manager;
	}

	/** Detector manager */
	protected final DetectorManager det_manager;

	/** Get the detector manager */
	public DetectorManager getDetectorManager() {
		return det_manager;
	}

	/** R_Node manager */
	protected final R_NodeManager r_node_manager;

	/** Get the r_node manager */
	public R_NodeManager getR_NodeManager() {
		return r_node_manager;
	}

	/** Warning sign manager */
	protected final WarningSignManager warn_manager;

	/** Get the warning sign manager */
	public WarningSignManager getWarnManager() {
		return warn_manager;
	}

	/** Ramp meter manager */
	protected final MeterManager meter_manager;

	/** Get the ramp meter manager */
	public MeterManager getMeterManager() {
		return meter_manager;
	}

	/** Incident manager */
	protected final IncidentManager inc_manager;

	/** List of all tabs */
	protected final List<MapTab> tabs = new LinkedList<MapTab>();

	/** Get a list of all tabs */
	public List<MapTab> getTabs() {
		return tabs;
	}

	/** Create a new session */
	public Session(SonarState st, SmartDesktop d, Properties p, Logger l,
		List<Layer> bl) throws IOException
	{
		state = st;
		user = state.getUser();
		namespace = state.getNamespace();
		desktop = d;
		props = p;
		logger = l;
		baseLayers = bl;
		loc_manager = new GeoLocManager(state.getGeoLocs());
		r_node_manager = new R_NodeManager(this,
			state.getDetCache().getR_Nodes(), loc_manager);
		cam_manager = new CameraManager(this,
			state.getCamCache().getCameras(), loc_manager);
		dms_manager = new DMSManager(this,state.getDmsCache().getDMSs(),
			loc_manager);
		lcs_array_manager = new LCSArrayManager(this, loc_manager);
		lcsi_manager = new LCSIManager(this, loc_manager);
		lane_marking_manager = new LaneMarkingManager(this,
			state.getLaneMarkings(), loc_manager);
		det_manager = new DetectorManager(
			state.getDetCache().getDetectors(), loc_manager);
		warn_manager = new WarningSignManager(this,
			state.getWarningSigns(), loc_manager);
		meter_manager = new MeterManager(this,
			state.getRampMeters(), loc_manager);
		inc_manager = new IncidentManager(this, loc_manager);
		initializeManagers();
		seg_layer = r_node_manager.createSegmentLayer();
		addTabs();
	}

	/** Initialize all the proxy managers */
	protected void initializeManagers() {
		r_node_manager.initialize();
		cam_manager.initialize();
		dms_manager.initialize();
		lcs_array_manager.initialize();
		lcsi_manager.initialize();
		lane_marking_manager.initialize();
		det_manager.initialize();
		warn_manager.initialize();
		meter_manager.initialize();
		inc_manager.initialize();
	}

	/** Add the tabs */
	protected void addTabs() throws IOException {
		if(canRead(DMS.SONAR_TYPE))
			addDMSTab();
		if(canRead(Incident.SONAR_TYPE))
			addIncidentTab();
		if(canRead(RampMeter.SONAR_TYPE))
			addMeterTab();
		if(canRead(LCSArray.SONAR_TYPE))
			addLcsTab();
		if(canRead(Camera.SONAR_TYPE))
			addCameraTab();
		if(canAdd(R_Node.SONAR_TYPE))
			addRoadwayTab();
	}

	/** Add the DMS tab */
	protected void addDMSTab() {
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		tabs.add(new DMSTab(this, dms_manager, lstates));
	}

	/** Create the layer states */
	protected List<LayerState> createLayers() {
		List<LayerState> lstates = createBaseLayers();
		if(seg_layer != null)
			lstates.add(seg_layer.createState());
		if(canRead(Camera.SONAR_TYPE))
			lstates.add(cam_manager.getLayer().createState());
		if(canRead(RampMeter.SONAR_TYPE))
			lstates.add(meter_manager.getLayer().createState());
		if(canRead(DMS.SONAR_TYPE))
			lstates.add(dms_manager.getLayer().createState());
		if(canRead(LCSArray.SONAR_TYPE))
			lstates.add(lcs_array_manager.getLayer().createState());
		if(canRead(WarningSign.SONAR_TYPE))
			lstates.add(warn_manager.getLayer().createState());
		if(canRead(Incident.SONAR_TYPE))
			lstates.add(inc_manager.getLayer().createState());
		return lstates;
	}

	/** Create the base layer states */
	protected List<LayerState> createBaseLayers() {
		LinkedList<LayerState> lstates = new LinkedList<LayerState>();
		for(Layer l: baseLayers)
			lstates.add(l.createState());
		return lstates;
	}

	/** Add the incident tab */
	protected void addIncidentTab() throws IOException {
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new IncidentTab(this, inc_manager, lstates));
	}

	/** Add the meter tab */
	protected void addMeterTab() throws IOException {
		List<LayerState> lstates = createLayers();
		for(LayerState ls: lstates) {
			for(Theme t: ls.getThemes()) {
				if(t instanceof FreewayTheme)
					ls.setTheme(t);
			}
		}
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new RampMeterTab(this, meter_manager, lstates));
	}

	/** Add the LCS tab */
	protected void addLcsTab() throws IOException {
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, inc_manager.getProxyType());
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new LcsTab(this, lcs_array_manager, lstates));
	}

	/** Add the camera tab */
	protected void addCameraTab() {
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new CameraTab(cam_manager, lstates, props, logger,
			state, user));
	}

	/** Add the roadway tab */
	protected void addRoadwayTab() {
		List<LayerState> lstates = createBaseLayers();
		tabs.add(new RoadwayTab(this, r_node_manager, lstates));
	}

	/** Check if the user can add an object */
	public boolean canAdd(String tname) {
		return canAdd(tname, "oname");
	}

	/** Check if the user can add an object */
	public boolean canAdd(String tname, String oname) {
		return oname != null &&
		       namespace.canAdd(user, new Name(tname, oname));
	}

	/** Check if the user can read a type */
	public boolean canRead(String tname) {
		return namespace.canRead(user, new Name(tname));
	}

	/** Check if the user can update an attribute */
	public boolean canUpdate(String tname, String aname) {
		return namespace.canUpdate(user, new Name(tname,"oname",aname));
	}

	/** Check if the user can update an attribute */
	public boolean canUpdate(String tname) {
		return canUpdate(tname, "aname");
	}

	/** Check if the user can update a proxy */
	public boolean canUpdate(SonarObject proxy) {
		return proxy != null &&
		       namespace.canUpdate(user, new Name(proxy));
	}

	/** Check if the user can update a proxy */
	public boolean canUpdate(SonarObject proxy, String aname) {
		return proxy != null &&
		       namespace.canUpdate(user, new Name(proxy, aname));
	}

	/** Dispose of the session */
	public void dispose() {
		desktop.closeFrames();
		for(MapTab tab: tabs)
			tab.dispose();
		r_node_manager.dispose();
		cam_manager.dispose();
		dms_manager.dispose();
		lcs_array_manager.dispose();
		lcsi_manager.dispose();
		lane_marking_manager.dispose();
		det_manager.dispose();
		warn_manager.dispose();
		meter_manager.dispose();
		inc_manager.dispose();
		loc_manager.dispose();
	}
}
