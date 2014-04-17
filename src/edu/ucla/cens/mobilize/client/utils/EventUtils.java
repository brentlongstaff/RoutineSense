package edu.ucla.cens.mobilize.client.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.user.client.rpc.core.java.util.Arrays;
import com.google.gwt.user.client.ui.Widget;

import edu.ucla.cens.mobilize.client.common.EventLabel;
import edu.ucla.cens.mobilize.client.common.LocationStatus;
import edu.ucla.cens.mobilize.client.common.MobilityMode;
import edu.ucla.cens.mobilize.client.model.EventInfo;
import edu.ucla.cens.mobilize.client.model.MobilityInfo;

public class EventUtils {
	private final static int MINUTES_IN_DAY = 24*60;
	private static final List<String> colorList;	// color map
	
    static {
    	List<String> staticList = new LinkedList<String>();
    	staticList.add("#ea5855");	// Earth-tone red
    	staticList.add("#f3b359");	// Earth-tone orange
    	staticList.add("#f4e64b");	// Earth-tone yellow
    	staticList.add("#95d480");	// Earth-tone green
    	staticList.add("#18b3f0");	// Earth-tone blue
    	// TODO more or figure out what to do when there are too many
    	// high contrast options:
//    	Color.FromArgb(0, 0, 255),      //Blue
    	staticList.add("#0000ff");
//	    Color.FromArgb(255, 0, 0),      //Red
    	staticList.add("#ff0000");
//	    Color.FromArgb(0, 255, 0),      //Green
    	staticList.add("#00ff00");
//	    Color.FromArgb(255, 255, 0),    //Yellow
    	staticList.add("#ffff00");
//	    Color.FromArgb(255, 0, 255),    //Magenta
    	staticList.add("#ff00ff");
//	    Color.FromArgb(255, 128, 128),  //Pink
    	staticList.add("#ff8080");
//	    Color.FromArgb(128, 128, 128),  //Gray
    	staticList.add("#8f8080");
//	    Color.FromArgb(128, 0, 0),      //Brown
    	staticList.add("#800000");
//	    Color.FromArgb(255, 128, 0),    //Orange
    	staticList.add("#ff8000");
    	colorList = Collections.unmodifiableList(staticList);
    }
	private final static String nullColor = "#dcdcdc"; // Earth-tone gray for no events at that point
	private final static String errorColor = "#000000"; // black for error
	/**
	 * @param mode MobilityMode
	 * @return Hex color code, e.g. "#ABCDEF"
	 */
	public static String getMobilityHTMLHexColor(String label, Map<String, String> colorMap) {
		if (!colorMap.containsKey(label))
			return errorColor;
		return colorMap.get(label);
	}
	
	public static String getColor(int index)
	{
		if (index < colorList.size())
			return colorList.get(index);
		else
		{
			return errorColor;
		}
	}
	
	/**
	 * @param labelMap 
	 * @return Mode to color map
	 */
	public static Map<String, String> getEventColorMap(HashMap<String, EventInfo> labelMap) {
		Map<String, String> colorTable = new HashMap<String, String>();
		int index = 0;
		for (String str : labelMap.keySet())
		{
			colorTable.put(str, getColor(index++));
		}
		colorTable.put(EventLabel.EmptyLabel().toString(), nullColor);
		return colorTable;
	}
	
	/**
	 * @param date Date to calculate minutes
	 * @return Elapsed time in minutes for the single day
	 */
	public static int getTimeInMinutes(Date date) {
		int mod = 0;
		boolean correcttz = false;
		if (correcttz)
		{
			if (date.getHours() >= 17)
				mod = -17;
			else mod = 7;
		}
		return (60*date.getHours() + date.getMinutes()) + 60*mod;
	}
	
//	public static Widget createLocationIDMapWidget(final double lat, final double lon)
//	{
//		
//	}

