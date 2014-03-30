package edu.ucla.cens.mobilize.client.model;

import java.util.ArrayList;
import java.util.Date;

import edu.ucla.cens.mobilize.client.common.EventLabel;
import edu.ucla.cens.mobilize.client.common.EventType;
import edu.ucla.cens.mobilize.client.common.MobilityMode;

public class EventInfo 
{
	// standard event attributes
	Date ts;			//timestamp
	String tz;			//timezone
	Long duration;		// length in seconds
	String label;		// depends on the type, could be app name or collection of apps name or activity mode &c
	
	// descriptors to describe events to users
	
	// location
	double latitude;
	double longitude;
	String direction;
	ArrayList<String> apps;
	

	EventType type;
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public ArrayList<String> getApps() {
		return apps;
	}
	public void setApps(ArrayList<String> apps) {
		this.apps = apps;
	}
	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	public Date getDate() {
		return ts;
	}
	public void setDate(Date ts) {
		this.ts = ts;
	}
	public String getTimezone() {
		return tz;
	}
	public void setTimezone(String tz) {
		this.tz = tz;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	
	 
	
}
