package arimaa3;

import ai_util.*;
import java.util.*;

import utilities.MyDB;

public class ArimaaEngine extends ArimaaBaseClass implements Runnable {
  public ArimaaEngine() {
  }


  // Sets the maximum search depth (excluding extensions)
  private int max_search_depth = MAX_PLY;

  public void setMaxSearchDepth(int max_depth) {
    // Force condition  0 <= max_search_depth <= MAX_PLY
    this.max_search_depth = Math.min(max_depth, MAX_PLY);
    this.max_search_depth = Math.max(0, max_search_depth);
  }


  TimeControl time_control = new TimeControl();

  /**
   * Generates a move for the arimaa server
   * @param info ArimaaServerInfo
   * @return MoveInfo
   */
  public MoveInfo genMove(ArimaaServerInfo info) {
    // Setup time control stuff
    ArimaaClockManager clock = new ArimaaClockManager();
    SearchTimes times = clock.calculate(info);
    return genMove(info, times);
  }


  /**
   * Used for testing
   * Generates a move using fixed search time
   * @param gs GameState
   * @param fixed_search_time int
   * @return MoveInfo
   */
  public MoveInfo genMove(GameState gs, int fixed_search_time) {

    SearchTimes times = new SearchTimes();
    times.base_time_sec = fixed_search_time;
    times.panic_time_sec = fixed_search_time;
    times.mate_time_sec = fixed_search_time;

    ArimaaServerInfo info = new ArimaaServerInfo(gs);

    return genMove(info, times);
  }


  private long duplicate_positions[] = null;

  /**
   * Common genmove entry point
   * @param info ArimaaServerInfo
   * @param times SearchTimes
   * @return MoveInfo
   */
  public MoveInfo genMove(ArimaaServerInfo info, SearchTimes times) {

    GameState gs = new GameState(info.position);
    resetStats();

    // Print some game info in the log file!
    LogFile.message("Opponent: " + info.enemy_name);
    LogFile.message((new Date()).toString());
    LogFile.message("\"" + gs.toEPDString() + "\","); // Formatted for array use
    LogFile.message(gs.toBoardString());
    LogFile.message(gs.toSetupString());
    LogFile.message("Max Search Depth: " + this.max_search_depth);

    LogFile.message("TC: " + times.base_time_sec + " " + times.panic_time_sec +
      " " + times.mate_time_sec);
    time_control.setNominalSearchTime(times.base_time_sec * 1000);
    time_control.setPanicSearchTime(times.panic_time_sec * 1000);
    time_control.setMateSearchTime(times.mate_time_sec * 1000);
    time_control.setThreshold( -1500); // Panic score threshold
    time_control.setSearchDepthThreshold(4); // Search will be at least this deep
    time_control.setSearchStartTime();

    // Handle threefold repetition cases
    duplicate_positions = ThreeFoldRepetition.setup(info.move_list);

    // Its the first move, call initial position generator
    if (gs.getTurnNumber() == 1) {
      FirstMove first_move = new FirstMove();
      long random_seed = System.currentTimeMillis();
      LogFile.message("First move random seed: " + random_seed);
      String text = first_move.getFirstMove(gs, random_seed);
      MoveInfo move = new MoveInfo();
      move.move_text = text;
      return move;
    }

    // Its not the first move, so we have to search
    return engine_control_code(gs);
  }


  /**
   * Starts the engine in a separate thread.
   * It will search the given position.
   * The caller is required to setup the time control stuff
   * @param position ArimaaPosition
   * @return MoveInfo
   */
  private MoveInfo engine_control_code(GameState position) {
    // Start the time control thread
    thread_position = position;
    Thread engine_control_thread = new Thread(this);
    engine_control_thread.start();

    // Sit here until time is expired
    engine_search_completed = false; // mate score
    thread_mi = null; // best move found
    current_search_depth = -1;

    while ((!time_control.isTimeExpired(current_best_score,
      current_search_depth)
      && !engine_search_completed) || (thread_mi == null)
      ) {
      try {
        Thread.sleep(100);
      } catch (Exception e) {}
    }

    LogFile.message("Time Expired");

    abort_search(); // Kills the engine thread call both!

    // Dump out some search stats
    LogFile.message(getStats());

    // Wait for engine thread to close
    long start_time = System.currentTimeMillis();
    while (!engine_search_completed) {
      try {
        Thread.sleep(100);
      } catch (Exception e) {}
    }
    long wait_time = System.currentTimeMillis() - start_time;
    LogFile.message("Wait Time: " + wait_time);

    return thread_mi;

  }


  // Variables used by engine thread
  GameState thread_position;
  MoveInfo thread_mi;
  boolean engine_search_completed;

