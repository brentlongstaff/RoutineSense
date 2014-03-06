package edu.ucla.cens.mobilize.client.common;

public enum EventType {
	ACTIVITY,
	LOCATION,
	APP,
	SMS,
	CALL,
	//CALENDAR,
	OTHER; // TODO make sure this matches type names
	
	public static EventType fromServerString(String typeInServerFormat) {
		EventType type = null;
		try {
			type = EventType.valueOf(typeInServerFormat.toUpperCase());
		} catch (Exception e) { // invalid mode is returned as ERROR
			type = EventType.OTHER;
		}
		return type;
	}
}