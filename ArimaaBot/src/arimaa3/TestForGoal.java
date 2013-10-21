package arimaa3;

import ai_util.*;

// this class takes a position and sees if a win is possible

public class TestForGoal extends ArimaaBaseClass {

  public TestForGoal() {
  }


  public static boolean use_debug = false;

  // Working variables for gen_moves
  private int stack_depth;
  private MoveList move_list_stack[] = new MoveList[15];
  private GameState gs_stack[] = new GameState[15];
  {
    for (int i = 0; i < gs_stack.length; i++) {
      gs_stack[i] = new GameState();
    }
    for (int i = 0; i < move_list_stack.length; i++) {
      move_list_stack[i] = new MoveList(1000);
    }
  }


  // Hash table for storing repeated positions
  private static long hash_mask = 0xFFFFF;
  private static long hash_table[] = new long[1048576];


  public String getStats() {
    String result = "";
    result += Util.ProbStats("Test HH", test_calls, test_hash_hits);
    return result;
  }

  public void resetStats() {
    test_calls = 0;
    test_hash_hits = 0;
  }

  // Statistics collection stuff
  static long test_calls = 0;
  static long test_hash_hits = 0;


  public boolean test(GameState game) {

    this.saved_goal_index = -1;
    this.saved_rabbit_index = -1;

    // Probe the hash table
    this.test_calls++;
    long hash_code = game.getPositionHash();
    int index = (int) (hash_code & hash_mask);
    // See if high bits match
    if ((hash_table[index] & ~hash_mask) == (hash_code & ~hash_mask)) {
      this.test_hash_hits++;
      boolean result = (hash_table[index] & 0x01L) == (0x01L) ? true : false;
      //    return result;
    }

    // Check if rabbit can win unassissted
    boolean result = can_rabbit_run(game, 4);
    if (result == false) {
      // Rabbit needs help
      result = test_goal_squares(game);
    }

    // Store result in hash table
    // result stored in LSB, 1==true, 0==false
    hash_table[index] = (result == true) ? hash_code | 0x01L :
      hash_code & 0xFFFFFFFFFFFFFFFEL;

    return result;
  }


  // If a win is possible, the location of the
  // winning rabbit and the goal are saved here
  private int saved_goal_index;
  private int saved_rabbit_index;

  public int getGoalIndex() {
    return saved_goal_index;
  }

  public int getRabbitIndex() {
    return saved_rabbit_index;
  }


  // test all 8 goals squares sequentially
  private boolean test_goal_squares(GameState game) {
    game.compute_tertiary_bitboards();
    int start_index = (game.player == 0) ? 56 : 0;
    for (int i = start_index; i < start_index + 8; i++) {
      boolean result = test_individual_goal_square(game, i, 4);
      if (result == true) {
        saved_goal_index = i; // Save the winning goal location
        return true;
      }
    }
    return false;
  }

  private boolean test_individual_goal_square(GameState game, int goal_index,
    int total_steps_available) {

    this.stack_depth = -1;

    // Test for impossible scoring patterns

    long enemy_bb = game.colour_bb[game.enemy]; // all enemy pieces
    long player_bb = game.colour_bb[game.player]; // all player pieces
    long empty_bb = game.empty_bb;
    long goal_bb = goal_pattern_bb[goal_index][0];
    long touch_goal_bb = goal_pattern_bb[goal_index][2];
    long rabbit_bb = game.piece_bb[game.player];
    long gmt_bb = goal_pattern_bb[goal_index][4];
    long player_nr_bb = player_bb & ~rabbit_bb;
    long touch_gmt_bb = goal_pattern_bb[goal_index][5];
    long three_step_bb = goal_pattern_bb[goal_index][8];

    boolean is_goal_empty = true;
    boolean is_gmt_empty = true;

    // If these patterns are true NO goal possible
    // three enemy piece in a row centred on goal square
    if ((goal_pattern_bb[goal_index][1] & enemy_bb) ==
        goal_pattern_bb[goal_index][1]) {
      return false;
    }

    // If enemy elephant on gmt or goal square, No goal possible
    if (((gmt_bb | goal_bb) & game.piece_bb[game.enemy + 10]) != 0) {
      return false;
    }

    // Compute minimum piece steps required for a goal
    // This can be wrong if dual purpose move is possible, but in that case
    // no goal is possible anyway.
    int piece_steps_required = 0; // min steps required to clear goal and gmt squares

    if ((goal_bb & enemy_bb) != 0) { // enemy on goal square
      is_goal_empty = false;
      piece_steps_required += 2; // To remove enemy
      if ((touch_goal_bb & player_nr_bb) == 0) { // no adj player piece
        piece_steps_required += 1; // To touch enemy piece
      }
    }

    if ((gmt_bb & enemy_bb) != 0) { // enemy piece on gmt square
      is_gmt_empty = false;
      piece_steps_required += 2; // To remove enemy;
      if ((touch_gmt_bb & player_nr_bb) == 0) { // no adj player piece
        piece_steps_required += 1;
      }
    }

    if ((goal_bb & player_nr_bb) != 0) { // player piece on goal square
      is_goal_empty = false;
      piece_steps_required += 1; // To remove player piece
      if ((touch_goal_bb & ~game.empty_bb) == touch_goal_bb) { // adj squares occupied
        piece_steps_required += 1; // To clear square for player piece
      }
    }

    if ((gmt_bb & player_nr_bb) != 0) { // player piece on gmt square
      is_gmt_empty = false;
      piece_steps_required += 1; // To remove player piece
      if ((touch_gmt_bb & ~game.empty_bb) == touch_gmt_bb) { // adj squares occupied
        piece_steps_required += 1; // To clear square for player piece
      }
    }

    // We know a rabbit can't win unassisted,
    // so at least one piece step is required
    if (piece_steps_required == 0) {
      piece_steps_required = 1;
    }

    // Any rabbit needs help to goal, since we already tested for unassisted wins
    // Compute minimum number of rabbit steps required for a goal
    int rabbit_steps_required = 99;
    if ((goal_pattern_bb[goal_index][7] & rabbit_bb) != 0) {
      rabbit_steps_required = 3;
      if ((goal_pattern_bb[goal_index][6] & rabbit_bb) != 0) {
        rabbit_steps_required = 2;
        if ((gmt_bb & rabbit_bb) != 0) {
          rabbit_steps_required = 1;
        }
      }
    }

    if (use_debug) {
      System.out.println(" " + goal_index + "Steps Avail:" +
        total_steps_available + " RReq: " +
        rabbit_steps_required + " PReq:" + piece_steps_required);
    }

    // Determine if there are enough steps available
    if (rabbit_steps_required + piece_steps_required > total_steps_available) {
      return false;
    }

    // There are enough steps, perform a detailed analysis
    boolean result = this.test_decide(game, goal_index, total_steps_available);
    return result;

  }


  // Try a two step move
  // Play out all sample moves
  private GenSteps gen_steps = new GenSteps();


  private GameState temp_gs1 = new GameState();
  private ArimaaMove temp_move1 = new ArimaaMove();

  // Rabbit on touch_gmt or on three_step
  // One piece step to assist
  private boolean test_2R1P_3R1P(GameState game, int goal_index,
    int total_steps_available) {

    if (use_debug) {
      System.out.println("test_2R1P_3R1P " + goal_index + " " +
        total_steps_available + "\n" + game);
    }

    int player = game.player;
    int piece_steps_available = 1;

    long enemy_bb = game.colour_bb[game.enemy]; // all enemy pieces
    long player_bb = game.colour_bb[game.player]; // all player pieces
    long empty_bb = game.empty_bb;
    long goal_bb = goal_pattern_bb[goal_index][0];
    long touch_goal_bb = goal_pattern_bb[goal_index][2];
    long rabbit_bb = game.piece_bb[game.player];
    long gmt_bb = goal_pattern_bb[goal_index][4];
    long player_nr_bb = player_bb & ~rabbit_bb;
    long enemy_nr_bb = game.stronger_enemy_bb[player];
    long touch_gmt_bb = goal_pattern_bb[goal_index][5];
    long three_step_bb = goal_pattern_bb[goal_index][8]; // Exactly three steps

    assert (total_steps_available >= 3);

    // If enemy on path
    if (((goal_bb | gmt_bb) & enemy_bb) != 0) {
      return false;
    }

    // If both goal and gmt occupied
    if (((goal_bb | gmt_bb) & empty_bb) == 0) {
      return false;
    }

    stack_depth++;

    // Case 1) Try moving piece

    // if total_steps_available == 3
    long path_bb = goal_bb | gmt_bb;
    long candidate_bb = touch_gmt_bb & rabbit_bb;

    if (total_steps_available == 4) {
      path_bb |= touch_gmt_bb;
      candidate_bb |= three_step_bb & rabbit_bb;
    }
    long frozen_rabbit_bb = candidate_bb & game.frozen_pieces_bb;

    // Try and support path
    long dest_bb = touching_bb(path_bb);

    // If rabbit is frozen, try and unfreeze
    if (frozen_rabbit_bb != 0) {
      dest_bb |= touching_bb(frozen_rabbit_bb) & empty_bb & ~gmt_bb;
    }

    // Check for freezer due to rabbit removing protection
    //  r m e - r
    //  e R R - -
    //  - - D m r
    long frozen_helper_bb = touching_bb(candidate_bb) & player_bb;
    if (frozen_helper_bb != 0) {
      dest_bb |= touching_bb(frozen_helper_bb) & empty_bb & ~gmt_bb;
    }

    // If piece on goal|gmt, must remove
    long start_bb = FULL_BB;
    if (((goal_bb | gmt_bb) & empty_bb) != (goal_bb | gmt_bb)) {
      start_bb = goal_bb | gmt_bb;
    }

    if (use_debug) {
      System.out.println("Case 1) Try moving piece");
      print_bitboard(start_bb, "start_bb");
      print_bitboard(dest_bb, "dest_bb");
    }

    move_list_stack[stack_depth].clear();
    game.genSlideMoves(move_list_stack[stack_depth], start_bb, dest_bb);
    for (ArimaaMove move : move_list_stack[stack_depth]) {
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, game);
      if (test_decide(new_position,
        goal_index, total_steps_available - move.steps)) {
        return true;
      }
    }