  /**
   * This is where the engine control thread starts
   */
  public void run() {
    // Start searching
    IterativeDeepeningRoot2(thread_position);
  }

  private int initial_search_depth = 0;

// This is used by time control to decide when to stop the search.
  private int current_best_score = 0;
  private int current_search_depth = 0;
  private GameState initial_position;

  /**
   * Another attempt at Root Move searching
   * @param position ArimaaPosition
   * @return MoveInfo
   */

  private MoveInfo IterativeDeepeningRoot2(GameState position) {

    // These are used by time control to decide how much time to use
    current_best_score = -SCORE_INFINITY;
    current_search_depth = 0;

    ArimaaMove best_move = null;
    this.initial_position = position;
    GameState initial_gs = position;
    resetStats();
    enable_search();
    GameState new_position = new GameState();

    // do eval preprocessing at the root
    eval.PreProcessRootPosition(position);

    MoveList root_moves = genRootMoves(initial_gs);
    ProcessRootMoves(initial_position, root_moves);

    int offset = setup_search(initial_gs);
    LogFile.message("Score Offset: " + offset);

    // Search until time is expired!
    try {
      for (int depth = initial_search_depth; depth <= max_search_depth; depth++) {
        current_search_depth = depth;
        int iteration_best_score = -SCORE_INFINITY;
        int new_beta = SCORE_INFINITY;

        root_moves.sort();

        // Iterate thru all root moves
        for (ArimaaMove move : root_moves) {

          int temp_score;
          if (move.move_ordering_value >= 30000 ||
            move.move_ordering_value <= -30000) {
            temp_score = move.move_ordering_value;
          } else {

            // Get a score for this move
            new_position.play(move, initial_position);
            temp_score = SearchPosition(new_position, depth,
              iteration_best_score, new_beta);
          }

          move.move_ordering_value = temp_score;

          // We have a new best move
          // Save the best move and score
          if (temp_score > iteration_best_score) {
            iteration_best_score = temp_score;
            current_best_score = temp_score;
            best_move = move;

            // This is a *LOWER* bound on the score!
            // There could be more moves to search!!!
            // Record in hash so PV can be figured out
            hash_table.RecordHash(initial_position.getPositionHash(), depth,
              iteration_best_score,
              HashTable.LOWER_BOUND, move, false);

            // Display search status
            String text = "D:" + Util.Pad(2, depth);
            text += " " + Util.toTimeString(time_control.getElapsedSearchTime());
            text += " " + Util.LeftJustify(8, convertScore(iteration_best_score));
            text += "     " + getPV(initial_position);
            LogFile.message(text);

            // report the results of the move
            MoveInfo mi = new MoveInfo();
            mi.eval_score = current_best_score;
            mi.move_text = gen_turn.getOfficialArimaaNotation(initial_position,
              best_move);
            mi.pv = getPV(position);
            mi.nodes_searched = this.ab_nodes_searched;
            mi.ply = depth;
            mi.search_time_ms = time_control.getElapsedSearchTime();
            thread_mi = mi;

            // If we have a winning mate score we are done!
            if (iteration_best_score >= SCORE_FORCED_WIN) {
              break;
            }
          }

        } // Root move loop
        // Prepare iteration status report
        // Display search status
        String text = "F:" + Util.Pad(2, depth);
        text += " " + Util.toTimeString(time_control.getElapsedSearchTime());
        text += " " + Util.LeftJustify(7, convertScore(current_best_score));
        text += "     ";
        text += " Nodes: " + ab_nodes_searched;
        text += " QNodes: " + q_nodes_searched;
        text += " kNPS: " +
          ab_nodes_searched /
          (time_control.getElapsedSearchTime() + 1);

        LogFile.message(text);

        // If we have a mate score we are done!
        if (isMateScore(iteration_best_score)) {
          break;
        }

      }

    } catch (AbortSearchException ex) {
      LogFile.message("Search Aborted!");
    }

    engine_search_completed = true;
    return thread_mi;
  }


  // Returns true iff score is a forced mate
  public static boolean isMateScore(int score) {
    if (Math.abs(score) > SCORE_FORCED_WIN) {
      return true;
    }
    return false;
  }


  /**
   * Converts score to a text string
   * @param score int
   * @return String
   */
  private String convertScore(int score) {
    String result = score + "";
    if (score >= arimaa3.Constants.SCORE_FORCED_WIN) {
      result = "WON " + (SCORE_MATE - score + 1);
    }
    if (score <= arimaa3.Constants.SCORE_FORCED_LOSS) {
      result = "LOST " + (SCORE_MATE + score + 1);
    }

    return result;
  }


  /**
   * Generate all possible turns from given position
   * @param initial GameState
   * @return MoveList
   */

  GenTurn gen_turn = new GenTurn();

