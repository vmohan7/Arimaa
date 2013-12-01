package utilities.helper_classes;

import java.util.concurrent.TimeUnit;

import naive_bayes.NBMain;

public class Utilities {
	
	/* If set to false, then log statements will be printed. If set to true, then only 
	 * results will be reported, in a csv-importable format. */
	public static boolean PARSEABLE_OUTPUT = false;
	
	/** Prints message without appending a new line. Use this for any message logging 
	 * that is not meant to be machine-parseable. 
	 * @param msg Message for human to read */
	public static void printInfoInline(String msg){
		if (!PARSEABLE_OUTPUT)
			System.out.print(msg);
	}

	/** Prints message and appends a new line. Use this for any message logging 
	 * that is not meant to be machine-parseable. 
	 * @param msg Message for human to read */
	public static void printInfo(String msg){
		if (!PARSEABLE_OUTPUT)
			System.out.println(msg);
	}

	/** Prints message and appends a new line. Use this for any results reporting 
	 * meant to be machine-parseable. 
	 * @param msg Message for parser to read */	
	public static void printParseable(String msg){
		if (PARSEABLE_OUTPUT)
			System.out.println(msg);		
	}
	
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
		//		i.e., 11 days, 10 minutes prints 11 days, 0 hours (because 0 hours is passed in)
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
