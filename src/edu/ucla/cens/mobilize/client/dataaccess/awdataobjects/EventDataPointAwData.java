package edu.ucla.cens.mobilize.client.dataaccess.awdataobjects;

import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

import edu.ucla.cens.mobilize.client.common.EventType;

/*
"result":"success",
"data":
[
    { <--- *** EventDataPoint JsArray ***
        "m":"still",
        "ts":"09-13-2010 12:34:56",
        "tz":"PST",
        "ls": "valid",
        "l":{
           "la": 34.44434343,
           "lo": -43.3443343,
           "pr":"GPS",
           "t":"1322720340280",
           "ac":30
        }
    },..
]
*/

public class EventDataPointAwData extends QueryAwData {
	protected EventDataPointAwData() {};
	
	public final native EventType getType() /*-{ return this.m; }-*/;
	public final native String getTimeStamp() /*-{ return this.ts; }-*/;
	public final native double getTimeRaw() /*-{ return this.t; }-*/;	//needs to be cast to long
	public final long getTime() { return (long)getTimeRaw(); }
	public final native String getTimezone() /*-{ return this.tz; }-*/;
	public final native long getDuration() /*-{ return this.ls; }-*/;
	public final native double getLatitude();
	public final native double getLongitude();
	public final native String getName();
	public final native ArrayList<String> getApps();
	public final native String getDirection();
	//public final native MobilityLocationAwData getLocation() /*-{ return eval('(' + this.l + ')');  }-*/;
	public final native MobilityLocationAwData getLocation() /*-{ return this.l;  }-*/;
}
