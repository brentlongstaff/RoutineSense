package edu.ucla.cens.mobilize.client.common;

public class EventLabel {

	public EventLabel(String label)
	{
		name = label;
	}
	
	String name;
	@Override
	public String toString() {
		return name;

	}
	public static EventLabel EmptyLabel() {
		// TODO Auto-generated method stub
		return new EventLabel("EMPTY_LABEL");
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return name == ((EventLabel)obj).name;
	}

	
}