  public MoveList genRootMoves(GameState root_position) {
    MoveList root_moves = new MoveList(400000);
    gen_turn.genAllTurns(root_position, root_moves);
    return root_moves;
  }


  public void ProcessRootMoves(GameState initial_position, MoveList root_moves) {

    // Setup stats
    int opponent_goals = 0;
    int normal_moves = 0;
    int game_over = 0;
    int repetition_banned = 0;

    // Process moves
    GameState gs = new GameState();
    for (ArimaaMove move : root_moves) {

      gs.play(move, initial_position);

      // Check if game is over
      if (gs.isGameOver()) {
        move.move_ordering_value = gs.getGameResult();
        game_over++;
      }

      // Check for repetition banned positions
      else if (isRepetitionBanned(gs.getPositionHash())) {
        move.move_ordering_value = -SCORE_MATE;
        repetition_banned++;
      }

      // Check if opponent can goal
      else if (goal.test(gs)) {
        move.move_ordering_value = -SCORE_MATE + 1;
        opponent_goals++;
      }

      // Must be a normal move
      else {
        move.move_ordering_value = -eval.Evaluate(gs, false);
        normal_moves++;
      }

//      System.out.println(move+" "+move.move_ordering_value);

    }

    // Report stats
    LogFile.message("Root Moves: "+root_moves.size());
    LogFile.message("Game Over: " + game_over);
    LogFile.message("Repetition Banned: " + repetition_banned);
    LogFile.message("Opponent Goals: " + opponent_goals);
    LogFile.message("Normal Moves: " + normal_moves);

  }

  /**
   * Checks if given hash code is a repetition banned position
   * @param hash_code long
   * @return boolean
   */
  private boolean isRepetitionBanned(long hash_code) {

    for (long banned_hash : duplicate_positions) {
      if (banned_hash == hash_code) {
        return true;
      }
    }

    return false;
  }


  private int SearchPosition(GameState position, int depth,
    int alpha,
    int beta) throws
    AbortSearchException {

    int score = AlphaBeta(depth, 0, alpha, beta, position, false, false, true);

    // If we have an interesting move, run verification search
    if (score > alpha) {
      score = AlphaBeta(depth, 0, alpha, beta, position, false, true, true);
    }
    return score;
  }


  private static boolean use_tricks = true;
  private static final boolean use_futility_pruning = true;
  private static final boolean use_hash_table = true;
  private static final boolean use_defend_threat_search = true;
  private static final boolean use_null_move = true;
  private static final boolean use_quiesce = true;

  public static boolean show_alphabeta_search_trace = false;
  private static int trace_level = 5;


  private HashTable hash_table = new HashTable(500000);
  private ArimaaEvaluate2 eval = new ArimaaEvaluate2();

  // Working variables for gen_moves
  private MoveList move_list_stack[] = new MoveList[MAX_PLY];
  private GameState gs_stack[] = new GameState[MAX_PLY];
  private ArimaaMove killer_move1[] = new ArimaaMove[MAX_PLY];
  private ArimaaMove killer_move2[] = new ArimaaMove[MAX_PLY];

  private static final int MAX_KILLERS = 10;
  private ArimaaMove killer_turn[][][] = new ArimaaMove[MAX_PLY][MAX_KILLERS][4];
  private int killer_turn_length[][] = new int[MAX_PLY][MAX_KILLERS];
  {
    for (int i = 0; i < gs_stack.length; i++) {
      gs_stack[i] = new GameState();
    }
    for (int i = 0; i < move_list_stack.length; i++) {
      move_list_stack[i] = new MoveList(1000);
    }
    for (int i = 0; i < killer_move1.length; i++) {
      killer_move1[i] = new ArimaaMove();
      killer_move1[i].clear();
      killer_move2[i] = new ArimaaMove();
      killer_move2[i].clear();
    }

    for (int i = 0; i < killer_turn.length; i++) {
      for (int j = 0; j < killer_turn[i].length; j++) {
        for (int k = 0; k < killer_turn[i][j].length; k++) {
          killer_turn[i][j][k] = new ArimaaMove();
          killer_turn[i][j][k].clear();
        }
      }
    }
  }


  // Used to test if initial position restored
  private GameState saved_initial_position = null;


  // Statistics collection stuff
  public long ab_nodes_searched = 0;
  public long q_nodes_searched = 0;
  private long initial_repeated_positions = 0;
  private long hash_hits = 0;
  private long hash_calls = 0;
  private long goal_calls = 0;
  private long goal_hits = 0;
  private long eval_calls = 0;
  private long enemy_goal_threats = 0;
  private long futility_cuts = 0;
  private long useless_cuts = 0;
  private long defence_nodes_searched = 0;
  private long null_move_calls = 0;
  private long null_move_cuts = 0;