    // Case 2) Try moving a rabbit towards the goal
    start_bb = candidate_bb;
    dest_bb = path_bb;

    if (use_debug) {
      System.out.println("Case 2) Try moving rabbit towards goal");
      print_bitboard(start_bb, "start_bb");
      print_bitboard(dest_bb, "dest_bb");
    }

    move_list_stack[stack_depth].clear();
    gen_steps.genOneStep(game, move_list_stack[stack_depth], start_bb, dest_bb);
    for (ArimaaMove move : move_list_stack[stack_depth]) {
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, game);
      if (test_decide(new_position,
        goal_index, total_steps_available - move.steps)) {
        return true;
      }
    }

    stack_depth--;
    return false;
  }


  // Rabbit on touch_gmt, 2 piece steps to assist
  private boolean test_2R2P(GameState game, int goal_index,
    int total_steps_available) {

    if (use_debug) {
      System.out.println("test_2R2P " + goal_index + " " +
        total_steps_available + "\n" + game);
    }

    int player = game.player;
    int piece_steps_available = 1;

    long enemy_bb = game.colour_bb[game.enemy]; // all enemy pieces
    long player_bb = game.colour_bb[game.player]; // all player pieces
    long empty_bb = game.empty_bb;
    long goal_bb = goal_pattern_bb[goal_index][0];
    long touch_goal_bb = goal_pattern_bb[goal_index][2];
    long rabbit_bb = game.piece_bb[game.player];
    long gmt_bb = goal_pattern_bb[goal_index][4];
    long player_nr_bb = player_bb & ~rabbit_bb;
    long enemy_nr_bb = game.stronger_enemy_bb[player];
    long touch_gmt_bb = goal_pattern_bb[goal_index][5];
    long three_step_bb = goal_pattern_bb[goal_index][8]; // Exactly three steps

    assert (total_steps_available == 4);
    assert ((rabbit_bb & touch_gmt_bb) != 0); // Rabbit on Touch GMT

    // TODO fill in test cases

    // Case 1) Try using only one piece step
    if (test_2R1P_3R1P(game, goal_index, total_steps_available)) {
      return true;
    }

    stack_depth++;

    // Case 2) Try using both piece steps
    // These all have to be piece moves. Trap square interactions are nasty



    // Case 1) Try remove protection plays
    if (test_remove_protection(game, goal_index, total_steps_available)) {
      return true;
    }

    // Case 2) Try unfreezing a rabbit by putting player piece beside rabbit
    // Case 3) Try Unfreezing GMT square by putting player piece beside GMT
    long start_bb = FULL_BB;
    long dest_bb = touching_bb(touch_gmt_bb & rabbit_bb & game.frozen_pieces_bb);
    dest_bb |= touch_gmt_bb;
    move_list_stack[stack_depth].clear();
    gen_steps.genTwoStep(game, move_list_stack[stack_depth], start_bb, dest_bb);
    for (ArimaaMove move : move_list_stack[stack_depth]) {
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, game);
      if (test_decide(new_position,
        goal_index, total_steps_available - move.steps)) {
        return true;
      }
    }

    // Case 4) If player on Goal, move it
    // Case 5) If player on GMT, move it
    start_bb = (goal_bb | gmt_bb) & player_bb;
    dest_bb = FULL_BB;

    if (start_bb != 0) {

      move_list_stack[stack_depth].clear();
      gen_steps.genTwoStep(game, move_list_stack[stack_depth], start_bb,
        dest_bb);
      for (ArimaaMove move : move_list_stack[stack_depth]) {
        GameState new_position = gs_stack[stack_depth];
        new_position.playFull(move, game);
        if (test_decide(new_position,
          goal_index, total_steps_available - move.steps)) {
          return true;
        }
      }

    }

    // Case 6) Pull enemy piece off goal
    // Case 7) Pull enemy piece off gmt
    // Case 8) Pull enemy piece off gmt_freezer
    // Case 9) Pull enemy piece off rabbit freezer

    // NEW CASE) Pull enemy protection piece away from trap
    // This can work for case 8 or case 9

    long e_start_bb = 0;

    // Case 6 and 7
    e_start_bb |= (goal_bb | gmt_bb) & enemy_bb;
    // Case 8 and 9
    long e_possible_bb = (touch_gmt_bb & enemy_nr_bb);
    e_possible_bb |=
      touching_bb(touch_gmt_bb & rabbit_bb & game.frozen_pieces_bb) &
      enemy_bb;
    e_start_bb |= e_possible_bb;

    // New case
    // If enemy piece is vulnerable on trap
    // Try and remove its support
    for (int i = 0; i <= 3; i++) {
      if ((e_possible_bb & TRAP[i]) != 0) {
        long etest_bb = TOUCH_TRAP[i] & enemy_bb;
        if (atMostOneBitSet(etest_bb)) {
          e_start_bb |= etest_bb;
        }
      }
    }

    long e_dest_bb = FULL_BB & ~goal_bb & ~gmt_bb;
    dest_bb = FULL_BB & ~goal_bb & ~gmt_bb;
    start_bb = FULL_BB;

    if (e_start_bb != 0) {
      move_list_stack[stack_depth].clear();
      game.genDragMoves(move_list_stack[stack_depth], start_bb, dest_bb,
        e_start_bb,
        e_dest_bb);
      for (ArimaaMove move : move_list_stack[stack_depth]) {
        GameState new_position = gs_stack[stack_depth];
        new_position.playFull(move, game);
        if (test_decide(new_position,
          goal_index, total_steps_available - move.steps)) {
          return true;
        }
      }
    }

    // Support rabbit, so another piece can move away!!
    dest_bb = touching_bb(touch_gmt_bb & rabbit_bb) & empty_bb;
    start_bb = FULL_BB & ~touch_gmt_bb;
    move_list_stack[stack_depth].clear();
    game.genSlideMoves(move_list_stack[stack_depth], start_bb, dest_bb);
    for (ArimaaMove move : move_list_stack[stack_depth]) {
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, game);
      if (test_decide(new_position,
        goal_index, total_steps_available - move.steps)) {
        return true;
      }
    }

    // Support piece on trap, so rabbit can move away!!
    long danger_bb = touching_bb(touch_gmt_bb & rabbit_bb) & TRAP_SQUARES &
      player_bb;
    dest_bb = touching_bb(danger_bb);
    start_bb = FULL_BB & ~danger_bb;
    move_list_stack[stack_depth].clear();
    game.genSlideMoves(move_list_stack[stack_depth], start_bb, dest_bb);
    for (ArimaaMove move : move_list_stack[stack_depth]) {
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, game);
      if (test_decide(new_position,
        goal_index, total_steps_available - move.steps)) {
        return true;
      }
    }

    // Case 2) Try moving a rabbit towards the goal
    start_bb = touch_gmt_bb & rabbit_bb & ~game.frozen_pieces_bb;
    dest_bb = gmt_bb;
    move_list_stack[stack_depth].clear();
    gen_steps.genOneStep(game, move_list_stack[stack_depth], start_bb, dest_bb);
    for (ArimaaMove move : move_list_stack[stack_depth]) {
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, game);
      if (test_decide(new_position,
        goal_index, total_steps_available - move.steps)) {
        return true;
      }
    }

    stack_depth--;
    return false;
  }


  /**
   * Decides which function to call based on provided gamestate
   * @param game GameState
   * @param goal_index int
   * @param total_steps_available int
   * @return boolean
   */
  private boolean test_decide(GameState game, int goal_index, int steps) {
    long enemy_bb = game.colour_bb[game.enemy]; // all enemy pieces
    long player_bb = game.colour_bb[game.player]; // all player pieces
    long empty_bb = game.empty_bb;
    long goal_bb = goal_pattern_bb[goal_index][0];
    long rabbit_bb = game.piece_bb[game.player];
    long gmt_bb = goal_pattern_bb[goal_index][4];
    long touch_gmt_bb = goal_pattern_bb[goal_index][5];
    long three_step_bb = goal_pattern_bb[goal_index][8];

    if ((goal_bb & empty_bb) == 0 && (gmt_bb & rabbit_bb) != 0) {
      return test_GmtRabbit_GoalNotEmpty(game, goal_index, steps);
    } else if ((goal_bb & empty_bb) != 0 && (gmt_bb & rabbit_bb) != 0) {
      return test_GmtRabbit_GoalEmpty(game, goal_index, steps);
    }
    // If we get here, no rabbit on gmt
    else if ((touch_gmt_bb & rabbit_bb) != 0 && (steps == 3)) {
      return test_2R1P_3R1P(game, goal_index, steps);
    } else if ((touch_gmt_bb & rabbit_bb) == 0 &&
      (three_step_bb & rabbit_bb) != 0 && (steps == 4)) {
      return test_2R1P_3R1P(game, goal_index, steps);
    } else if ((touch_gmt_bb & rabbit_bb) != 0 && (steps == 4)) {
      return test_2R2P(game, goal_index, steps);
    } else {
      return test_4RP0(game, goal_index, steps);
    }

  }

  private boolean test_4RP0(GameState game, int goal_index,
    int total_steps_available) {

    if (use_debug) {
      System.out.println("test_4RP0 " + goal_index + " " +
        total_steps_available + "\n" + game);
    }

    return can_rabbit_run(game, total_steps_available);
  }

  private boolean test_GmtRabbit_GoalNotEmpty(GameState game, int goal_index,
    int total_steps_available) {

    if (use_debug) {
      System.out.println("test_GmtRabbit_GoalNotEmpty " + goal_index + " " +
        total_steps_available + "\n" + game);
    }

    int player = game.player;
    int piece_steps_available = total_steps_available - 1;

    long enemy_bb = game.colour_bb[game.enemy]; // all enemy pieces
    long player_bb = game.colour_bb[game.player]; // all player pieces
    long empty_bb = game.empty_bb;
    long goal_bb = goal_pattern_bb[goal_index][0];
    long touch_goal_bb = goal_pattern_bb[goal_index][2];
    long rabbit_bb = game.piece_bb[game.player];
    long gmt_bb = goal_pattern_bb[goal_index][4];
    long player_nr_bb = player_bb & ~rabbit_bb;
    long enemy_nr_bb = game.stronger_enemy_bb[player];
    long touch_gmt_bb = goal_pattern_bb[goal_index][5];
    long three_step_bb = goal_pattern_bb[goal_index][8];

    stack_depth++;

    assert ((goal_bb & empty_bb) == 0); // Goal NOT empty
    assert ((rabbit_bb & gmt_bb) != 0); // Rabbit on GMT

    // Not enough steps to goal!
    if (total_steps_available < 2) {
      return false;
    }

    if (piece_steps_available == 1) {
      // Only Case!! Slide player piece off goal
      if ((goal_bb & player_bb) != 0) {
        if ((touch_goal_bb & empty_bb) != 0) {
          // Test if gmt rabbit is frozen
          if ((touch_gmt_bb & enemy_nr_bb) == 0 ||
            (touch_gmt_bb & player_bb) != 0) {
            return debug(true);
          }
        }
      }
    }

    if (piece_steps_available == 2) {
      // Case A: Unassisted Slide
      // Case B: Assisted Slide
      // Case C: Push Out

      if ((goal_bb & player_bb) != 0) {

        long escape_bb = touch_goal_bb;
        while (escape_bb != 0) {
          long lsb_bb = escape_bb & -escape_bb;
          escape_bb ^= lsb_bb; // Remove the piece

          // Case 1) Escape_bb is empty
          if ((lsb_bb & empty_bb) != 0) {

            // Test if gmt rabbit is frozen
            if ((touch_gmt_bb & enemy_nr_bb) == 0 ||
              (touch_gmt_bb & player_bb) != 0) {
              return debug(true);
            }

            // One step left to free rabbit
            // Rabbit still frozen
            // Make new gamestate and send on to empty goal tester
            temp_move1.clear();
            temp_move1.piece_bb[game.getPieceType(goal_bb)] ^= goal_bb |
              lsb_bb;
            temp_move1.steps = 1;
            temp_gs1.playFull(temp_move1, game);
            if (test_GmtRabbit_GoalEmpty(temp_gs1, goal_index,
              total_steps_available - 1)) {
              return true;
            }
          }

          // Case 2) Escape_bb contains player
          if ((lsb_bb & player_bb) != 0) {
            long dest_bb = touching_bb(lsb_bb) & ~goal_bb & empty_bb;

            // 1a) Slide out and slide goal (lsb2 == empty) 2 steps
            if (dest_bb != 0) {
              // Test if gmt rabbit is frozen
              if ((dest_bb & touch_gmt_bb) != 0 ||
                (touch_gmt_bb & enemy_nr_bb) == 0 ||
                (touch_gmt_bb & player_bb) != 0) {
                return debug(true);
              }
            }
          }
          // Case 3 Escape_bb contains enemy
          if ((lsb_bb & enemy_bb) != 0) {
            assert (piece_steps_available >= 2);
            long dest_bb = touching_bb(lsb_bb) & ~goal_bb & empty_bb;
            int pt = game.getPieceType(lsb_bb);

            // 2a) Push out (lsb2==empty) 2 steps
            if (dest_bb != 0) {
              // Check if goal piece is strong enough to push out
              if ((game.stronger_enemy_bb[pt] & goal_bb) != 0) {
                // Test if gmt rabbit is frozen

                // Enemy piece could be pushed onto touch_gmt square!
                if ((touch_gmt_bb & enemy_nr_bb) == 0) {
                  if ((lsb_bb & enemy_nr_bb) == 0 ||
                    (dest_bb & ~touch_gmt_bb) != 0) {
                    return debug(true);
                  }
                }
                if ((touch_gmt_bb & player_bb) != 0) {
                  return debug(true);
                }

              }

            }
          }
        }
      }
      if ((goal_bb & enemy_bb) != 0) {

        // Only Case: Pull enemy piece off goal
        int pt = game.getPieceType(goal_bb);
        long escape_bb = touch_goal_bb & player_bb;
        while (escape_bb != 0) {
          long lsb_bb = escape_bb & -escape_bb;
          escape_bb ^= lsb_bb; // Remove the piece

          // Check if player piece is strong enough to drag
          // Check if player piece is frozen
          if ((game.stronger_enemy_bb[pt] & lsb_bb) != 0 &&
            (lsb_bb & game.frozen_pieces_bb) == 0) {

            long dest_bb = touching_bb(lsb_bb) & ~goal_bb & empty_bb;

            if (dest_bb != 0) {
              // Test if gmt rabbit is frozen
              if ((dest_bb & touch_gmt_bb) != 0 ||
                (touch_gmt_bb & enemy_nr_bb) == 0 ||
                (touch_gmt_bb & player_bb) != 0) {
                return debug(true);
              }
            }
          }
        }
      }
    }

    if (piece_steps_available == 3) {
      // Player piece on goal square
      if ((goal_bb & player_bb) != 0) {

        long escape_bb = touch_goal_bb;
        while (escape_bb != 0) {
          long lsb_bb = escape_bb & -escape_bb;
          escape_bb ^= lsb_bb; // Remove the piece

          // Case 1) Escape_bb is empty
          if ((lsb_bb & empty_bb) != 0) {
            assert (piece_steps_available >= 1);

            // Test if gmt rabbit is frozen
            if ((touch_gmt_bb & enemy_nr_bb) == 0 ||
              (touch_gmt_bb & player_bb) != 0) {
              return debug(true);
            }

            // Two steps left to free rabbit
            // Rabbit still frozen
            // Make new gamestate and send on to empty goal tester
            temp_move1.clear();
            temp_move1.piece_bb[game.getPieceType(goal_bb)] ^= goal_bb |
              lsb_bb;
            temp_move1.steps = 1;
            temp_gs1.playFull(temp_move1, game);
            if (test_GmtRabbit_GoalEmpty(temp_gs1, goal_index,
              total_steps_available - 1)) {
              return true;
            }
          }

          // Case 2) Escape_bb contains player
          if ((lsb_bb & player_bb) != 0) {
            assert (piece_steps_available >= 2);
            long dest_bb = touching_bb(lsb_bb) & ~goal_bb;
            while (dest_bb != 0) {
              long lsb2_bb = dest_bb & -dest_bb;
              dest_bb ^= lsb2_bb; // Remove the piece

              // 1a) Slide out and slide goal (lsb2 == empty) 2 steps
              if ((lsb2_bb & empty_bb) != 0) {
                // Test if gmt rabbit is frozen
                if ((lsb2_bb & touch_gmt_bb) != 0 ||
                  (touch_gmt_bb & enemy_nr_bb) == 0 ||
                  (touch_gmt_bb & player_bb) != 0) {
                  return debug(true);
                }

                // One step left to free rabbit
                // Goal is clear, but rabbit still frozen
                // Make new gamestate and send on to empty goal tester
                temp_move1.clear();
                temp_move1.piece_bb[game.getPieceType(goal_bb)] ^= goal_bb |
                  lsb_bb;
                temp_move1.piece_bb[game.getPieceType(lsb_bb)] ^= lsb_bb |
                  lsb2_bb;
                temp_move1.steps = 2;
                temp_gs1.playFull(temp_move1, game);
                if (test_GmtRabbit_GoalEmpty(temp_gs1, goal_index,
                  total_steps_available - 2)) {
                  return true;
                }

              }

              // 1b) Push Out and slide goal (lsb2 == enemy) 3 steps
              if ((lsb2_bb & enemy_bb) != 0) {

                // Check if lsb can push out
                int pt = game.getPieceType(lsb2_bb);
                if ((game.stronger_enemy_bb[pt] & lsb_bb) != 0) {
                  long far_dest_bb = touching_bb(lsb2_bb) & empty_bb;
                  if (far_dest_bb != 0) {
                    // Test if gmt rabbit is frozen
                    if ((lsb2_bb & touch_gmt_bb) != 0 ||
                      (touch_gmt_bb & enemy_nr_bb) == 0 ||
                      (touch_gmt_bb & player_bb) != 0) {
                      return debug(true);
                    }
                  }
                }
                // All steps used up
              }
              // 1c) Vacate with slide and slide and slide goal (lsb2 == player) 3 steps
              if ((lsb2_bb & player_bb) != 0) {
                // Check for final flight square
                long far_dest_bb = touching_bb(lsb2_bb,
                  game.getPieceType(lsb2_bb)) & empty_bb;
                if (far_dest_bb != 0) {
                  // Test if GMT rabbit is frozen
                  if ((touch_gmt_bb & enemy_nr_bb) == 0 ||
                    (touch_gmt_bb & player_bb) != 0) {
                    return debug(true);
                  }
                }
              }

            }
          }

          // Case 3 Escape_bb contains enemy
          if ((lsb_bb & enemy_bb) != 0) {
            assert (piece_steps_available >= 2);
            long dest_bb = touching_bb(lsb_bb) & ~goal_bb;
            int pt = game.getPieceType(lsb_bb);
            while (dest_bb != 0) {
              long lsb2_bb = dest_bb & -dest_bb;
              dest_bb ^= lsb2_bb; // Remove the piece

              // 2a) Push out (lsb2==empty) 2 steps
              if ((lsb2_bb & empty_bb) != 0) {
                // Check if goal piece is strong enough to push out
                if ((game.stronger_enemy_bb[pt] & goal_bb) != 0) {
                  // Test if gmt rabbit is frozen

                  // Enemy piece could be pushed onto touch_gmt square!
                  if ((touch_gmt_bb & enemy_nr_bb) == 0) {
                    if ((lsb_bb & enemy_nr_bb) == 0 ||
                      (lsb2_bb & touch_gmt_bb) == 0) {
                      return debug(true);
                    }
                  }
                  if ((touch_gmt_bb & player_bb) != 0) {
                    return debug(true);
                  }

                  // Rabbit is frozen but one step remains to try and free it
                  // Make new gamestate and send on to empty goal tester
                  temp_move1.clear();
                  temp_move1.piece_bb[game.getPieceType(goal_bb)] ^=
                    goal_bb |
                    lsb_bb;
                  temp_move1.piece_bb[game.getPieceType(lsb_bb)] ^= lsb_bb |
                    lsb2_bb;
                  temp_move1.steps = 2;
                  temp_gs1.playFull(temp_move1, game);
                  if (test_GmtRabbit_GoalEmpty(temp_gs1, goal_index,
                    total_steps_available - 2)) {
                    return true;
                  }

                }

              }

              if ((lsb2_bb & player_bb) != 0) {

                // 2b) Vacate escape with pull, and slide goal (lsb2==player) 3steps
                // 2c) Vacate push dest with slide, push out goal (lsb2==player) 3 steps

                // Check if piece is not frozen
                if ((lsb2_bb & game.frozen_pieces_bb) == 0) {
                  // Check if piece has empty square to move to
                  if ((touching_bb(lsb2_bb, game.getPieceType(lsb2_bb)) &
                    empty_bb) != 0) {
                    // Check if one of the player pieces is strong enough to drag
                    if ((game.stronger_enemy_bb[pt] & (goal_bb | lsb2_bb)) !=
                      0) {

                      // Test if gmt rabbit is frozen

                      // Enemy piece could be pushed onto touch_gmt square!
                      if ((touch_gmt_bb & enemy_nr_bb) == 0) {
                        if ((lsb_bb & enemy_nr_bb) == 0 ||
                          (lsb2_bb & touch_gmt_bb) == 0) {
                          return debug(true);
                        }
                      }

                      // Player piece on lsb2_bb leaves touch_gmt square!
                      if (((touch_gmt_bb & ~lsb2_bb) & player_bb) != 0) {
                        return debug(true);
                      }

                    }
                  }
                }

              }

              // NOTE: (lsb2==enemy) Not possible to clear goal

            }

          }

        }

      }

      // Enemy piece on goal square
      if ((goal_bb & enemy_bb) != 0) {
        int pt = game.getPieceType(goal_bb);

        long escape_bb = touch_goal_bb;
        while (escape_bb != 0) {
          long lsb_bb = escape_bb & -escape_bb;
          escape_bb ^= lsb_bb; // Remove the piece

          // Escape square is empty
          if ((lsb_bb & empty_bb) != 0) {
            assert (piece_steps_available >= 3);
            long dest_bb = touching_bb(lsb_bb) & ~goal_bb;

            while (dest_bb != 0) {
              long lsb2_bb = dest_bb & -dest_bb;
              dest_bb ^= lsb2_bb; // Remove the piece

              // then slide and pull back 3 steps (2 options for slide back!)
              // lsb2 is stronger unfrozen player
              if ((game.stronger_enemy_bb[pt] & lsb2_bb) != 0 &&
                (lsb2_bb & game.frozen_pieces_bb) == 0) {

                // Test for lsb refreeze
                int pt2 = game.getPieceType(lsb2_bb);
                if ((game.stronger_enemy_bb[pt2] & touching_bb(lsb_bb)) ==
                  0) {
                  // Check if gmt rabbit is frozen
                  if ((touch_gmt_bb & enemy_nr_bb) == 0 ||
                    (touch_gmt_bb & player_bb) != 0) {
                    return debug(true);
                  }
                  // Slide back can be to different square!
                  if ((touching_bb(lsb_bb) & empty_bb & touch_gmt_bb) != 0) {
                    return debug(true);
                  }

                }
              }
            }
          }

          // Player piece on escape square
          if ((lsb_bb & player_bb) != 0) {

            // Check if player piece is strong enough to drag
            // Check if player piece is frozen
            if ((game.stronger_enemy_bb[pt] & lsb_bb) != 0 &&
              (lsb_bb & game.frozen_pieces_bb) == 0) {
              assert (piece_steps_available >= 2);
              long dest_bb = touching_bb(lsb_bb) & ~goal_bb;

              while (dest_bb != 0) {
                long lsb2_bb = dest_bb & -dest_bb;
                dest_bb ^= lsb2_bb; // Remove the piece

                // lsb2 == player  lsb2 slide away, lsb pull  3steps, check for lsb refreeze
                if ((lsb2_bb & player_bb) != 0) {
                  if ((touching_bb(lsb2_bb, game.getPieceType(lsb2_bb)) &
                    empty_bb) != 0) {
                    // Test for lsb refreeze
                    int pt2 = game.getPieceType(lsb_bb);
                    if ((game.stronger_enemy_bb[pt2] & touching_bb(lsb_bb)) ==
                      0) {
                      // Test if gmt rabbit is frozen
                      if ((touch_gmt_bb & enemy_nr_bb) == 0 ||
                        (touch_gmt_bb & player_bb) != 0) {
                        return debug(true);
                      }
                    }
                  }
                }

                // lsb2 == empty   lsb pull 2 steps, 1 step to free rabbit
                if ((lsb2_bb & empty_bb) != 0) {
                  // Test if gmt rabbit is frozen
                  if ((lsb2_bb & touch_gmt_bb) != 0 ||
                    (touch_gmt_bb & enemy_nr_bb) == 0 ||
                    (touch_gmt_bb & player_bb) != 0) {
                    return debug(true);
                  }

                  // Rabbit still frozen one step left to free rabbit
                  // Make new gamestate and send on to empty goal tester
                  temp_move1.clear();
                  temp_move1.piece_bb[game.getPieceType(goal_bb)] ^=
                    goal_bb |
                    lsb_bb;
                  temp_move1.piece_bb[game.getPieceType(lsb_bb)] ^= lsb_bb |
                    lsb2_bb;
                  temp_move1.steps = 2;
                  temp_gs1.playFull(temp_move1, game);
                  if (test_GmtRabbit_GoalEmpty(temp_gs1, goal_index,
                    total_steps_available - 2)) {
                    return true;
                  }

                }
              }

              // Case B) if other escape square empty, push and retreat 3 steps
              long other_escape_bb = touch_goal_bb & ~lsb_bb;
              if ((other_escape_bb & empty_bb) != 0) {
                // Test if GMT rabbit is frozen
                if ((touch_gmt_bb & enemy_nr_bb) == 0 ||
                  (touch_gmt_bb & player_bb) != 0) {
                  return debug(true);
                }

              }
            }

          }

          // NOTE: (lsb==enemy) Not possible to clear goal
        }
      }
    }

    stack_depth--;
    return false;
  }


  // Trys all remove protection moves at enemy traps
  private boolean test_remove_protection(GameState game, int goal_index,
    int total_steps_available) {

    if (use_debug) {
      System.out.println("test_GmtRabbit_GoalEmpty " + goal_index + " " +
        total_steps_available + "\n" + game);
    }

    int piece_steps_available = total_steps_available - 1;

    long enemy_bb = game.colour_bb[game.enemy]; // all enemy pieces
    long player_bb = game.colour_bb[game.player]; // all player pieces
    long empty_bb = game.empty_bb;
    long goal_bb = goal_pattern_bb[goal_index][0];
    long touch_goal_bb = goal_pattern_bb[goal_index][2];
    long rabbit_bb = game.piece_bb[game.player];
    long gmt_bb = goal_pattern_bb[goal_index][4];
    long player_nr_bb = player_bb & ~rabbit_bb;
    long touch_gmt_bb = goal_pattern_bb[goal_index][5];
    long three_step_bb = goal_pattern_bb[goal_index][8];

    stack_depth++;

    // Try remove protection captures for both enemy traps
    // use TRAP[game.enemy] to access


    for (int i = game.enemy; i <= 3; i += 2) {

      // Check for player piece
      if ((TRAP[i] & player_bb) != 0 &&
          atMostOneBitSet(TOUCH_TRAP[i] & player_bb)) {

        // Slide protecting piece away
        long start_bb = TOUCH_TRAP[i] & player_bb;
        long dest_bb = FULL_BB & ~start_bb;
        move_list_stack[stack_depth].clear();
        game.genSlideMoves(move_list_stack[stack_depth],
          start_bb, dest_bb);

        for (ArimaaMove move : move_list_stack[stack_depth]) {
          GameState new_position = gs_stack[stack_depth];
          new_position.playFull(move, game);
          if (test_decide(new_position,
            goal_index, total_steps_available - move.steps)) {
            return true;
          }
        }
      }

      // Check for enemy piece
      if ((TRAP[i] & enemy_bb) != 0 &&
          atMostOneBitSet(TOUCH_TRAP[i] & enemy_bb)) {

        // Slide protecting piece away
        long estart_bb = TOUCH_TRAP[i] & enemy_bb;
        long edest_bb = FULL_BB & ~estart_bb;
        move_list_stack[stack_depth].clear();
        game.genDragMoves(move_list_stack[stack_depth],
          FULL_BB, FULL_BB, estart_bb, edest_bb);

        for (ArimaaMove move : move_list_stack[stack_depth]) {
          GameState new_position = gs_stack[stack_depth];
          new_position.playFull(move, game);
          if (test_decide(new_position,
            goal_index, total_steps_available - move.steps)) {
            return true;
          }
        }
      }

    }

    stack_depth--;
    return false;
  }

  /**
   * Tests for goals with rabbit on gmt and goal square empty
   * Currently only handles <=3 steps
   * TODO add 4 step case
   * @param game GameState
   * @param goal_index int
   * @param total_steps_available int
   * @return boolean
   */

  private boolean test_GmtRabbit_GoalEmpty(GameState game, int goal_index,
    int total_steps_available) {

    if (use_debug) {
      System.out.println("test_GmtRabbit_GoalEmpty " + goal_index + " " +
        total_steps_available + "\n" + game);
    }

    int piece_steps_available = total_steps_available - 1;

    long enemy_bb = game.colour_bb[game.enemy]; // all enemy pieces
    long player_bb = game.colour_bb[game.player]; // all player pieces
    long empty_bb = game.empty_bb;
    long goal_bb = goal_pattern_bb[goal_index][0];
    long touch_goal_bb = goal_pattern_bb[goal_index][2];
    long rabbit_bb = game.piece_bb[game.player];
    long gmt_bb = goal_pattern_bb[goal_index][4];
    long player_nr_bb = player_bb & ~rabbit_bb;
    long touch_gmt_bb = goal_pattern_bb[goal_index][5];
    long three_step_bb = goal_pattern_bb[goal_index][8];

    assert ((goal_bb & empty_bb) != 0); // Goal empty
    assert ((rabbit_bb & gmt_bb) != 0); // Rabbit on GMT
    assert (total_steps_available >= 1);

    stack_depth++;

    // Check if gmt rabbit can goal on its own
    if ((gmt_bb & game.frozen_pieces_bb) == 0) {
      return debug(true);
    }

    if (piece_steps_available >= 1) {
      // Rabbit is frozen on gmt, try one step support slide
      long support_bb = touching_bb(touch_gmt_bb & empty_bb) & ~gmt_bb;
      if ((~game.frozen_pieces_bb & player_bb & support_bb) != 0) {
        return debug(true);
      }
    }

    if (piece_steps_available >= 2) {

      // Try remove protection plays
      if (test_remove_protection(game, goal_index, total_steps_available)) {
        return true;
      }

      // Rabbit is frozen on the gmt, try to remove freezer
      // OR push in to unfreeze
      long gmt_freeze_bb = touch_gmt_bb &
        game.stronger_enemy_bb[game.player];

      if (use_debug) {
        print_bitboard(gmt_freeze_bb, "GMT_freeze");
      }
      boolean only_one_freezer = atMostOneBitSet(gmt_freeze_bb);

      // Determine targets for dragging
      // Any epiece touching gmt is a target, since any push will do
      long target_bb = touch_gmt_bb & enemy_bb;

      // Check if remove protection capture is possible
      // Even if two freezers, this works, remove the freezer and occupy the square
      for (int i = 0; i <= 3; i++) {
        if ((gmt_freeze_bb & TRAP[i]) != 0) {
          long etest_bb = TOUCH_TRAP[i] & enemy_bb;
          if (atMostOneBitSet(etest_bb)) {
            target_bb |= etest_bb;
          }
        }
      }

      if (use_debug) {
        print_bitboard(target_bb, "Target");
      }
      // If only_one_freezer then pull moves work
      // Push moves will always work

      while (target_bb != 0) {
        long lsb_bb = target_bb & -target_bb;
        target_bb ^= lsb_bb; // Remove the piece

        int pt = game.getPieceType(lsb_bb);

        // Need to actually move the piece, trap square interations are too complex
        if (piece_steps_available >= 3) {
          // Slide in a stronger piece, to attempt a push/pull
          long dest_bb = touching_bb(lsb_bb) & empty_bb;
          long start_bb = touching_bb(dest_bb) & game.stronger_enemy_bb[pt];
          move_list_stack[stack_depth].clear();
          gen_steps.genOneStep(game, move_list_stack[stack_depth],
            start_bb, dest_bb);

          for (ArimaaMove move : move_list_stack[stack_depth]) {
            GameState new_position = gs_stack[stack_depth];
            new_position.playFull(move, game);
            if (test_GmtRabbit_GoalEmpty(new_position,
              goal_index, total_steps_available - move.steps)) {
              return true;
            }
          }

          // Frozen stronger piece already touching
          // Slide a support piece to unfreeze it
          long stronger_bb = game.stronger_enemy_bb[pt] &
            touching_bb(lsb_bb);

          dest_bb = touching_bb(stronger_bb & game.frozen_pieces_bb) &
            empty_bb & ~goal_bb;
          start_bb = touching_bb(dest_bb) & player_bb &
            ~game.frozen_pieces_bb;
          move_list_stack[stack_depth].clear();
          gen_steps.genOneStep(game, move_list_stack[stack_depth],
            start_bb, dest_bb);

          for (ArimaaMove move : move_list_stack[stack_depth]) {
            GameState new_position = gs_stack[stack_depth];
            new_position.playFull(move, game);
            int new_steps_available = total_steps_available - move.steps;

            boolean result = test_GmtRabbit_GoalEmpty(new_position,
              goal_index,
              new_steps_available);
            if (result == true) {
              return true;
            }
          }

          // Stronger piece already touching (freeze status could change!)
          // Vacate pull dest (if only_one_freezer)
          if (only_one_freezer &&
            (touching_bb(stronger_bb & ~game.frozen_pieces_bb) & empty_bb & ~goal_bb) == 0) {
            start_bb = touching_bb(stronger_bb) & player_bb;
            dest_bb = touching_bb(start_bb) & empty_bb & ~goal_bb;

            if (use_debug) {
              System.out.println("Vacate pull dest");
              print_bitboard(start_bb, "Start_bb");
              print_bitboard(dest_bb, "dest_bb");
            }

            move_list_stack[stack_depth].clear();
            game.genSlideMoves(move_list_stack[stack_depth], start_bb, dest_bb);

            for (ArimaaMove move : move_list_stack[stack_depth]) {
              GameState new_position = gs_stack[stack_depth];
              new_position.playFull(move, game);
              int new_steps_available = total_steps_available - move.steps;

              boolean result = test_GmtRabbit_GoalEmpty(new_position,
                goal_index,
                new_steps_available);
              if (result == true) {
                return true;
              }
            }
          }

          // Vacate push dest
          if ((touching_bb(lsb_bb) & empty_bb) == 0) {
            start_bb = touching_bb(lsb_bb) & player_bb;
            dest_bb = touching_bb(start_bb) & empty_bb & ~goal_bb;

            if (use_debug) {
              System.out.println("Vacate push dest");
              print_bitboard(start_bb, "Start_bb");
              print_bitboard(dest_bb, "dest_bb");
            }

            move_list_stack[stack_depth].clear();
            game.genSlideMoves(move_list_stack[stack_depth], start_bb, dest_bb);

            for (ArimaaMove move : move_list_stack[stack_depth]) {
              GameState new_position = gs_stack[stack_depth];
              new_position.playFull(move, game);
              int new_steps_available = total_steps_available - move.steps;

              boolean result = test_GmtRabbit_GoalEmpty(new_position,
                goal_index, new_steps_available);
              if (result == true) {
                return true;
              }
            }
          }
        }

        // Test if piece can push/pull unassisted
        long stronger_bb = game.stronger_enemy_bb[pt] &
          touching_bb(lsb_bb) & ~game.frozen_pieces_bb;
        if (stronger_bb != 0) {

          // Can only push, if its not the freezer or multiple freezers
          long pdest_bb = FULL_BB;
          if (((lsb_bb & gmt_freeze_bb) == 0) || !only_one_freezer) {
            pdest_bb = lsb_bb;
          }

          move_list_stack[stack_depth].clear();
          game.genDragMoves(move_list_stack[stack_depth],
            FULL_BB, FULL_BB, lsb_bb, FULL_BB);

          for (ArimaaMove move : move_list_stack[stack_depth]) {
            GameState new_position = gs_stack[stack_depth];
            new_position.playFull(move, game);
            if (test_decide(new_position,
              goal_index, total_steps_available - move.steps)) {
              return true;
            }
          }

        }
      }

      // Try to unfreeze frozen piece, that then slides to unfreeze rabbit
      long frozen_sup = touching_bb(touch_gmt_bb & empty_bb) & ~gmt_bb &
        game.frozen_pieces_bb & player_bb;
      long unfreeze_sq = touching_bb(frozen_sup) & empty_bb;

      // Rabbit can't slide backwards to provide salvation
      long salvation_bb = touching_bb(unfreeze_sq) & player_bb &
        ~game.frozen_pieces_bb;

      move_list_stack[stack_depth].clear();
      game.genSlideMoves(move_list_stack[stack_depth],
        salvation_bb, unfreeze_sq);
      if (move_list_stack[stack_depth].size() > 0) {
        return true;
      }

      // Try unassisted/assisted two step slide
      long inbetween_bb = touching_bb(touch_gmt_bb & empty_bb) & empty_bb;
      long potential_bb = touching_bb(inbetween_bb) &
        ~game.frozen_pieces_bb & player_bb;
      //     print_bitboard(potential_bb, "Potential Unassisted 2 step slide");

      while (potential_bb != 0) {
        long lsb_bb = potential_bb & -potential_bb;
        potential_bb ^= lsb_bb; // Remove the piece

        // Try unassisted two step slide
        int index = Util.FirstOne(lsb_bb);
        long reach_bb = game.piece_can_reach(index, 2);
        if ((reach_bb & touch_gmt_bb) != 0) {
          return debug(true);
        }

        // Try assisted two step slide
        if (piece_steps_available >= 3) {
          long dest_bb = touching_bb(inbetween_bb) & empty_bb;
          long start_bb = touching_bb(lsb_bb) & player_bb;
          move_list_stack[stack_depth].clear();
          gen_steps.genOneStep(game, move_list_stack[stack_depth],
            start_bb, dest_bb);

          for (ArimaaMove move : move_list_stack[stack_depth]) {
            GameState new_position = gs_stack[stack_depth];
            new_position.playFull(move, game);
            if (test_GmtRabbit_GoalEmpty(new_position,
              goal_index, total_steps_available - move.steps)) {
              return true;
            }
          }
        }

      }
    }

    if (piece_steps_available >= 3) {
      // Try unassisted 3 step slide
      long inbetween_bb = touching_bb(touch_gmt_bb & empty_bb) & empty_bb;
      long inbetween2_bb = touching_bb(inbetween_bb) & empty_bb;
      long potential_bb = touching_bb(inbetween2_bb) &
        ~game.frozen_pieces_bb & player_bb;
      //     print_bitboard(potential_bb, "Potential Unassisted 3 step slide");
      while (potential_bb != 0) {
        long lsb_bb = potential_bb & -potential_bb;
        potential_bb ^= lsb_bb; // Remove the piece

        int index = Util.FirstOne(lsb_bb);
        long reach_bb = game.piece_can_reach(index, 3);
        if ((reach_bb & touch_gmt_bb) != 0) {
          return debug(true);
        }

      }

      // Try slide/push combo
      long dest_bb = touching_bb(touch_gmt_bb & enemy_bb) & empty_bb;
      long start_bb = touching_bb(dest_bb) & player_nr_bb;
      move_list_stack[stack_depth].clear();
      gen_steps.genOneStep(game, move_list_stack[stack_depth],
        start_bb, dest_bb);

      for (ArimaaMove move : move_list_stack[stack_depth]) {
        GameState new_position = gs_stack[stack_depth];
        new_position.playFull(move, game);
        if (test_GmtRabbit_GoalEmpty(new_position,
          goal_index, total_steps_available - move.steps)) {
          return true;
        }
      }

      // Try 2step/slide combo
      inbetween_bb = touching_bb(touch_gmt_bb & empty_bb) & ~gmt_bb;
//      print_bitboard(inbetween_bb,"2step/slide");
      move_list_stack[stack_depth].clear();
      gen_steps.genTwoStep(game, move_list_stack[stack_depth],
        FULL_BB, inbetween_bb);

      for (ArimaaMove move : move_list_stack[stack_depth]) {
        GameState new_position = gs_stack[stack_depth];
        new_position.playFull(move, game);
        if (test_decide(new_position,
          goal_index, total_steps_available - move.steps)) {
          return true;
        }
      }

      // TODO: try assisted two step slide
      // Move support piece from touching_piece to touching inbetween_square
      // Is this the only case???? I think so!

      // Try assisted 1 step slide
      potential_bb = touching_bb(touch_gmt_bb) & ~gmt_bb & player_bb;
      if (use_debug) {
        print_bitboard(potential_bb, "Ass 1 step slide");
      }

      while (potential_bb != 0) {
        long lsb_bb = potential_bb & -potential_bb;
        potential_bb ^= lsb_bb; // Remove the piece

        // Case a) Piece is frozen, try to touch with player piece
        dest_bb = touching_bb(lsb_bb) & ~touch_gmt_bb;
        if (use_debug) {
          print_bitboard(dest_bb, "dest_bb");
        }
        move_list_stack[stack_depth].clear();
        gen_steps.genTwoStep(game, move_list_stack[stack_depth],
          FULL_BB & ~lsb_bb, dest_bb);

        for (ArimaaMove move : move_list_stack[stack_depth]) {
          GameState new_position = gs_stack[stack_depth];
          new_position.playFull(move, game);
          if (test_decide(new_position,
            goal_index, total_steps_available - move.steps)) {
            return true;
          }
        }

        // Case b) Try to remove freezer
        long freeze_bb = touching_bb(lsb_bb) &
          game.stronger_enemy_bb[game.getPieceType(lsb_bb)];
        if (atMostOneBitSet(freeze_bb)) {
          // Check for loss of protection captures
          long estart_bb = freeze_bb;
          if ((freeze_bb & TRAP_SQUARES) != 0) {
            long protection_bb = touching_bb(freeze_bb) & TOUCH_TRAPS &
              enemy_bb;
            if (atMostOneBitSet(protection_bb)) {
              estart_bb |= protection_bb;
            }
          }

          if (use_debug) {
            print_bitboard(estart_bb, "estart_bb");
          }
          move_list_stack[stack_depth].clear();
          game.genDragMoves(move_list_stack[stack_depth],
            FULL_BB, FULL_BB, estart_bb, FULL_BB);

          for (ArimaaMove move : move_list_stack[stack_depth]) {
            GameState new_position = gs_stack[stack_depth];
            new_position.playFull(move, game);
            if (test_decide(new_position,
              goal_index, total_steps_available - move.steps)) {
              return true;
            }
          }

        }

      }

    }

// No goal possible
    stack_depth--;
    return false;
  }


