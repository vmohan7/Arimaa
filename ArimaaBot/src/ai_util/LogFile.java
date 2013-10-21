package ai_util;

import java.util.*;
import java.io.*;

// Handles logging data to disk

public class LogFile {

  private static String log_file_name = "game.log";

  private static boolean show_thinking = true;
  public static void setMessageDisplay(boolean status) {
    show_thinking = status;
  }

  // Only prints to screen if setMessageDisplay == true
  public static void message(String text) {
    LogFile.write(text);
    if (show_thinking) {
      System.out.println(text);
    }
  }

  // Always prints to screen
  public static void println( String text ) {
    System.out.println(text);
    System.out.flush();
    write( log_file_name, text );
  }

  // Never prints to screen
  public static void write( String text ) {
    write( log_file_name, text );
  }

  // writes data to disk
  public static void write( String log_file_name, String text ) {

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