	/**
	 * Generates a widget containing a temporal summary of the bucketed mobility data. If buckets contains all ERROR modes, this will be indicated via a text overlay on the graph. All invalid parameters returns null
	 * @param buckets the List of MobilityMode data for an entire day
	 * @param interval the minute duration of each bucket
	 * @param width the output widget width
	 * @param height the output widget height
	 * @param showAxisLabels True to show axis time labels (e.g. 3pm) and pad borders with white space; False to hide
	 * @param showLegend True to show a legend on the right of the plot (this would reduce the plot size); False to hide
	 * @return the HTML5 canvas widget
	 */
	public static Widget createLocationEventsBarChartCanvasWidget(final List<EventLabel> buckets, final int interval, final int width, final int height, boolean showAxisLabels, boolean showLegend, HashMap<String, EventInfo> labelMap, Map<String, String> colorTable) {
		// Color mapping for each MobilityMode; this is hardcoded here for function portability
		
		
		//--- (0) Error checking
		if (buckets == null || buckets.size() == 0 || interval <= 0 || width <= 0 || height <= 0) {
			return null;
		}
		
		//--- (1) Set up canvas, determine offsets
		Canvas canvas = Canvas.createIfSupported();
		if (canvas == null)
			return null;
		
		canvas.setWidth(Integer.toString(width));
		canvas.setHeight(Integer.toString(height));
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
		
		Context2d context = canvas.getContext2d();
		
		int axisLabelHeight = (showAxisLabels) ? 20 : 0;
		int legendWidth = (showLegend) ? 200 : 0;
		int plotXoffset = (showAxisLabels) ? 20 : 0;
		int plotYoffset = (showAxisLabels) ? 10 : 0;
		int plotWidth = width - 2*plotXoffset - legendWidth;
		int plotHeight = height - 2*plotYoffset - axisLabelHeight;
		
		// --- (2) Draw X-axis
		if (showAxisLabels) {
			final int hourInterval = 3;	// NOTE: Must be a common factor of 24
			for (int hour = 0; hour <= 24; hour += hourInterval) {
				// Calculate tick mark origin
				double x, y;
				x = plotXoffset + (double)hour * (double)plotWidth / 24.0;
				y = plotYoffset + plotHeight;
				
				// Draw tick mark
				context.setLineWidth(1);
				context.setStrokeStyle(CssColor.make("#CCCCCC"));
				context.beginPath();
				context.moveTo(x, y+3);
				context.lineTo(x, y+10);
				context.stroke();
				
				// Draw label underneath tick mark
				String str = getPrettyHourStr(hour);
				context.setFillStyle(CssColor.make("#666666"));
				context.setFont("bold 8pt Arial");
				context.setTextAlign(TextAlign.CENTER);
				context.setTextBaseline(TextBaseline.TOP);
				context.fillText(str, x, y+10);
			}
		}
		
		// --- (3) Draw legend
		if (showLegend) {
			double legendYspacing = (double)plotHeight / (double)colorTable.size();
			double legendYoffset = legendYspacing / 2.0;
			double legendXoffset = 40;
			
			int keyCount = 0;
			ArrayList<String> orderedKeys = new ArrayList<String>(); 
			orderedKeys.addAll(colorTable.keySet());
			Collections.sort(orderedKeys);
			for (String m : orderedKeys) {
				// Calculate offset for the color icon and text label
				double x, y, w, h;
				x = plotXoffset + plotWidth + legendXoffset;
				y = plotYoffset + legendYoffset + legendYspacing*keyCount;
				w = 30.0;
				h = legendYspacing - 4.0;
				
				// Draw color icon
				context.setFillStyle(CssColor.make(colorTable.get(m)));
				context.fillRect(x, y - (h / 2.0), w, h);
				
				// Draw text label
				String str = m.toString();
				context.setFillStyle(CssColor.make("#000000"));
				context.setFont("bold 8pt Arial");
				context.setTextAlign(TextAlign.LEFT);
				context.setTextBaseline(TextBaseline.MIDDLE);
				context.fillText(str, x+w+4, y);
				
				keyCount++;
			}
		}
		
		// --- (4) Now, draw the plot
		final int overflow = 24*60 - buckets.size()*interval;
		final double stretchFactor = (double)plotWidth / (24*60);
		boolean hasPlottableData = false;
		
		// Draw white background for plot
		context.setFillStyle(CssColor.make("#FFFFFF"));
		context.fillRect(plotXoffset, plotYoffset, plotWidth, plotHeight);
		
		double x_pos = 0;
		for (int i = 0; i < buckets.size(); i++) {
			double x, y, w, h;
			
			// Determine x,y,w,h to draw rect
			x = plotXoffset + x_pos;
			y = plotYoffset;
			w = stretchFactor * (double) interval;
			h = (double) plotHeight;
			
			// Determine color
			String color = colorTable.get(buckets.get(i).toString());
			int z = 0;
			if (i > 100)
				z = 1;
			if (color == null)
				color = colorTable.get(MobilityMode.ERROR);
			context.setFillStyle(CssColor.make(color));
			
			// Draw rectangle
			context.fillRect(x,y,w,h);
			
			// Increment position
			x_pos += w;
			if (buckets.get(i).equals(EventLabel.EmptyLabel()) == false)
				hasPlottableData = true;
		}
		
		// Fill overflow (if any) with gray rectangles 
		if (overflow > 0) {
			double x, y, w, h;
			x = plotYoffset + x_pos;
			y = plotYoffset;
			w = plotWidth - x_pos;
			h = (double) plotHeight;
			context.setFillStyle(CssColor.make(colorTable.get(MobilityMode.ERROR)));
			context.fillRect(x,y,w,h);
		}
		
		// Draw "No data" text if empty
		if (hasPlottableData == false) {
			double x, y;
			
			// Determine x,y,w,h to draw rect
			x = plotXoffset + (double)plotWidth / 2.0;
			y = plotYoffset + (double)plotHeight / 2.0;
			
			// Draw text label
			String str = "(no valid mobility data for this day)";
			context.setShadowColor("#FFFFFF");
			context.setShadowOffsetX(0.0);
			context.setShadowOffsetY(0.0);
			context.setShadowBlur(8.0);
			context.setFillStyle(CssColor.make("#333"));
			context.setFont("bold 14pt Arial");
			context.setTextAlign(TextAlign.CENTER);
			context.setTextBaseline(TextBaseline.MIDDLE);
			context.fillText(str, x, y);
		}
		
		return canvas;
	}
	