// returns true iff a player piece can reach the goal_bb
// *unassisted* in the required number of steps
  boolean can_piece_reach(GameState game, long goal_bb, long origin_bb,
    int steps) {
    // Flood fill to test for win
    long reached_bb = goal_bb & game.empty_bb;

    for (int i = 1; i <= steps; i++) {
      reached_bb = game.touching_bb(reached_bb & game.empty_bb);
      long test_bb = reached_bb & origin_bb;
      while (test_bb != 0) {

        int piece_index = ai_util.Util.FirstOne(test_bb);
        test_bb ^= 1L << piece_index; // Clear bit

        // We touched a piece, now run the scan forward to verify result
        long result = game.piece_can_reach(piece_index, i);
        if ((result & goal_bb) != 0) {
          return true;
        }
      }
    }

    return false;
  }

// Check for unassisted rabbit dashes
  boolean can_rabbit_run(GameState game, int steps) {

    int player = game.player;

    long goal_bb = (player == PL_WHITE) ? RANK_8 : RANK_1;
    long start_bb = game.empty_bb & goal_bb;

    // *Almost* all squares a rabbit can reach unaided
    long good_sq_bb = game.touching_bb(game.colour_bb[player]) |
      ~game.touching_bb(game.stronger_enemy_bb[player]);
    good_sq_bb &= game.empty_bb;
    good_sq_bb |= game.piece_bb[player];

    /*
        if (use_test_win_debug) {
          print_bitboard(good_sq_bb, "Good SQ BB");
        }
     */
    // Flood fill to test for win
    long reached_bb = start_bb;

    for (int i = 1; i <= steps; i++) {
      reached_bb = game.touching_bb(reached_bb) & good_sq_bb;
      long test_bb = reached_bb & game.piece_bb[player];
      while (test_bb != 0) {
        int rabbit_index = ai_util.Util.FirstOne(test_bb);
        test_bb ^= 1L << rabbit_index;

        // We touched a rabbit, now run the scan forward to verify result
        long result = game.piece_can_reach(rabbit_index, i);
        if ((result & goal_bb) != 0) {
          this.saved_rabbit_index = rabbit_index;
          return true;
        }
      }
    }

    return false;
  }

  private boolean debug(boolean result) {
    if (use_debug) {
      Thread.dumpStack();
      System.err.flush();
    }

    return result;
  }

