package utilities.helper_classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
			Utilities.printInfo("File ouput to " + (new File(filename)).getAbsolutePath());
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("Invalid filename: " + filename);
			e.printStackTrace();
			return false;
		}	
	}
	
	//to test functionality
	public static void main(String args[]) {
		final String filename = "CSVTester.csv";
		CSVDataFormatter csvDF = new CSVDataFormatter();
		
		String[] values = {"1", "2", "3", "4"};
		for (int i = 0; i < values.length; i++)
			csvDF.appendValue(values[i]);
		csvDF.nextLine();
		for (int i = 0; i < values.length - 1; i++)
			csvDF.appendValue(values[i]);
		csvDF.nextLine();
		for (int i = values.length - 1; i >= 0; i--)
			csvDF.appendValue(values[i]);
		
		if (!csvDF.finalizeFile(filename)) return;
		
		File f = new File(filename);
		Utilities.printInfo("File absolute path: " + f.getAbsolutePath());
		
		Utilities.printInfo("File validation result: " + validateFile(filename));
	}
	
	public static boolean validateFile(String filename) {
		BufferedReader rd;
		try {
			FileReader f;
			rd = new BufferedReader(f = new FileReader(filename));
			while (true) {
				String line = rd.readLine();
				if (line == null) break;
				Utilities.printInfo(line);
			}
			rd.close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
		
	}
}