  public void resetStats() {
    ab_nodes_searched = 0;
    initial_repeated_positions = 0;
    hash_hits = 0;
    goal_hits = 0;
    eval_calls = 0;
    goal_calls = 0;
    hash_calls = 0;
    enemy_goal_threats = 0;
    futility_cuts = 0;
    useless_cuts = 0;
    defence_nodes_searched = 0;
    null_move_calls = 0;
    null_move_cuts = 0;

  }

  // Dumps out statistics
  public String getStats() {
    String result = "";
    result += "AB Nodes: " + ab_nodes_searched + "\n";
    result += "Q Nodes: " + q_nodes_searched + "\n";
    result += "Defence Nodes: " + defence_nodes_searched + "\n";
    result += "Init Repeated Positions: " + initial_repeated_positions + "\n";
    result += Util.ProbStats("Hash Table:", hash_calls, hash_hits);
    result += Util.ProbStats("Null Move:", null_move_calls, null_move_cuts);
    result += Util.ProbStats("Test Goal:", goal_calls, goal_hits);
    result += Util.ProbStats("E Goal Thr:", goal_calls, enemy_goal_threats);
    result += "Futility Cuts: " + futility_cuts + "\n";
    result += "Useless Cuts: " + useless_cuts + "\n";
    return result;
  }

  public boolean can_player_goal(GameState gs) {
    saved_initial_position = gs;
    int score = AlphaBetaTest(4, 0, -SCORE_INFINITY, +SCORE_INFINITY, gs);
    if (score == SCORE_MATE) {
      return true;
    }
    return false;
  }


  public class AbortSearchException extends Exception {
  }


  private boolean abort_search_flag;

  /**
   * Timer thread calls this function to abort the search
   */
  public void abort_search() {
    abort_search_flag = true;
  }

  public void enable_search() {
    abort_search_flag = false;
  }


  public int setup_search(GameState root_gs) {
    int score_offset = eval.PreProcessRootPosition(root_gs);
    return score_offset;
  }


  private int AlphaBetaTest(int depth, int ply, int alpha, int beta,
    GameState gs) {

    ab_nodes_searched++;

    // Check if its time to call eval
    if (depth <= 0 || ply >= MAX_PLY) {
      return SCORE_DRAW;
    }

    // Check the hash table if not at the root
    int score = hash_table.ProbeHash(gs.getPositionHash(), depth, alpha, beta, false);
    hash_calls++;
    if (score != HashTable.NO_SCORE && use_tricks) {
      hash_hits++;
      return score;
    }

    // Generate all moves
    MoveList moves = move_list_stack[ply];
    moves.clear();

    long start_bb = FULL_BB;
    long dest_bb = FULL_BB;
    // If only one step left, only generate rabbit goal moves
    if (depth == 1 && use_tricks) {
      start_bb = gs.piece_bb[gs.player];
      dest_bb = (gs.player == PL_WHITE) ? RANK_8 : RANK_1;
    }

    gs.GENERATE_MOVES(moves, start_bb, dest_bb);
    GameState new_gs = gs_stack[ply];

    // Iterate thru the moves
    int moves_searched = 0;
    for (ArimaaMove move : moves) {

      // Make the move
      new_gs.play(move, gs);

      if (show_alphabeta_search_trace) {
        SearchTrace(ply, move.toString());
      }

      // Test for returning to initial position
      if (new_gs.equals(this.saved_initial_position)) {
        initial_repeated_positions++;
        continue;
      }

      // Check if game is over, can be WIN/DRAW/LOSS
      if (new_gs.isGameOver()) {
        // Note must search rest of possible moves!!!
        score = new_gs.getGameResult();
        if (show_alphabeta_search_trace) {
          SearchTrace(ply, "Game Over: " + score);
        }
      } else {
        // Game is NOT over, so we search
        int new_depth = depth - move.steps;
        int new_ply = ply + move.steps;

        // It's the other player's turn
        if (new_gs.getStepsRemaining() == 4) {
          score = -AlphaBetaTest(new_depth, new_ply, -beta, -alpha, new_gs);
        }
        // It's still the same player's turn
        else {
          score = AlphaBetaTest(new_depth, new_ply, alpha, beta, new_gs);
        }
      }

      // Check if we got an interesting score
      if (score > alpha) {
        if (score >= beta) {
          hash_table.RecordHash(gs.getPositionHash(), depth, score,
            HashTable.LOWER_BOUND, null, false);
          return score;
        }
        alpha = score;
      }

      // Loop post operations
      moves_searched++;

      // If we get a mate score we are done
      if (score >= SCORE_FORCED_WIN) {
        break;
      }

    }

    // If we can't move, we lose!
    if (moves_searched == 0) {
      if (show_alphabeta_search_trace) {
        SearchTrace(ply, "No Moves!");
      }
      return -SCORE_MATE;
    }

    hash_table.RecordHash(gs.getPositionHash(), depth, alpha,
      HashTable.UPPER_BOUND, null, false);
    return alpha;
  }


