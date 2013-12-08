package utilities.helper_classes;

import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import arimaa3.GameState;
import feature_extractor.FeatureExtractor;

public class Utilities {
	
	public static final String col_text = "abcdefgh";
	public static final String dir_text = "nsewx";
	
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
	
	public static byte[] getStepSources(String move_text) {
		
		byte[] stepSources = {-1, -1, -1, -1};
		StringTokenizer tokenizer = new StringTokenizer(move_text);
		int stepNumber = 0;
		
		while (tokenizer.hasMoreTokens()) {
			String move = tokenizer.nextToken();

			if (move.equals("pass")) {
				break;
			}

			// Convert the text move to numbers
			int col = col_text.indexOf(move.substring(1, 2));
			int row = Integer.parseInt(move.substring(2, 3)) - 1;
			int direction = dir_text.indexOf(move.substring(3, 4));

			//If not a capture
			if (direction != 4)
				stepSources[stepNumber++] = (byte) (row * 8 + col);
		}
		
		return stepSources;
	}

	/** Calculates the piece type (e.g. 3) for each piece id (e.g. black dog) for the current game state.
	 * @return byte[] containing the above calculation (for each id) */
	public static byte[] calculatePieceTypes(GameState curr){
		byte[] pieceTypes = new byte[curr.piece_bb.length];
		
		for (int i = 0; i < 2; i++){ // calculate for rabbits 
			byte numStronger = FeatureExtractor.countOneBits(curr.stronger_enemy_bb[i]);
			if (numStronger < 5)
				pieceTypes[i] = 7;
			else if (numStronger < 7)
				pieceTypes[i] = 6;
			else
				pieceTypes[i] = 5;
		}
		
		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
			byte numStronger = FeatureExtractor.countOneBits(curr.stronger_enemy_bb[i]);
			switch (numStronger) {
				case 0: pieceTypes[i] = 0; break;
				case 1: pieceTypes[i] = 1; break;
				case 2: pieceTypes[i] = 2; break;
				case 3: case 4: pieceTypes[i] = 3; break;
				default: pieceTypes[i] = 4; break;
			}
		}
		
		return pieceTypes;
	}
	
}