	public static Widget createActivityEventsBarChartCanvasWidget(final List<EventLabel> buckets, final int interval, final int width, final int height, boolean showAxisLabels, boolean showLegend, HashMap<String, EventInfo> labelMap) {
		// Color mapping for each MobilityMode; this is hardcoded here for function portability
		Map<String, String> colorTable = EventUtils.getEventColorMap(labelMap);
		
		//--- (0) Error checking
		if (buckets == null || buckets.size() == 0 || interval <= 0 || width <= 0 || height <= 0) {
			return null;
		}
		
		//--- (1) Set up canvas, determine offsets
		Canvas canvas = Canvas.createIfSupported();
		if (canvas == null)
			return null;
		
		canvas.setWidth(Integer.toString(width));
		canvas.setHeight(Integer.toString(height));
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
		
		Context2d context = canvas.getContext2d();
		
		int axisLabelHeight = (showAxisLabels) ? 20 : 0;
		int legendWidth = (showLegend) ? 200 : 0;
		int plotXoffset = (showAxisLabels) ? 20 : 0;
		int plotYoffset = (showAxisLabels) ? 10 : 0;
		int plotWidth = width - 2*plotXoffset - legendWidth;
		int plotHeight = height - 2*plotYoffset - axisLabelHeight;
		
		// --- (2) Draw X-axis
		if (showAxisLabels) {
			final int hourInterval = 3;	// NOTE: Must be a common factor of 24
			for (int hour = 0; hour <= 24; hour += hourInterval) {
				// Calculate tick mark origin
				double x, y;
				x = plotXoffset + (double)hour * (double)plotWidth / 24.0;
				y = plotYoffset + plotHeight;
				
				// Draw tick mark
				context.setLineWidth(1);
				context.setStrokeStyle(CssColor.make("#CCCCCC"));
				context.beginPath();
				context.moveTo(x, y+3);
				context.lineTo(x, y+10);
				context.stroke();
				
				// Draw label underneath tick mark
				String str = getPrettyHourStr(hour);
				context.setFillStyle(CssColor.make("#666666"));
				context.setFont("bold 8pt Arial");
				context.setTextAlign(TextAlign.CENTER);
				context.setTextBaseline(TextBaseline.TOP);
				context.fillText(str, x, y+10);
			}
		}
		
		// --- (3) Draw legend
		if (showLegend) {
			double legendYspacing = (double)plotHeight / (double)colorTable.size();
			double legendYoffset = legendYspacing / 2.0;
			double legendXoffset = 40;
			
			int keyCount = 0;
			for (String m : colorTable.keySet()) {
				// Calculate offset for the color icon and text label
				double x, y, w, h;
				x = plotXoffset + plotWidth + legendXoffset;
				y = plotYoffset + legendYoffset + legendYspacing*keyCount;
				w = 30.0;
				h = legendYspacing - 4.0;
				
				// Draw color icon
				context.setFillStyle(CssColor.make(colorTable.get(m)));
				context.fillRect(x, y - (h / 2.0), w, h);
				
				// Draw text label
				String str = m.toString();
				context.setFillStyle(CssColor.make("#000000"));
				context.setFont("bold 8pt Arial");
				context.setTextAlign(TextAlign.LEFT);
				context.setTextBaseline(TextBaseline.MIDDLE);
				context.fillText(str, x+w+4, y);
				
				keyCount++;
			}
		}
		
		// --- (4) Now, draw the plot
		final int overflow = 24*60 - buckets.size()*interval;
		final double stretchFactor = (double)plotWidth / (24*60);
		boolean hasPlottableData = false;
		
		// Draw white background for plot
		context.setFillStyle(CssColor.make("#FFFFFF"));
		context.fillRect(plotXoffset, plotYoffset, plotWidth, plotHeight);
		
		double x_pos = 0;
		for (int i = 0; i < buckets.size(); i++) {
			double x, y, w, h;
			
			// Determine x,y,w,h to draw rect
			x = plotXoffset + x_pos;
			y = plotYoffset;
			w = stretchFactor * (double) interval;
			h = (double) plotHeight;
			
			// Determine color
			String color = colorTable.get(buckets.get(i).toString());
			int z = 0;
			if (i > 100)
				z = 1;
			if (color == null)
				color = colorTable.get(MobilityMode.ERROR);
			context.setFillStyle(CssColor.make(color));
			
			// Draw rectangle
			context.fillRect(x,y,w,h);
			
			// Increment position
			x_pos += w;
			if (buckets.get(i).equals(EventLabel.EmptyLabel()) == false)
				hasPlottableData = true;
		}
		
		// Fill overflow (if any) with gray rectangles 
		if (overflow > 0) {
			double x, y, w, h;
			x = plotYoffset + x_pos;
			y = plotYoffset;
			w = plotWidth - x_pos;
			h = (double) plotHeight;
			context.setFillStyle(CssColor.make(colorTable.get(MobilityMode.ERROR)));
			context.fillRect(x,y,w,h);
		}
		
		// Draw "No data" text if empty
		if (hasPlottableData == false) {
			double x, y;
			
			// Determine x,y,w,h to draw rect
			x = plotXoffset + (double)plotWidth / 2.0;
			y = plotYoffset + (double)plotHeight / 2.0;
			
			// Draw text label
			String str = "(no valid mobility data for this day)";
			context.setShadowColor("#FFFFFF");
			context.setShadowOffsetX(0.0);
			context.setShadowOffsetY(0.0);
			context.setShadowBlur(8.0);
			context.setFillStyle(CssColor.make("#333"));
			context.setFont("bold 14pt Arial");
			context.setTextAlign(TextAlign.CENTER);
			context.setTextBaseline(TextBaseline.MIDDLE);
			context.fillText(str, x, y);
		}
		
		return canvas;
	}
	