  public void SearchTrace(int ply, int depth, int alpha, int beta,
    ArimaaMove move) {
    String result = "";

    // Output the move
    if (move.move_ordering_value == ORDER_HASH) {
      result += "HM";
    } else if (move.move_ordering_value == ORDER_KILLER_TURN) {
      result += "KT";
    } else if (move.move_ordering_value == ORDER_CAPTURE) {
      result += "CP";
    } else if (move.move_ordering_value == ORDER_KILLER_ONE) {
      result += "K1";
    } else if (move.move_ordering_value == ORDER_KILLER_TWO) {
      result += "K2";
    }

    result += move.toString() + " ";
    result += "[" + alpha + " " + beta + "] ";
    result += depth + " ";

    SearchTrace(ply, result);
  }


  // Outputs the search trace
  protected void SearchTrace(int ply, String text) {
    if (ply < trace_level) {
      String result = Util.LeftJustify(3, ply + "");

      // Provide indentation
      for (int i = 0; i < ply; i++) {
        result += "  ";
      }

      result += text;

      // Debugging only
      LogFile.message(result);

      System.out.println(result);
      System.out.flush();
    }
  }


  // Adjust alpha,beta and result to reflect proper mate bounds
  // This localizes all mate bounds adjustments to here only
  // Select proper ab search mode (ie first step, later step
  // Handles negascout adjustments as well
  // Mate bounds are adjusted on a per turn basis not a per step basis
  // Handles saving initial position

  int AlphaBeta(int depth, int ply, int alpha, int beta, GameState gs,
    boolean is_defend_threat_search, boolean is_verification_search,
    boolean ok_to_null) throws
    AbortSearchException {

    ab_nodes_searched++;

    // Check for abort search
    if (abort_search_flag) {
      throw new AbortSearchException();
    }

    // It's the other player's turn
    if (gs.getStepsRemaining() == 4) {

      // Needed to handle nega scout mate scores adjustments
      if (alpha >= SCORE_MATE - 1) {
        return SCORE_MATE - 1;
      }

      if (beta <= -SCORE_MATE + 1) {
        return -SCORE_MATE + 1;
      }

      int new_alpha = (alpha > SCORE_FORCED_WIN) ? alpha + 1 :
        ((alpha < SCORE_FORCED_LOSS) ? alpha - 1 : alpha);
      int new_beta = (beta > SCORE_FORCED_WIN) ? beta + 1 :
        ((beta < SCORE_FORCED_LOSS) ? beta - 1 : beta);

      assert (new_alpha <= SCORE_MATE - 1);

      // Save the new initial position
      // This is required so steps can't return to the board position at turn start
      GameState temp_position = saved_initial_position;
      saved_initial_position = gs;

      int result = -AlphaBetaMain(depth, ply, -new_beta, -new_alpha, gs, true,
        is_defend_threat_search,
        is_verification_search, ok_to_null);

      // Restore the old initial position;
      saved_initial_position = temp_position;

      int new_result = (result > SCORE_FORCED_WIN) ? result - 1 :
        ((result < SCORE_FORCED_LOSS) ? result + 1 : result);
      return new_result;

    }
    // It's still the same player's turn
    else {
      int result = AlphaBetaMain(depth, ply, alpha, beta, gs, false,
        is_defend_threat_search, is_verification_search, ok_to_null);
      return result;
    }

  }


  /**
   * Just has to handle the search related stuff
   * Timeouts are handled elsewhere
   * NegaMax is handled elsewhere
   *
   * @param depth int
   * @param ply int
   * @param alpha int
   * @param beta int
   * @param gs GameState
   * @param is_first_step boolean
   * @return int
   * @throws AbortSearchException
   */

  private TestForGoal goal = new TestForGoal();


