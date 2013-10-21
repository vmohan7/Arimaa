package arimaa3;

import ai_util.*;

public class ArimaaClockManager {

  // This function manages the time useage
  public ArimaaClockManager() {
  }


  /**
   * Calculate time control for the search
   * @param info ArimaaServerInfo
   * @return SearchTimes
   */
  public SearchTimes calculate(ArimaaServerInfo info) {

    SearchTimes result = new SearchTimes();

    // Log some info for debugging purposes
    LogFile.message( "tc_move: "+info.tc_move );
    LogFile.message( "tc_player_reserve: "+info.tc_player_reserve );
    LogFile.message( "tc_max_turn_time: "+info.tc_max_turn_time );
    LogFile.message( "tc_max_reserve: "+info.tc_max_reserve);

    int internet_lag = 5;
    int move_time = info.tc_move - internet_lag;

    // Determine game pace ie. sec/move
    float game_moves_remaining = 40;
    float pace = move_time + ((float)info.tc_player_reserve)/game_moves_remaining;

    // Calculate base search time
    float base_time = .75f * pace;

    // If the reserve is full, use all initial time
    // since we can't bank anymore
    float reserve_build_time = move_time - base_time;
    if (info.tc_max_reserve <= info.tc_player_reserve + reserve_build_time) {
      base_time = info.tc_move;
    }

    // Don't overstep max time per move
    base_time = Math.min(base_time,info.tc_max_turn_time-internet_lag);

    // Calcluate panic search time
    // Use roughly 1/3 of reserve if in trouble
    int extra_time = Math.max(((info.tc_player_reserve - 30) / 3),0);
    int panic_time = move_time + extra_time;
    // Don't overstep max time per move
    panic_time = Math.min(panic_time,info.tc_max_turn_time-internet_lag);

    // Calculate mate search time
    // Use all available time except for 5 seconds
    int mate_time = move_time + info.tc_player_reserve - 5;
    // Don't overstep max time per move
    mate_time = Math.min(mate_time,info.tc_max_turn_time-internet_lag);

    // Copy data to result buffer
    result.base_time_sec = (int)base_time;
    result.panic_time_sec = panic_time;
    result.mate_time_sec = mate_time;

    // use special time control for test games against weak bots
    // so the games don't take forever
    if (info.enemy_name.equals("bot_shallowblue")) {
      result.base_time_sec = 10;
      result.panic_time_sec = 10;
      result.mate_time_sec = 10;
    }
    if (info.enemy_name.equals("bot_arimaazilla")) {
      result.base_time_sec = 10;
      result.panic_time_sec = 10;
      result.mate_time_sec = 10;
    }
    if (info.enemy_name.equals("bot_arimaalon")) {
      result.base_time_sec = 10;
      result.panic_time_sec = 10;
      result.mate_time_sec = 10;
    }
    if (info.enemy_name.equals("bot_arimaanator")) {
      result.base_time_sec = 60;
      result.panic_time_sec = 60;
      result.mate_time_sec = 60;
    }


    return result;
  }
}


