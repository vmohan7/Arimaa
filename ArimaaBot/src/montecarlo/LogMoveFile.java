package montecarlo;

import java.io.*;

// Handles logging data to disk

public class LogMoveFile {

  private static boolean canWrite = true;
  private static String log_file_name = "game_move.log";

  public static void setWrtie(boolean status) {
    canWrite = status;
  }


  // Never prints to screen
  public static void write( String text ) {
    write( log_file_name, text );
  }

  // writes data to disk
  public static void write( String log_file_name, String text ) {
	if (!canWrite)
		return;
    try {
      // Open the file for append
      FileWriter fw = new FileWriter( log_file_name, true );
      fw.write( text + System.getProperty("line.separator") );

      // Close the file
      fw.flush();
      fw.close();

    }
    catch ( FileNotFoundException ex ) {
      System.err.println( "LogFile: File not found: "+log_file_name );
    }
    catch ( Exception e ) {
      System.err.println( "LogFile: Unknown error!" );
    }
  }


  // writes data to disk
  public static void over_write( String log_file_name, String text ) {

    try {
      // Open the file for append
      FileWriter fw = new FileWriter( log_file_name, false );
      fw.write( text + System.getProperty("line.separator") );

      // Close the file
      fw.flush();
      fw.close();

    }
    catch ( FileNotFoundException ex ) {
      System.err.println( "LogFile: File not found: "+log_file_name );
    }
    catch ( Exception e ) {
      System.err.println( "LogFile: Unknown error!" );
    }
  }

}

