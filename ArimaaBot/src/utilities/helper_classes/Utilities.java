package utilities.helper_classes;

import java.util.concurrent.TimeUnit;

public class Utilities {
	
	public static String msToString(long ms) {
		long days = TimeUnit.MILLISECONDS.toDays(ms);
		long hours = TimeUnit.MILLISECONDS.toHours(ms) % 24; // hours in a day
		long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60; // minutes in an hour
		long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60; // seconds in a minute
		
		StringBuffer sb = new StringBuffer();
		boolean secondsCondition = true;
		boolean minutesCondition = seconds != 0;
		boolean hoursCondition = minutes != 0 || minutesCondition;
		boolean daysCondition = hours != 0 || hoursCondition;
		boolean usedDays = 	  	  appendToBuffer(sb, days,	  "%d day"	  + (days != 1 ? "s" : "") 		+ ", ", daysCondition);
		boolean usedHours =	 	  appendToBuffer(sb, hours,   "%d hour"	  + (hours != 1 ? "s" : "") 	+ ", ", hoursCondition);
		boolean usedMinutes = 	  appendToBuffer(sb, minutes, "%d minute" + (minutes != 1 ? "s" : "") 	+ ", ", minutesCondition);
		/*boolean usedSeconds = */appendToBuffer(sb, seconds, "%d second" + (seconds != 1 ? "s" : ""), secondsCondition);
		String timeStamp = sb.toString();
		
		//TODO: there is an issue if one of the hours, days, minutes is 0--the %d is skipped in the string but the argument is not...
		if (usedDays) return String.format(timeStamp, days, hours, minutes, seconds);
		if (usedHours) return String.format(timeStamp, hours, minutes, seconds);
		if (usedMinutes) return String.format(timeStamp, minutes, seconds);
		/*if (usedSeconds)*/ return String.format(timeStamp, seconds);
	}
	
	//assume sb is valid
	private static boolean appendToBuffer(StringBuffer sb, long time, String toAppend,
			boolean includeNext) { //determines whether to keep ", "
		if (time != 0) {
			String str = includeNext ? 
					toAppend : toAppend.substring(0, toAppend.length() - 2);
			sb.append(str);
			return true;
		}
		
		//TODO: what if 0 seconds only?
		return false;
	}

}