	/**
	 * Helper function for createMobilityBarChartCanvasWidget(...)
	 * @param hour the hour of the day represented in 24-hours (0 ~ 23), starting at 0 for midnight
	 * @return the string representing the 12-hour format value along with meridiem notation
	 */
	private static String getPrettyHourStr(int hour) {
		hour %= 24;
		String str = "";
		if (hour == 0)		str += "12";
		else if (hour > 12)	str += Integer.toString(hour-12);
		else						str += Integer.toString(hour);
		str += (hour < 12 || hour >= 24) ? "am" : "pm";
		return str;
	}
	
	/**
	 * Makes minutes into pretty hours/min
	 * @param minutes The minutes
	 * @return Pretty time string, e.g. "2 hours & 25 minutes"
	 */
	public static String getPrettyHoursMinutesStr(int minutes) {
		int h = minutes / 60;
		int m = minutes % 60;
		// NOTE: I inlined-if'ed this just for fun. Sorry :) - Eric
		return ((h > 0) ? (Integer.toString(h) + " hour" + ((h != 1) ? "s" : "") + ((m > 0) ? " & " : "")) : "")
				+ ((m > 0) ? (Integer.toString(m) + " minutes" + ((m == 1) ? "s" : "")) : "");
	}
	
	/**
	 * Estimates the duration of each mode
	 * @param data
	 * @return
	 */
	public static Map<String, Integer> getModeDurations(final List<EventInfo> data) {
		final int DEFAULT_INTERVAL = 5;	// Minutes
		final List<EventLabel> bucketed = bucketByInterval(data, DEFAULT_INTERVAL);
		
		Map<String, Integer> durations = new LinkedHashMap<String, Integer>();
		for (EventLabel m : bucketed) {
			addIntegerToModeMap(durations, m.toString(), DEFAULT_INTERVAL);
		}
		
		return durations;
	}
	
