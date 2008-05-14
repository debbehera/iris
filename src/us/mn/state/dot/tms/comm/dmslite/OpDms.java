/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.tms.comm.dmslite;

import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.DebugLog;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.DeviceOperation;

import java.io.IOException;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class OpDms extends DeviceOperation {

   // timeout for DMS messages
   static final int TIMEOUT_DMS_DEFAULT_MS = 1000*30;
   static final int TIMEOUT_DMS_MODEM_MS = 1000*45*5;
   static final int TIMEOUT_DMS_WIZARD_MS = 1000*30;

    /** DMS debug log */
    static protected final DebugLog DMS_LOG = new DebugLog("dms");

    /** DMS to operate */
    protected final DMSImpl m_dms;

    /** Create a new DMS operation */
    public OpDms(int p, DMSImpl d) {
        super(p, d);
        m_dms = d;
    }

    /** Log exceptions in the DMS debug log */
    public void handleException(IOException e) {
        if (e instanceof ChecksumException) {
            ChecksumException ce = (ChecksumException) e;
            DMS_LOG.log(m_dms.getId() + " (" + toString() + "), " + ce.getScannedData());
        }

        super.handleException(e);
    }

    /** Cleanup the operation */
    public void cleanup() {
        m_dms.setReset(success);
        super.cleanup();
    }

    /** return the timeout for this operation */
    public int calcTimeoutMS() {
	assert m_dms!=null : "m_dms is null in OpDms.getTimeoutMS()";
	String a=m_dms.getSignAccess();
	int ms=TIMEOUT_DMS_DEFAULT_MS;
	if (a.toLowerCase().contains("modem")) {
        	ms=TIMEOUT_DMS_MODEM_MS;
		//System.err.println("connection type is modem:"+a+", dms="+m_dms.toString());
	} else if (a.toLowerCase().contains("wizard")) {
        	ms=TIMEOUT_DMS_WIZARD_MS;
		System.err.println("connection type is wizard:"+a+", dms="+m_dms.toString());
	} else {
        	ms=TIMEOUT_DMS_DEFAULT_MS;
		// unknown sign type, this happens when the first 
	        // OpDmsQueryConfig message is being sent, so a 
	        // default timeout should be assigned.
		//System.err.println("OpDms.calcTimeoutMS(): unknown sign access type:"+a+", dms="+m_dms.toString());
	}
        return ms;
    }

    /** set message attributes which are a function of the operation, sign, etc. */
    public void setMsgAttributes(Message m) {
 	m.setTimeoutMS(this.calcTimeoutMS());
   }

}

