package utilities.helper_classes;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

/** A generic CSV writer, with convenience methods! */
public class CSVDataFormatter {

	private boolean isNewLine;
	private StringBuilder sb;
	
	public CSVDataFormatter() {
		isNewLine = true;
		sb = new StringBuilder();
	}
	
	/**
	 * Add another value to the same line.
	 * @param val value to be added to the comma-separated list
	 */
	public void appendValue(String val) {
		sb.append((isNewLine ? "" : ", ") + val);
		isNewLine = false;
	}
	
	/**
	 * Signify a new line for the file.
	 */
	public void nextLine() {
		sb.append('\n');
		isNewLine = true;
	}
	
	/**
	 * Required to actually print the CSV to a file.
	 * Note: this overwrites existing files.
	 * @param filename The absolute path for the destination.
	 * @return true if the file was successfully written, false otherwise
	 */
	public boolean finalizeFile(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.print(sb.toString());
			pw.close();
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("Invalid filename: " + filename);
			e.printStackTrace();
			return false;
		}	
	}
}