	/**
	 * Calculates distance between two sets of lat/long coordinates
	 * @param lat1 latitude 1
	 * @param lng1 longitude 1
	 * @param lat2 latitude 2
	 * @param lng2 longitude 2
	 * @return float distance in meters
	 */
	private static float distanceBetween(float lat1, float lng1, float lat2, float lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
				Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		int meterConversion = 1609;

		return new Float(dist * meterConversion).floatValue();
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public static Map<MobilityMode, Float> getModeDistances(final List<MobilityInfo> data) {
		Map<MobilityMode, Float> distanceMap = new HashMap<MobilityMode, Float>();
		
		// thresholds before we consider it as max travel speeds
		final int timeLimit = 10; //minutes
		final float distanceLimit = 25000;	//meters
		
		// consider classifications as backward-looking
		// noise reduction (later)
		
		MobilityInfo lastGoodPoint = null;
		
		for (int i = 1; i < data.size(); i++) {
			// consecutive check
			MobilityInfo a = data.get(i-1);
			MobilityInfo b = data.get(i);
			
			if (b.getLocationStatus().equals(LocationStatus.UNAVAILABLE))
				continue;
			else
				lastGoodPoint = b;
			
			//if a has location then proceed
			//else if a has no location and lastGoodMobilityPoint still valid
			//else continue
			
			if (a.getLocationStatus().equals(LocationStatus.UNAVAILABLE)) {
				if (lastGoodPoint == null)
					continue;
				
				int diff = EventUtils.getTimeInMinutes(b.getDate()) - EventUtils.getTimeInMinutes(lastGoodPoint.getDate());
				if (diff <= timeLimit)
					a = lastGoodPoint;
				else {
					lastGoodPoint = null;	//clear since it's an invalid/outdated point
					continue;
				}
			}
			
			if (a.getMode() != b.getMode())
				continue;
			
			int timeDiff = EventUtils.getTimeInMinutes(b.getDate()) - EventUtils.getTimeInMinutes(a.getDate());
			if (timeDiff < 0 || timeDiff > timeLimit)
				continue;
			
			float distance = distanceBetween((float)a.getLocationLat(), (float)a.getLocationLong(),
					(float)b.getLocationLat(), (float)b.getLocationLong());
			
			if (distance < 0.0 || distance > distanceLimit)
				continue;
			
			addFloatToModeMap(distanceMap, b.getMode(), distance);
		}
		
		return distanceMap;
	}
	
	/**
	 * TODO: Description
	 * @param data
	 * @return
	 */
	public static Map<MobilityMode, List<Float>> getModeSpeeds(final List<MobilityInfo> data) {
		// NOTE: This function would be much more accurate if we used actual GPS data
		
		Map<MobilityMode, List<Float>> speedAvgMap = new HashMap<MobilityMode, List<Float>>();
		
		// thresholds before we consider it as max travel speeds
		final int timeLimit = 15; //minutes
		final float distanceLimit = 35*timeLimit;	//meters
		
		// consider classifications as backward-looking
		// noise reduction (later)
		
		for (int i = 1; i < data.size(); i++) {
			MobilityInfo a = data.get(i-1);
			MobilityInfo b = data.get(i);
			
			// Make sure we're measuring between two same modes
			if (a.getMode().equals(b.getMode()) == false)
				continue;
			
			if (a.getLocationStatus().equals(LocationStatus.UNAVAILABLE) || b.getLocationStatus().equals(LocationStatus.UNAVAILABLE))
				continue;
			
			float distance = distanceBetween((float)a.getLocationLat(), (float)a.getLocationLong(), (float)b.getLocationLat(), (float)b.getLocationLong());
			if (distance < 0.0 || distance > distanceLimit)
				continue;
			
			int timeDiff = EventUtils.getTimeInMinutes(b.getDate()) - EventUtils.getTimeInMinutes(a.getDate());
			if (timeDiff < 0 || timeDiff > timeLimit)
				continue;
			
			// Calculate speed
			float speed = distance / (float)timeDiff;
			
			// Insert to distanceMap
			if (speedAvgMap.get(b.getMode()) == null) {
				speedAvgMap.put(b.getMode(), new ArrayList<Float>());
			}
			speedAvgMap.get(b.getMode()).add(speed);
		}
		
		return speedAvgMap;
	}
	
	/**
	 * Buckets your single-day, time-sorted list of EventLabel by a specified interval in minutes
	 * @param data Time-sorted list of MobilityInfo for a single day of data
	 * @param intervalInMinutes Interval/resolution to bucket the data
	 * @return List of MobilityModes, where each item represents the time interval starting from midnight (00:00)
	 */
	public static List<EventLabel> bucketByInterval(final List<EventInfo> data, final int intervalInMinutes) {
		// Validate parameters
		if (data == null || intervalInMinutes <= 0) {
			return new ArrayList<EventLabel>();
		}
		
		boolean generateBlankPlot = (data.size() == 0);
		
		int intervals = MINUTES_IN_DAY / intervalInMinutes;
		//int overflow = MINUTES_IN_DAY % intervalInMinutes;	// NOTE: This should be handled by method callee
		
		List<EventLabel> bucketedLabels = new ArrayList<EventLabel>();
		List<EventInfo> bucket = new ArrayList<EventInfo>();
		Map<String, Integer> votes = new LinkedHashMap<String, Integer>();
		
		// TODO: Describe what we're doing
		int curIndex = 0;
		
		for (int i = 0; i < intervals; i++)
		{
			if (generateBlankPlot == false) {
				// Fill up bucket with mobility data within current interval
				if (curIndex < data.size())
				{
					EventInfo m = data.get(curIndex);
					
					// Compute the day's mobility time in minutes
					int curTimeInMin = getTimeInMinutes(m.getDate());
					
					int endTimeInMin = curTimeInMin + (int)m.getDuration() / 60;
					// Check if the current mobility point is within the current interval
					if (curTimeInMin < i * intervalInMinutes) 
					{
						bucket.add(m);
						if (endTimeInMin < i * intervalInMinutes) // only if done
						{
							curIndex++;
						}
						bucketedLabels.add(new EventLabel(m.getEventLabel()));
						continue;
					} 
//					else {
//						prevData = m;
//						break;
//					}
				}
				
			}
//			else
				bucketedLabels.add(EventLabel.EmptyLabel());
		}
		if (true)return bucketedLabels;
		
		for (int i = 0; i < intervals; i++) {
			EventInfo prevData = null;
			bucket.clear();
			votes.clear();
			
			if (generateBlankPlot == false) {
				// Fill up bucket with mobility data within current interval
				while (curIndex < data.size()) {
					EventInfo m = data.get(curIndex);
					
					// Compute the day's mobility time in minutes
					int curTimeInMin = getTimeInMinutes(m.getDate());
					int endTimeInMin = curTimeInMin + (int)m.getDuration() / 60;
					// Check if the current mobility point is within the current interval
					if (curTimeInMin < i * intervalInMinutes) {
						bucket.add(m);
						if (endTimeInMin < i * intervalInMinutes) // only if done
							curIndex++;
					} else {
						prevData = m;
						break;
					}
				}
				
				// Count the durations
				for (int j = 0; j < bucket.size(); j++) {
					// Case 1: Leading duration prior to first data point
					// Case 2: Tailing data point duration
					// Case 3: Calculate mid-progress duration votes
					
					if (j == 0 && prevData != null) {
						// Case 1: head
						addIntegerToModeMap(votes, prevData.getLabel().toString(), getTimeInMinutes(prevData.getDate()));
						prevData = null;	// Clear this just to be safe
					}
					
					if (j == bucket.size() - 1) {	// NOTE: This should NOT be an else-if to handle the case where there is only 1 data point in bucket
						// Case 2: tail
						int remainingTime = intervalInMinutes - getTimeInMinutes(bucket.get(j).getDate());
						addIntegerToModeMap(votes, bucket.get(j).getLabel().toString(), remainingTime);
					} else {
						// Case 3: middle
						int elapsedTime = getTimeInMinutes(bucket.get(j+1).getDate()) - getTimeInMinutes(bucket.get(j).getDate());
						addIntegerToModeMap(votes, bucket.get(j).getLabel().toString(), elapsedTime);
					}
				}
			}
			
			// Determine highest vote; if any ties, just choose first one
			Map.Entry<String, Integer> maxEntry = null;
			for (Map.Entry<String, Integer> entry : votes.entrySet()) {
				if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
					maxEntry = entry;
				}
			}
			
			// Add to bucketedModes
			if (maxEntry != null) {
				bucketedLabels.add(new EventLabel(maxEntry.getKey()));
			} else {	// If no data for this interval, we default to "ERROR"
				bucketedLabels.add(EventLabel.EmptyLabel());
			}
		}
		
		return bucketedLabels;
	}
	
	/**
	 * @param votes Map of mobility mode votes (in minutes)
	 * @param key New or existing MobilityMode
	 * @param valueToAdd Positive integer value to add
	 */
	private static void addIntegerToModeMap(Map<String,Integer> votes, String key, int valueToAdd) {
		if (votes == null || key == null || valueToAdd <= 0)	// remove last check if we want to add negative numbers
			return;
		
		if (votes.containsKey(key))
			votes.put(key, votes.get(key) + valueToAdd);
		else
			votes.put(key, valueToAdd);
	}

	/**
	 * Map adding for floats
	 * @param votes
	 * @param key
	 * @param valueToAdd
	 */
	private static void addFloatToModeMap(Map<MobilityMode,Float> votes, MobilityMode key, float valueToAdd) {
		if (votes == null || key == null || valueToAdd <= 0)	// remove last check if we want to add negative numbers
			return;
		
		if (votes.containsKey(key))
			votes.put(key, votes.get(key) + valueToAdd);
		else
			votes.put(key, valueToAdd);
	}
}