  private int AlphaBetaMain(int depth, int ply, int alpha, int beta,
    GameState gs, boolean is_first_step, boolean is_defend_threat_search,
    boolean is_verification_search, boolean ok_to_null) throws
    AbortSearchException {

    int best_score = -SCORE_INFINITY - 10000;

    // Test for a goal on first step
    if (is_first_step) {
      goal_calls++;
      if (goal.test(gs)) {
        goal_hits++;
        return SCORE_MATE;
      }
    }

    // Check the hash table
    if (use_hash_table) {
      hash_calls++;
      int result = hash_table.ProbeHash(gs.getPositionHash(), depth, alpha,
        beta, is_defend_threat_search);

      if (result != HashTable.NO_SCORE) {
        if (show_alphabeta_search_trace) {
          SearchTrace(ply, "Hash Hit: " + result);
        }

        hash_hits++;
        return result;
      }
    }

    // Test if opponent has a goal threat
    if (use_defend_threat_search) {
      if (is_first_step && depth < 4 && !is_defend_threat_search) {
        gs_stack[ply].playPASS(gs);
        if (goal.test(gs_stack[ply])) {
          enemy_goal_threats++;

          if (show_alphabeta_search_trace) {
            SearchTrace(ply, "Enemy Goal Threat");
          }

          // Check if a defence is possible
          // Use IID to cut search size
          // No NULL move when defending goal threat
          for (int def_depth = depth; def_depth <= 4; def_depth++) {
            int result = AlphaBetaMain(def_depth, ply, alpha, beta, gs, true, true,
              is_verification_search, false);
            if (show_alphabeta_search_trace) {
              SearchTrace(ply, "EGTd: " + def_depth + " Score: " + result);
            }
            if (result > alpha) {
              if (result >= beta) {
                return result;
              }
              alpha = result;
            }
          }

          // No defence found
          if (show_alphabeta_search_trace) {
            SearchTrace(ply, "No Defence Found!");
          }
          return alpha;
//          return ( -SCORE_MATE + 1);
        }
      }
    }

    // Check if its time to call eval
    if (depth <= 0 || ply >= MAX_PLY) {

      // Eval is no good if enemy can goal
      if (is_defend_threat_search) {
        gs_stack[ply].playPASS(gs);
        if (goal.test(gs_stack[ply])) {
          if (show_alphabeta_search_trace) {
            SearchTrace(ply, "Enemy can still goal!");
          }
          return -SCORE_MATE;
        } else {
          eval_calls++;
          return eval.Evaluate(gs, false);
        }
      }

      if (use_quiesce && is_first_step) {
        int result = Quiesce(ply, 2, alpha, beta, gs);
        if (show_alphabeta_search_trace) {
          SearchTrace(ply, "Q result: " + result);
        }
        return result;
      } else {
        eval_calls++;
        int result = eval.Evaluate(gs, false);
        if (show_alphabeta_search_trace) {
          SearchTrace(ply, "Eval: " + result);
        }
        return result;
      }
    }

    GameState new_gs = gs_stack[ply];

    // Try null move pruning
    if (use_null_move) {
      if (ok_to_null) {
        int new_depth = depth - 4; // null move depth reduction?
        int new_ply = ply + gs.getStepsRemaining();
        new_gs.playPASS(gs);
        if (show_alphabeta_search_trace && ply <= trace_level) {
          SearchTrace(ply, "Trying Null move " + beta);
        }
        null_move_calls++;
        int result = AlphaBeta(new_depth, new_ply, beta, beta + 1, new_gs,
          is_defend_threat_search, is_verification_search, false);

        if (show_alphabeta_search_trace && ply <= trace_level) {
          SearchTrace(ply, "Finished Null move " + result);
        }

        if (result >= beta) {
          null_move_cuts++;
          return result;
        }

      }

    }

    // Check for futility pruning
    // 1) Eval says we are going to fail low big time
    // 2) Only one move left to raise the score
    // Only thing to do that is a goal or capture

    // if there are less than 4 steps left, all captures have already been tried,
    // so we are screwed. We can fail low right away!

    // Cant use futility pruning for verification search
    // All moves must be tried

    // Cant use futility pruning on defend threat search
    // We are defending a goal threat, so all moves must be tried

    boolean gen_caps_only = false;
    if (use_futility_pruning && !is_verification_search &&
        !is_defend_threat_search) {
      if (depth < (gs.getStepsRemaining() + 4)) {
        int static_score = eval.Evaluate(gs, false);
        if ((static_score + 1500) < alpha) {
          futility_cuts++;

          // All captures are tried on the first step
          // There is no hope of reaching alpha, so fail low right away.
          if (gs.getStepsRemaining() < 4) {
            useless_cuts++;
            if (show_alphabeta_search_trace) {
              SearchTrace(ply, "Useless cut " + static_score);
            }
            return alpha;
          }

          gen_caps_only = true;
          if (show_alphabeta_search_trace) {
            SearchTrace(ply, "D2 futility: " + static_score);
          }

          // For fail soft just indicate that score will fail low
          // Also, can't return a losing mate score since we are pruning moves!
          // This is a fail hard stand pat score.

          // Currently search is fail hard
          best_score = alpha;

        }
      }
    }

    // Generate all moves
    MoveList moves = move_list_stack[ply];
    moves.clear();
    gen_moves(ply, gs, moves, is_first_step, gen_caps_only);
    ArimaaMove best_move = null;

    // Iterate thru the moves
    int moves_searched = 0;
    for (ArimaaMove move : moves) {

      // Make the move
      new_gs.play(move, gs);

      if (show_alphabeta_search_trace) {
        SearchTrace(ply, depth, alpha, beta, move);
      }

      // Test for returning to initial position
      if (new_gs.equals(this.saved_initial_position)) {
        initial_repeated_positions++;
        if (show_alphabeta_search_trace) {
          SearchTrace(ply, "Initial position repetition");
        }
        continue;
      }

      // Check if game is over, can be WIN/DRAW/LOSS
      int score;
      if (new_gs.isGameOver()) {
        // Note must search rest of possible moves!!!
        score = new_gs.getGameResult();
      } else {
        // Game is NOT over, so we search
        int new_depth = depth - move.steps;
        int new_ply = ply + move.steps;

        // This routine handles mate bounds, timeouts, negamax
        score = AlphaBeta(new_depth, new_ply, alpha, beta, new_gs,
          is_defend_threat_search, is_verification_search, ok_to_null);

        // Determine if verification search is required
        // Any interesting move with depth>=4 must be looked at again
        // The verification search checks for any mate scores
        if (!is_verification_search && new_gs.getStepsRemaining() == 4 &&
          new_depth >= 4 && score > alpha) {
          score = AlphaBeta(new_depth, new_ply, alpha, beta, new_gs,
            is_defend_threat_search, true, ok_to_null);
        }
      }

      if (score > best_score) {
        best_score = score;
        // As opposed to updating if score>alpha YMMV
        best_move = move;

      }

      // Check if we got an interesting score
      if (score > alpha) {
        if (score >= beta) {
          hash_table.RecordHash(gs.getPositionHash(), depth, score,
            HashTable.LOWER_BOUND, move, is_defend_threat_search);
          // Requires hash table already updated
          UpdateKillerMoves(ply, gs, move, false);
          return score;
        }
        alpha = score;
//        best_move = move;
      }

      // Loop post operations
      moves_searched++;

      // If we get a mate score we are done
      if (score >= SCORE_FORCED_WIN) {
        break;
      }

    }

    // If we can't move, we lose!
    // Warning: This is NOT TRUE IF PRUNING IS BEING DONE!!!!
    if (moves_searched == 0 && !gen_caps_only) {
      if (show_alphabeta_search_trace) {
        SearchTrace(ply, "No Moves!");
      }
      return -SCORE_MATE - 1;
    }

    hash_table.RecordHash(gs.getPositionHash(), depth, alpha,
      HashTable.UPPER_BOUND, best_move, is_defend_threat_search);
    return best_score;

  }