// Create bitboards for goal patterns
  private static long goal_pattern_bb[][];
  static {
    goal_pattern_bb = new long[64][9];

    for (int i = 0; i < 64; i++) {
      long goal_bb = 1L << i;

      // Pattern 0 is the goal square
      goal_pattern_bb[i][0] = goal_bb;

      // Pattern 1 is the goal square and touching squares on the same rank
      long result = goal_bb;
      result |= (goal_bb & NOT_FILE_H) << 1;
      result |= (goal_bb & NOT_FILE_A) >>> 1;
      goal_pattern_bb[i][1] = result;

      // Pattern 2 is squares touching the goal square on the same rank
      goal_pattern_bb[i][2] = result ^ goal_bb;

      // Pattern 3 is the goal square and touching squares on a different rank
      result = goal_bb;
      result |= (goal_bb & NOT_RANK_8) << 8;
      result |= (goal_bb & NOT_RANK_1) >>> 8;
      goal_pattern_bb[i][3] = result;

      // Pattern 4 is touching squares on a different rank
      // ie this is the gmt square
      goal_pattern_bb[i][4] = result ^ goal_bb;

      // Pattern 5 is all squares touching gmt square *EXCEPT* the goal square
      // This is all squares exactly 2 steps from goal, not on goal line
      result = GameState.touching_bb(goal_pattern_bb[i][4]);
      result ^= goal_bb;
      goal_pattern_bb[i][5] = result;

      // Pattern 6 is all squares within 2 steps of goal square not on goal line
      goal_pattern_bb[i][6] = goal_pattern_bb[i][4] | goal_pattern_bb[i][5];

      // Pattern 7 is all squares within 3 steps of goal square not on goal line
      result = GameState.touching_bb(goal_pattern_bb[i][6]);
      result &= NOT_RANK_8;
      result &= NOT_RANK_1;
      goal_pattern_bb[i][7] = result;

      // Pattern 8 is all squares exactly 3 steps from goal, not on goal line
      result = GameState.touching_bb(goal_pattern_bb[i][5]);
      result &= ~goal_pattern_bb[i][5];
      goal_pattern_bb[i][8] = result;
    }
  }

  public static void main(String args[]) {
    String text[] = {

      "17b %13 +-----------------+%138| r r   c c r   r |%137|     r     d     |%136| r H             |%135|                 |%134|           E r   |%133| R H       R M d |%132|           e R r |%131|   R   R C h   R |%13 +-----------------+%13   a b c d e f g h%13", // Belbo

      "26w %13 +-----------------+%138|   r r d H r r r |%137| r     c E R   r |%136|   h             |%135|                 |%134|     m           |%133|       r         |%132|     M e   R     |%131| R     R D   R R |%13 +-----------------+%13   a b c d e f g h%13", // 99of9 vs RonWeasley Test position

      "2w %13 +-----------------+%138| r r         r   |%137| r R E   r   M r |%136| C m h     X E   |%135|   H   d c       |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   e r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

      "2w %13 +-----------------+%138| r r     r r r r |%137| e e r C r   r   |%136|   D R R   X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "33b %13 +-----------------+%138|         d r     |%137|   r     M r e   |%136|   d   c     R   |%135|       c   E   R |%134| r   H         r |%133| D h R   d   r C |%132| r D m   C       |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13",

      "33b %13 +-----------------+%138|         d r     |%137|   r     M r e   |%136|   d   c   R     |%135|       c   E   R |%134| r   H         r |%133| D h R   d   C   |%132| r D m   R   r C |%131| R R       R   R |%13 +-----------------+%13   a b c d e f g h%13",

      "31b %13 +-----------------+%138|   c   d     c   |%137|         d r     |%136|               C |%135|   r             |%134|             r h |%133|   M   R   r r R |%132| R h   m E e H   |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

      "33w %13 +-----------------+%138|               H |%137| r         M e   |%136|   d   c     R   |%135|       c     E R |%134| r   H         r |%133| D h R   d   r C |%132| r D m         C |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13",

      "33b %13 +-----------------+%138|     r r r r M r |%137| r         c r   |%136|       d d       |%135| h D c m         |%134|     E e         |%133|     r           |%132|   R h         H |%131| R R   H R R R R |%13 +-----------------+%13   a b c d e f g h%13",

      "22w %13 +-----------------+%138|                 |%137| r r r       H   |%136|   d     h   h R |%135| c   d   r r     |%134| r C     c   r   |%133| D e   r   E C   |%132|     H M D m     |%131| R R R R R R R   |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137| D   c R         |%136|       e e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137| D   c R c       |%136|       e e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|   D c R c       |%136|       e e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|     M   e       |%136|       R e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|                 |%136|       R e       |%135|                 |%134|         C        |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|       R e       |%136|                 |%135|                 |%134|         C        |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e       |%136|                 |%135|                 |%134|       C         |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e       |%136|                 |%135|       C e       |%134|                 |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|                 |%136|       R d       |%135|                 |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|         d H     |%136|       R         |%135|                 |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   r r r r |%137|     r D R r r   |%136|     M R h r     |%135|   e R M r r     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r   r r r |%137|     r r R r r   |%136|     M   h r     |%135|   e R M r r     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r   r r r |%137|     r r R r r   |%136|       M h r     |%135|   e R M r r     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r   r r r |%137|     r r R r r   |%136|     D M h r     |%135|   e R M   D     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   E r r r r |%137|     r e R e r   |%136|   D r R   X     |%135|   e R M   D     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r E r   r r |%137|     r e R e r   |%136|   D r R   X     |%135|   e R M         |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r C r r r r |%137|     r       r   |%136|   D r R r X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r     r r r r |%137|     r C r   r   |%136|   D r R r X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r     r r r r |%137|     r C r   r   |%136|   D R     X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138|   r r   H r r r |%137|     e   C r     |%136|     e           |%135|     e R M e     |%134|         R       |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138|   r r   H r r r |%137|     e   C r     |%136|     e           |%135|     e R M e     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r r r E   |%137|       R R     d |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|   r   R r       |%133|     X r r X     |%132|       d R     r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|   r   R r       |%133|     X r r X   E |%132|       d R     r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|   r   R r       |%133|     X r r X     |%132|       d R R     |%131| R R R R   e   R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r r r r r |%137|   R   r r   r   |%136|     X     X     |%135|                 |%134|           D     |%133|     X     X e   |%132| R R R R R h     |%131|           c     |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r r r E   |%137|               d |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r E r r E   |%137|     E         d |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138|   r r r r r r r |%137|   R   r r   r   |%136|     X     X     |%135|                 |%134|                 |%133|     X     X     |%132|                 |%131|           c     |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138|   r r r r r r r |%137|     R r r   r   |%136|     X     X     |%135|                 |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X     X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X C   X     |%135|                 |%134|     R           |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2b %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r r r E   |%137|       R R       |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r r r E   |%137|       R R     r |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   e r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r d e r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|     R           |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|     R           |%133|   r X   r X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   r   r   |%136|   e r   r X     |%135|       R R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   E   r   |%136|   e r   r X     |%135|       R R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   E   r   |%136|   e r   r X     |%135|       R R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r e H   r r r |%137|   e e   e   r   |%136|   e e R M X     |%135|         R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   r   r   |%136|   e r r   X     |%135|         R       |%134|       R         |%133|     X     X     |%132|                 |%131|     R         R |%13 +-----------------+%13   a b c d e f g h%13",

      "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C   |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C e |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C e |%136|                 |%135|             D   |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

      "2w %13 +-----------------+%138| r r r r   r r r |%137|     E d R   C e |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C e |%136|                 |%135|                 |%134|         D       |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R     e |%136|             C   |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

      "2w %13 +-----------------+%138| r r   H d r r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r d H r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r D   r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r D H r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r r D d r r |%137|       r R H     |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   D d r r |%137|       h R H     |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r   D d r r |%137|       h R     H |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

      "2w %13 +-----------------+%138| r r r   D d r r |%137|       h R     H |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   D D d r r |%137|       h R   H   |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

      "2w %13 +-----------------+%138| r r r D c   r r |%137|       h R h H   |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r r D c   r r |%137|       h R h H   |%136|         C       |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r E D c   r r |%137|       h R h H   |%136|         C       |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

    };

    String text2[] = {
   //   "2w %13 +-----------------+%138| h r   D D E e r |%137|     R h M E H   |%136|       R e       |%135|       c         |%134|   H     h     D |%133|   e             |%132|           D     |%131|                 |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

   //   "2w %13 +-----------------+%138| r r D   r r h r |%137| e e h R r h r   |%136|   D R h H       |%135|   e R M H     e |%134|       R       M |%133|             M   |%132|             R   |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

 //     "2w %13 +-----------------+%138| r r D   r r h r |%137| e e r R m   r   |%136|   D R d R       |%135|   e R M D     e |%134|       R         |%133|             M   |%132|             R   |%131| C     R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

      "2w %13 +-----------------+%138| r r   E D   e r |%137|     R h R h H   |%136|       R         |%135|     r           |%134|         h     H |%133|   e e           |%132|         M       |%131| h               |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

   //   "2w %13 +-----------------+%138| r r r E   r r r |%137|     r m R r r   |%136|       M m   d   |%135| h e   M e D     |%134|   c   R   c     |%133| H               |%132|             D   |%131| e     R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

   //   "2w %13 +-----------------+%138| r r r M   r r r |%137|     r c R r r   |%136|       C h   d   |%135| h e d M e D     |%134|       R h       |%133|               H |%132|                 |%131| e d   R D     R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

      /*
            "2w %13 +-----------------+%138| r r   D D d e r |%137|     R h R   H   |%136|       R         |%135|                 |%134|         h     H |%133|   e             |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137| C   R d R   C e |%136|       R         |%135|                 |%134|                 |%133| C C     c       |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r r   r r r D |%137|       R e R   E |%136|         C     h |%135|               H |%134|         e       |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   M e |%137|     e R     H   |%136|   e     m m r C |%135|         m e   H |%134| h   d e     M   |%133| m     H       c |%132| H         R   H |%131|       C m     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r D   r r h r |%137| e e r R r   r   |%136|     R d H       |%135|   e R M e     e |%134|                 |%133|             M   |%132|             R   |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r D   r r h r |%137| e e r R r   r   |%136|   D R d H       |%135|   e R M e     e |%134|       R         |%133|             M   |%132|             R   |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r r   e   C m |%137|     e R     H   |%136|           m r H |%135| E   c c   c     |%134|     R e     M m |%133| m     H   M   e |%132|       r   R   C |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   C   |%137|     e R     H m |%136|           m r H |%135| E   c c   c     |%134|     R e     M m |%133| m     H   M   e |%132|       r   R   C |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138|   r r   e   M e |%137|     e R     H   |%136|   D     e m r D |%135| E   c R         |%134|     R e     M R |%133| m   D H       e |%132|       r   R   C |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C e |%136|                 |%135|       R   m     |%134|     d R         |%133|         h   h   |%132| E               |%131|                 |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r r   e   D e |%137| d C e R     C   |%136|     e   h m r H |%135|         e       |%134|     R e   m M   |%133| m R   e   c   e |%132|           R   H |%131|       C h h   m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r r   e   D e |%137|     e R     H   |%136|         m m r C |%135| E   c           |%134|     R r     M R |%133| m     H   c h e |%132|       r d R   C |%131|       H h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r r   e   D e |%137| d C e R     C   |%136|     e   h m r H |%135|         e       |%134|     R e   m M   |%133| m R   e   c   e |%132|           R   H |%131|       C h h   m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2w %13 +-----------------+%138| r r r   e   D e |%137|     e R     H   |%136|         m m r C |%135| E   c           |%134|     R r     M R |%133| m     H   c h e |%132|       r d R   C |%131|       H h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "17b %13 +-----------------+%138| r r   c c r   r |%137|     r     d     |%136| r H             |%135|                 |%134|           E r   |%133| R H       R M d |%132|           e R r |%131|   R   R C h   R |%13 +-----------------+%13   a b c d e f g h%13", // Belbo

            "26w %13 +-----------------+%138|   r r d H r r r |%137| r     c E R   r |%136|   h             |%135|                 |%134|     m           |%133|       r         |%132|     M e   R     |%131| R     R D   R R |%13 +-----------------+%13   a b c d e f g h%13", // 99of9 vs RonWeasley Test position

            "2w %13 +-----------------+%138| r r         r   |%137| r R E   r   M r |%136| C m h     X E   |%135|   H   d c       |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   e r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

            "2w %13 +-----------------+%138| r r     r r r r |%137| e e r C r   r   |%136|   D R R   X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "33b %13 +-----------------+%138|         d r     |%137|   r     M r e   |%136|   d   c     R   |%135|       c   E   R |%134| r   H         r |%133| D h R   d   r C |%132| r D m   C       |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13",

            "33b %13 +-----------------+%138|         d r     |%137|   r     M r e   |%136|   d   c   R     |%135|       c   E   R |%134| r   H         r |%133| D h R   d   C   |%132| r D m   R   r C |%131| R R       R   R |%13 +-----------------+%13   a b c d e f g h%13",

            "31b %13 +-----------------+%138|   c   d     c   |%137|         d r     |%136|               C |%135|   r             |%134|             r h |%133|   M   R   r r R |%132| R h   m E e H   |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

            "33w %13 +-----------------+%138|               H |%137| r         M e   |%136|   d   c     R   |%135|       c     E R |%134| r   H         r |%133| D h R   d   r C |%132| r D m         C |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13",

            "33b %13 +-----------------+%138|     r r r r M r |%137| r         c r   |%136|       d d       |%135| h D c m         |%134|     E e         |%133|     r           |%132|   R h         H |%131| R R   H R R R R |%13 +-----------------+%13   a b c d e f g h%13",

            "22w %13 +-----------------+%138|                 |%137| r r r       H   |%136|   d     h   h R |%135| c   d   r r     |%134| r C     c   r   |%133| D e   r   E C   |%132|     H M D m     |%131| R R R R R R R   |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137| D   c R         |%136|       e e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137| D   c R c       |%136|       e e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|   D c R c       |%136|       e e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|     M   e       |%136|       R e       |%135|                 |%134|                  |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|                 |%136|       R e       |%135|                 |%134|         C        |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|       R e       |%136|                 |%135|                 |%134|         C        |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e       |%136|                 |%135|                 |%134|       C         |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e       |%136|                 |%135|       C e       |%134|                 |%133|     X R   X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|                 |%136|       R d       |%135|                 |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|         d H     |%136|       R         |%135|                 |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|     r D R r r   |%136|     M R h r     |%135|   e R M r r     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137|     r r R r r   |%136|     M   h r     |%135|   e R M r r     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137|     r r R r r   |%136|       M h r     |%135|   e R M r r     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137|     r r R r r   |%136|     D M h r     |%135|   e R M   D     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   E r r r r |%137|     r e R e r   |%136|   D r R   X     |%135|   e R M   D     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r E r   r r |%137|     r e R e r   |%136|   D r R   X     |%135|   e R M         |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r C r r r r |%137|     r       r   |%136|   D r R r X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r     r r r r |%137|     r C r   r   |%136|   D r R r X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r     r r r r |%137|     r C r   r   |%136|   D R     X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138|   r r   H r r r |%137|     e   C r     |%136|     e           |%135|     e R M e     |%134|         R       |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138|   r r   H r r r |%137|     e   C r     |%136|     e           |%135|     e R M e     |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r r r E   |%137|       R R     d |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
            "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|   r   R r       |%133|     X r r X     |%132|       d R     r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
            "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|   r   R r       |%133|     X r r X   E |%132|       d R     r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
            "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|   r   R r       |%133|     X r r X     |%132|       d R R     |%131| R R R R   e   R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r r r r r |%137|   R   r r   r   |%136|     X     X     |%135|                 |%134|           D     |%133|     X     X e   |%132| R R R R R h     |%131|           c     |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r r r E   |%137|               d |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r E r r E   |%137|     E         d |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138|   r r r r r r r |%137|   R   r r   r   |%136|     X     X     |%135|                 |%134|                 |%133|     X     X     |%132|                 |%131|           c     |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138|   r r r r r r r |%137|     R r r   r   |%136|     X     X     |%135|                 |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X     X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X C   X     |%135|                 |%134|     R           |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2b %13 +-----------------+%138| r r   r r r r r |%137|       r r   r   |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r r r E   |%137|       R R       |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r r r E   |%137|       R R     r |%136|     X C   X     |%135|                 |%134|     R           |%133|     X   r X     |%132|                 |%131| R R R   R R R R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   e r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r d e r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|     R           |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|     R           |%133|   r X   r X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   r   r   |%136|   e r   r X     |%135|       R R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   E   r   |%136|   e r   r X     |%135|       R R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   E   r   |%136|   e r   r X     |%135|       R R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r e H   r r r |%137|   e e   e   r   |%136|   e e R M X     |%135|         R       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   r   r   |%136|   e r r   X     |%135|         R       |%134|       R         |%133|     X     X     |%132|                 |%131|     R         R |%13 +-----------------+%13   a b c d e f g h%13",

            "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C   |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C e |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C e |%136|                 |%135|             D   |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

            "2w %13 +-----------------+%138| r r r r   r r r |%137|     E d R   C e |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R   C e |%136|                 |%135|                 |%134|         D       |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r   r r r |%137|       d R     e |%136|             C   |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

            "2w %13 +-----------------+%138| r r   H d r r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r d H r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r D   r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r D H r r |%137|       d R       |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r r D d r r |%137|       r R H     |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   D d r r |%137|       h R H     |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r   D d r r |%137|       h R     H |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

            "2w %13 +-----------------+%138| r r r   D d r r |%137|       h R     H |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r   D D d r r |%137|       h R   H   |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

            "2w %13 +-----------------+%138| r r r D c   r r |%137|       h R h H   |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r r D c   r r |%137|       h R h H   |%136|         C       |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
            "2w %13 +-----------------+%138| r r E D c   r r |%137|       h R h H   |%136|         C       |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

            "2w %13 +-----------------+%138| r r   r r r r r |%137|   R     e       |%136|     h           |%135|     r C c       |%134|                 |%133| C     R   c r e |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e       |%136| E   h           |%135|     r C c       |%134|                 |%133| C     R   c r e |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r   r r r r r |%137|     R           |%136|     h           |%135|     r C d       |%134|                 |%133| C     R   c r e |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r   r r r r r |%137|     R           |%136|     h           |%135|     r           |%134|     C d         |%133|           c r e |%132|     R           |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r   r r r r r |%137|     R           |%136|     h           |%135|     r           |%134|   C   d         |%133|           c r e |%132|     R           |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   C m |%137|     e R         |%136|           m r H |%135| E   c R   c     |%134|             M m |%133| m m m H       e |%132|                 |%131|       C       m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

            "2b %13 +-----------------+%138| m r   H r d r r |%137|   e   C r C r   |%136|   e R           |%135|   e R M e       |%134|   r   R r   r   |%133|       E r     H |%132|       d R     r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
            "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e   h   |%136| E   h           |%135|     r R c       |%134|     R M         |%133| C     H   c d e |%132|               H |%131|       C       m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2b %13 +-----------------+%138| r r r H r r r r |%137|   e   C r   r   |%136|   e             |%135|   e R M e d     |%134|   r   c r       |%133|       r r r   E |%132|       d E     r |%131| R R R M   R   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
            "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C R   r   |%136| r e             |%135|   e D M e     c |%134|   r   R r       |%133|       R r r   E |%132|       E R H   r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
            "3b %13 +-----------------+%138|         d r     |%137|   r     M r r   |%136|   d   c     R c |%135|       c   E   c |%134| r   H         r |%133| D R R   D   r C |%132| r D m   e     r |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13TS: 268%13",
            "2w %13 +-----------------+%138| r r   r r r r r |%137| d R     e       |%136|       R         |%135|   m r C c       |%134| c               |%133| H     R   c c e |%132|                 |%131|   H   R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| e r   e r r r r |%137|       r R h r   |%136|       C     D   |%135| D   R h         |%134|         C       |%133|                 |%132|                 |%131|     m           |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| m r r   r r r r |%137|     R   e       |%136|       e         |%135|       R e       |%134|       M     c   |%133|       R H       |%132|     h           |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|     R   e   h   |%136| E R   e         |%135|     r R c       |%134|     R M     M   |%133| m     H       e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R         |%136|         D   r C |%135|                 |%134|     R e     M   |%133| m     H       e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R r r r r |%136|         D   r C |%135|       r r E     |%134|     R e r   M   |%133| m     H       e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R         |%136|         D   r C |%135|                 |%134|     R e     M   |%133| m     H       e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r m e   r r |%137|     e R R       |%136|         D   r c |%135|         r       |%134|     R e     H   |%133| m     H       e |%132|     R     R   H |%131| e     C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137| r   e R     H   |%136|       m D   r C |%135|                 |%134|     R r     M   |%133| m     H h     e |%132|           R M   |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R     H   |%136|   e     D   r C |%135|         m     H |%134| h   d e     M   |%133| m     H       M |%132|           R   H |%131|       C m     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r r   E r r |%137| D   r r R r r   |%136|       h h   D   |%135|   d R M r r     |%134|                 |%133|                 |%132|                 |%131|       R     C R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|   C e R     C   |%136|         D   r H |%135|         e       |%134|     R e   m M   |%133| m R   M       e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e r r r |%137|     e R     H   |%136|         D   c C |%135|         m       |%134|     R e   C M   |%133| m     H       e |%132|   H       R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2b %13 +-----------------+%138| r r   C r r r r |%137|   e   C R   r   |%136| r e             |%135|   e D M e H   c |%134| C r   R r       |%133|       R C     E |%132|       E R H   r |%131| R R   R   E   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
            "2w %13 +-----------------+%138| r r r h   r r r |%137|     r m R r r   |%136|       M h   d   |%135| h e R M e D     |%134|       R         |%133|                 |%132|                 |%131| e     R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R     C   |%136|         D   r C |%135|         m     E |%134|     R e     M   |%133| m     H       e |%132|         R R   H |%131|     D   h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R     H   |%136|             r C |%135| E   c           |%134|     R e     M R |%133| m     H       e |%132|       r   R   C |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2w %13 +-----------------+%138| r r r   r   r   |%137| e   e R     D   |%136|         D   r C |%135|         m M     |%134|     H e     M   |%133| m     H       e |%132|           R   H |%131|       C R     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
            "2b %13 +-----------------+%138| r r   C r r r r |%137|   e   C R   r   |%136| r e             |%135|   e D M e H   c |%134| C r   R r       |%133|       R C     E |%132|       E R H   r |%131| R R   R   E   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
            "2w %13 +-----------------+%138| r r r m e   r r |%137|     e R R       |%136|         D   r c |%135|         r       |%134|     R e     H   |%133| m     H       e |%132|     R     R   H |%131| e     C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
      */

    };

    TestForGoal test = new TestForGoal();
    ArimaaEngine engine = new ArimaaEngine();
    test.use_debug = true;

    for (String position_text : text2) {
      GameState position = new GameState(position_text);

      test_position(position, test, engine);
    }

    System.out.println("Agree: " + agree);
    System.out.println("Disagree: " + disagree);

  }


  private static long agree = 0;
  private static long disagree = 0;

  private static void test_position(GameState position, TestForGoal test,
    ArimaaEngine engine) {
    // Run test positions
    System.out.println(position.toBoardString());
    System.out.println(position.toEPDString());
    System.out.println(position.toSetupString());
    position.clear_trap_squares();
    System.out.println(position.toBoardString());

    boolean result = test.test(position);
    engine.resetStats();
    boolean result2 = engine.can_player_goal(position);
    System.out.println("FINAL: " + result + " Engine: " + result2);
    System.out.println(engine.getStats());
    System.out.flush();
    if (result == result2) {
      agree++;
    } else {
      disagree++;
    }
  }

}
