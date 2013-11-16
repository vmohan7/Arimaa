package utilities.helper_classes;

import java.util.concurrent.TimeUnit;

public class Utilities {
	
	public static String msToString(long milliseconds) {
		return String.format("%d min, %d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(milliseconds),
			    TimeUnit.MILLISECONDS.toSeconds(milliseconds) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
			);
	}

}