  /**
   * Quiescence search
   * Only searches capture turns and goal turns
   * Qsearch is fail hard
   * All moves played in quiesce consume all 4 steps
   */
  private int Quiesce(int ply, int depth, int alpha, int beta, GameState gs) throws
    AbortSearchException {

    // Always need to have full turn available
    assert (gs.getStepsRemaining() == 4);

    // Keep track of how many nodes we have searched
    q_nodes_searched++;

    // Test for a goal on first step
    goal_calls++;
    if (goal.test(gs)) {
      goal_hits++;
      return SCORE_MATE;
    }

    // Get a stand pat score
    int score = eval.Evaluate(gs, false);

    // Limit depth of q search
    if (depth <= 0) {
      return score;
    }

    if (show_alphabeta_search_trace) {
      SearchTrace(ply, "QStand pat score: " + score);
    }
    if (score > alpha) {
      if (score >= beta) {
        return score;
      }
      alpha = score;
    }

    // Generate all moves
    GameState new_gs = gs_stack[ply];
    MoveList moves = move_list_stack[ply];
    moves.clear();
    captures.genCaptures(gs, moves, true);
    moves.sort(); // Currently does nothing!!!!

    ArimaaMove best_move = null;

    for (ArimaaMove move : moves) {

      new_gs.play(move, gs);

      if (show_alphabeta_search_trace) {
        SearchTrace(ply, -1, alpha, beta, move);
      }

      if (new_gs.isGameOver()) {
        // Note must search rest of possible moves!!!
        score = new_gs.getGameResult();
      } else {
        // Game is NOT over, so we search
        int new_ply = ply + move.steps;
        score = -Quiesce(new_ply, depth - 1, -beta, -alpha, new_gs);
      }

      if (score > alpha) {
        if (score >= beta) {
          return score;
        }
        alpha = score;
      }
    }

    return alpha;
  }


  GenCaptures captures = new GenCaptures();
  private static final int ORDER_HASH = 1000;
  private static final int ORDER_KILLER_TURN = 1001;
  public static final int ORDER_CAPTURE = 1002;
  private static final int ORDER_KILLER_ONE = 1003;
  private static final int ORDER_KILLER_TWO = 1004;


