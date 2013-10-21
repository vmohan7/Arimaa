package arimaa3;

import java.io.*;
import java.util.*;
import ai_util.*;


public class ArimaaServerInfo {

  public String enemy_name = "";
  public String move_list = "";
  public String position = "";

  // All times are in seconds
  public int tc_player_reserve = -1;
  public int tc_enemy_reserve = -1;
  public int tc_max_reserve = -1;

  public int tc_move = -1;
  public int tc_max_turn_time = -1;
  public int tc_game_length = -1;
  public int tc_max_game_length = -1;

  // Perecentage of left over time transfered to reserve
  public int tc_percent = -1;

  public boolean is_rated = true;


  /**
   * Uses only provided position
   * @param position GameState
   */
  public ArimaaServerInfo(GameState position) {
    this.position = position.toEPDString();
  }


  /**
   * Uses a gamestate file from arimaa server
   * @param file_name String
   */
  public ArimaaServerInfo(String file_name) {
    try {

      // Process the gamestate file from the server
      BufferedInputStream file_reader = new BufferedInputStream(new
        FileInputStream(file_name));
      Properties gameData = new Properties();
      gameData.load(file_reader);

      // Debug purposes only
      //gameData.list(System.out);


      // Figure out who the opponent is
      String enemy_text = "unknown";
      String white_text = gameData.getProperty("wplayer").toLowerCase();
      String black_text = gameData.getProperty("bplayer").toLowerCase();
      String player_text = gameData.getProperty("turn").toLowerCase();

      // Get the name of the opponent
      enemy_text = player_text.equals("w") ? black_text : white_text;
      enemy_text = enemy_text.substring(2);

      this.enemy_name = enemy_text;

      // Get the position
      this.move_list = gameData.getProperty("moves");
      this.position = gameData.getProperty("position");

      // Get the time control info
      String side_to_move_text = gameData.getProperty("turn");
      String tc_reserve_text = gameData.getProperty((side_to_move_text.equals(
        "w")) ? "tcwreserve" : "tcbreserve");
      String tc_enemy_reserve_text = gameData.getProperty((side_to_move_text.
        equals(
          "b")) ? "tcwreserve" : "tcbreserve");
      this.tc_player_reserve = Integer.parseInt(tc_reserve_text);
      this.tc_enemy_reserve = Integer.parseInt(tc_enemy_reserve_text);

      this.tc_max_reserve = Integer.parseInt(gameData.getProperty("tcmax"));
      if (this.tc_max_reserve == 0) {
        this.tc_max_reserve = Integer.MAX_VALUE;
      }

      this.tc_move = Integer.parseInt(gameData.getProperty("tcmove"));
      this.tc_game_length = Integer.parseInt(gameData.getProperty("tcgame","0"));

      this.tc_max_game_length = Integer.parseInt(gameData.getProperty("tctotal"));
      if (this.tc_max_game_length == 0) {
        this.tc_max_game_length = Integer.MAX_VALUE;
      }

      this.tc_max_turn_time = Integer.parseInt(gameData.getProperty(
        "tcturntime"));
      if (this.tc_max_turn_time == 0) {
        this.tc_max_turn_time = Integer.MAX_VALUE;
      }

      this.tc_percent = Integer.parseInt(gameData.getProperty("tcpercent"));


      // Get rated game info
      String rated = gameData.getProperty("rated");
      this.is_rated = rated.equals("0") ? false: true;


      // If there is a problem we are dead anyway
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

  }

}
