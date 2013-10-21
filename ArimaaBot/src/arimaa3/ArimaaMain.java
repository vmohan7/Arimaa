package arimaa3;

import java.io.*;
import java.util.*;
import ai_util.*;

public class ArimaaMain {
  public ArimaaMain() {
  }

  // First argument is the file name
  // This file has all the game info in it
  public static void main(String args[]) {

    // Used for testing purposes
//    String file_name = "c:/arimaa/bot/running/gamestate804";
//    execute(file_name);

    // This is for arimaa online server
    if (args.length == 3) {
      execute(args[2]);
    }

    // This is for offline match player
    if (args.length == 5) {
      int search_time = Integer.parseInt(args[1]);
      fixed_match(args[2], search_time);
    }

  }


  /**
   * This is for offline match player
   * @param pos_file_name String
   * @param search_time int
   */
  private static void fixed_match(String pos_file_name, int search_time) {

    try {
      BufferedReader file_reader = new BufferedReader(new
        FileReader(pos_file_name));

      // Get the position from the file
      String position_text = "";
      String data;
      while ((data = file_reader.readLine()) != null) {
        position_text += data + "%13";
      }
      ;

      GameState position = new GameState(position_text);
      ArimaaEngine engine = new ArimaaEngine();
      LogFile.setMessageDisplay(false);
      engine.setMaxSearchDepth(99);

      MoveInfo move = engine.genMove(position, search_time); // Fixed search time

      // remove any pass words, as arimaa-online doesn't want them
      String final_move = move.move_text.replaceAll(" pass", "");
      LogFile.write("Engine Move: *" + final_move + "*");
      System.out.println(final_move);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private static void execute(String file_name) {

    long start_time = System.currentTimeMillis();

    // Load info from engine config file
    int max_search_depth = 99;
    boolean rated_game_only_mode = false;

    try {
      BufferedInputStream file_reader = new BufferedInputStream(new
        FileInputStream("clueless.cfg"));
      Properties gameData = new Properties();
      gameData.load(file_reader);
      String text = gameData.getProperty("max_search_depth");
      max_search_depth = Integer.parseInt(text);
      String text2 = gameData.getProperty("rated_game_only_mode").toLowerCase();
      rated_game_only_mode = Boolean.parseBoolean(text2);
    } catch (Exception e) {
      max_search_depth = 99;
      rated_game_only_mode = false;
    }

    LogFile.write("Clueless.cfg Max Search Depth: " + max_search_depth);
    LogFile.write("Clueless.cfg Rated game only mode: " + rated_game_only_mode);

    try {


//      if ( true ) {
//        System.out.println("resign"); // This actually works!!!
//        return;
//      }


      ArimaaServerInfo info = new ArimaaServerInfo(file_name);

      // Check if we are only playing rated games
      if (rated_game_only_mode) {
        if (!info.is_rated && info.tc_game_length>60) {
          LogFile.write("Unrated game. I resign");
          System.out.println("resign"); // This actually works!!!
          return;
        }
      }

      // Get the engine to figure out a move
      ArimaaEngine engine = new ArimaaEngine();
      LogFile.setMessageDisplay(false);
      engine.setMaxSearchDepth(max_search_depth);
      MoveInfo move = engine.genMove(info);

      // remove any pass words, as arimaa-online doesn't want them
      String final_move = move.move_text.replaceAll(" pass", "");

      LogFile.write("Engine Move: *" + final_move + "*");
      System.out.println(final_move);

      long elapsed_time = System.currentTimeMillis() - start_time;
      LogFile.write("Total Elapsed Time: "+elapsed_time+"\n");

    // This is here for debug purposes, in case of disaster :-0
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