  private void gen_moves(int ply, GameState gs, MoveList moves,
    boolean is_first_step, boolean gen_caps_only) {

    // Get hash move
    ArimaaMove hash_move = hash_table.ProbeHashMove(gs.getPositionHash());
    if (hash_move != null) {
      ArimaaMove temp = moves.getMove();
      temp.copy(hash_move);
      temp.move_ordering_value = ORDER_HASH;
    }

    // Get full turn killer moves
    if (gs.getStepsRemaining() == 4) {

      for (int cur_killer = 0; cur_killer <= 3; cur_killer++) {
        temp_gsk.copy(gs);
        ArimaaMove temp = null;
        // Test cur_killer
        for (int i = 0; i < killer_turn_length[ply][cur_killer]; i++) {
          ArimaaMove move = killer_turn[ply][cur_killer][i];
          if (temp_gsk.isLegalMove(move)) {
            // Update gamestate
            temp_gsk.play(move, temp_gsk);

            // Add move to move list
            if (temp == null) {
              temp = moves.getMove();
              temp.clear();
            }
            temp.add(temp, move);
            temp.move_ordering_value = ORDER_KILLER_TURN;

          } else {
            // Move not legal, quit
            break;
          }
        }
      }

    }

    // Get captures
    if (gs.getStepsRemaining() >= 3) {
      captures.genCaptures(gs, moves, false);
    }

    int start_index = moves.size();

    // Get slide/push/pull moves
    if (!gen_caps_only) {
      gs.GENERATE_MOVES(moves, FULL_BB, FULL_BB);

      // Find normal killer moves
      int end_index = moves.size();
      for (int index = start_index; index < end_index; index++) {
        ArimaaMove temp = moves.move_list[index];
        if (temp.equals(killer_move1[ply])) {
          ArimaaMove t2 = moves.move_list[index];
          moves.move_list[index] = moves.move_list[start_index];
          moves.move_list[start_index++] = t2;
          t2.move_ordering_value = ORDER_KILLER_ONE;
        }
        if (temp.equals(killer_move2[ply])) {
          ArimaaMove t2 = moves.move_list[index];
          moves.move_list[index] = moves.move_list[start_index];
          moves.move_list[start_index++] = t2;
          t2.move_ordering_value = ORDER_KILLER_TWO;
        }
      }

    }

  }


  // Updates killer move list
  private GameState temp_gsk = new GameState();
  private Random random = new Random(19730318);

  public void UpdateKillerMoves(int ply, GameState gs, ArimaaMove move,
    boolean isCaptureMove) {

    // Update traditional killer moves
    if (!killer_move1[ply].equals(move) && !isCaptureMove && move.steps <= 2) {
      killer_move2[ply].copy(killer_move1[ply]);
      killer_move1[ply].copy(move);
    }

    // Update full turn killer moves
    if (gs.getStepsRemaining() == 4) {
      int count = 0;
      int cur_killer = random.nextInt(3);
      cur_killer = 0;
      // Setup the proper gamestate
      temp_gsk.copy(gs);

      while (count <= 4) {
        // Get hash move
        ArimaaMove hash_move = hash_table.ProbeHashMove(temp_gsk.
          getPositionHash());
        if (hash_move == null) {
          break;
        }

        // Add move to killers
        killer_turn[ply][cur_killer][count].copy(hash_move);

        // Update the current gamestate
        temp_gsk.play(hash_move, temp_gsk);
        if (temp_gsk.getStepsRemaining() == 4) {
          break;
        }

        count++;
      }

      // Record the length of killer move
      killer_turn_length[ply][cur_killer] = count;
    }
  }

  public String getPV(GameState gs) {

    String result = "";
    int pv_moves = 0;
    temp_gsk.copy(gs);

    while (true) {
      // Get hash move
      ArimaaMove hash_move = hash_table.ProbeHashMove(temp_gsk.
        getPositionHash());
      if (hash_move == null) {
        break;
      }

      // Output the move
      result += hash_move.toString() + " ";

      // Update the gamestate
      temp_gsk.play(hash_move, temp_gsk);

      // Cutoff if the move list repeats
      pv_moves++;
      if (pv_moves > 50) {
        break;
      }
    }

    return result;
  }

  private static String tests[] = {
    "12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
    "12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

    "2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",

  };

  public static void main(String args[]) {

    ArimaaEngine engine = new ArimaaEngine();
    engine.resetStats();
    LogFile.setMessageDisplay(true);

    for (String text : tests) {
      GameState position = new GameState(text);

      MoveInfo info = engine.genMove(position, 10000);
      System.out.println("*" + info.move_text + "*");
    }
    
    MyDB.close();

  }

}
