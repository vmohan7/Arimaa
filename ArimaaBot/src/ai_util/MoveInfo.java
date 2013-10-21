
package ai_util;

import java.io.*;

// Returns a bunch of useful info about the move
// In essence a snap shot
public class MoveInfo implements Serializable {
  public String ponder_move = null;
  public String move_text = null;
  public String pv = null;
  public int eval_score = 0;
  public long search_time_ms = 0;
  public long nodes_searched = 0;
  public int ply = 0;
  public boolean is_game_theoretic_score = false;
}
