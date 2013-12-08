package arimaa3;

import ai_util.*;

public class GenCaptures extends ArimaaBaseClass {

  public GenCaptures() {
  }

  private final boolean createText = false;


  // Working variables for gen_moves
  private MoveList move_list_stack[] = new MoveList[5];
  private GameState gs_stack[] = new GameState[5];
  private int stack_depth;
  {
    for (int i = 0; i < gs_stack.length; i++) {
      gs_stack[i] = new GameState();
    }
    for (int i = 0; i < move_list_stack.length; i++) {
      move_list_stack[i] = new MoveList(1000);
    }
  }


  // TODO: detect repeated positions in the captures!

  //changed to protected by Neema
  private MoveList move_data;
  protected GameState saved_initial_position = null;
  protected RepetitionHashTable repetition = new RepetitionHashTable(19);
  private GenSteps gen_steps = new GenSteps();

  public void genCaptures(GameState initial_position, MoveList move_list, boolean complete_turn) {

    this.complete_turn = complete_turn;
    // Need the frozen/dominated bb's
    initial_position.compute_tertiary_bitboards();

    // Setup working variables
    this.move_data = move_list;
    this.gen_capture_calls++;
    this.saved_initial_position = initial_position;
    this.stack_depth = -1;
    this.repetition.increaseAge();

    // Test each trap square individually
    for (int i = 0; i < 4; i++) {
      if (createText) {
        System.out.println("Trap Square: " +
          GameState.getStringIndex(TRAP_INDEX[i]) +
          "\n");
      }

      if (test_trap_precondition(initial_position, i)) {
        test_trap(initial_position, i);
      }
    }

  }

  //******** changed by Neema to protected *******
  // Statistics collection stuff
  protected long trap_precondition_calls = 0;
  protected long trap_precondition_false = 0;
  private long gen_capture_calls = 0;

  public String getStats() {
    String result = "";
    result += "Gen capture calls: " + gen_capture_calls + "\n";
    result +=
      Util.ProbStats("Trap Pre", trap_precondition_calls,
      trap_precondition_false);
    return result;
  }

  public void resetStats() {
    trap_precondition_calls = 0;
    trap_precondition_false = 0;
    gen_capture_calls = 0;
  }

  /**
   * Performs some basics test to see if captures can be ruled out
   * @param game GameState
   * @param trap_id int
   * @return boolean
   */
  private boolean test_trap_precondition(GameState game, int trap_id) {

    trap_precondition_calls++;

    // If enemy elephant is touching trap, NO CAPTURES POSSIBLE!
    if ((TOUCH_TRAP[trap_id] & game.piece_bb[10 + game.enemy]) != 0) {
      trap_precondition_false++;
      return false;
    }

    // If 3 e pieces touch trap, NO CAPTURES POSSIBLE
    long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap_id];
    int e_touch = Util.PopCnt(e_touch_bb);
    if (e_touch >= 3) {
      trap_precondition_false++;
      return false;
    }

    // If 1 non dominated e piece touch trap
    // AND any other epiece touch trap, NO CAPTURE POSSIBLE
    long e_touch_non_dominated_bb = (e_touch_bb & ~game.dominated_pieces_bb);
    int e_touch_non_dominated = Util.PopCnt(e_touch_non_dominated_bb);
    if (e_touch_non_dominated >= 1 && e_touch >= 2) {
      trap_precondition_false++;
      return false;
    }

    if (game.getStepsRemaining() <= 3) {
      // If no epieces touch trap, NO CAPTURES POSSIBLE
      if (e_touch == 0) {
        trap_precondition_false++;
        return false;
      }

      // If two epieces touch trap, NO CAPTURES POSSIBLE
      if (e_touch >= 2) {
        trap_precondition_false++;
        return false;
      }

      // If one non=dominated epiece touch trap, NO CAPTURES POSSIBLE
      if (e_touch_non_dominated >= 1) {
        trap_precondition_false++;
        return false;
      }
    }

    return true;
  }


  // Attempt to generate useful moves towards capturing an enemy piece
  private void test_trap(GameState game, int trap_id) {

    // Get some working variables
    long enemy_bb = game.colour_bb[game.enemy];
    long player_bb = game.colour_bb[game.player];
    int steps_available = game.getStepsRemaining();

    // Case 1: There is an epiece on the trap

    // I believe this case is complete!
    if ((enemy_bb & TRAP[trap_id]) != 0) {

      // By Definition an epiece must be touching trap
      long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap_id];
      int e_touch = Util.PopCnt(e_touch_bb);

      // Try and drag the epieces away with an already touching piece
      if (steps_available >= e_touch * 2) {
        try_drag_moves(game, e_touch_bb, FULL_BB, trap_id);
      }

      // Try and move a dominant piece next to the epiece touching the trap
      int extra_steps = steps_available - 2 * e_touch;
      if (extra_steps >= 1) {

        // Given: there is only 1 epiece touching the trap
        // Given: there is no epiece on the trap
        // The precondition check catches 2 non dominant pieces touching trap
        assert (Util.PopCnt(e_touch_bb) == 1);
        assert ((enemy_bb & TRAP[trap_id]) != 0);

        // Get the enemy piece type
        int e_piece_type = game.getPieceType(e_touch_bb);

        long p_dest_bb = game.touching_bb(e_touch_bb);
        long p_stronger_bb = game.stronger_enemy_bb[e_piece_type];

        // Try moving a dominant piece next to the epiece
        try_two_steps(game, p_stronger_bb, p_dest_bb, trap_id, extra_steps);

      }

    }

    // Case 2: There is no epiece on the trap AND
    // there are epiece(s) touching the trap

    // I believe this case is complete!
    // TODO algorithm misses player suiciding his piece on the trap.
    else if ((enemy_bb & TOUCH_TRAP[trap_id]) != 0) {

      // By Definition an epiece must be touching trap
      long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap_id];
      int e_touch = Util.PopCnt(e_touch_bb);

      // Try and drag an epiece onto the trap
      // Only works if there are at least 2 steps available
      if (steps_available >= 2) {
        long e_start_bb = TOUCH_TRAP[trap_id];
        long e_dest_bb = TRAP[trap_id];
        try_drag_moves(game, e_start_bb, e_dest_bb, trap_id);
      }

      // Try and move a dominant piece next to the epiece touching the trap
      int extra_steps = steps_available - 2 * e_touch;
      if (extra_steps >= 1) {

        // Given: there is only 1 epiece touching the trap
        // Given: there is no epiece on the trap
        // The precondition check catches 2 non dominant pieces touching trap
        assert (Util.PopCnt(e_touch_bb) == 1);
        assert ((enemy_bb & TRAP[trap_id]) == 0);

        // Get the enemy piece type
        int e_piece_type = game.getPieceType(e_touch_bb);

        long p_dest_bb = touching_bb(e_touch_bb);
        long p_stronger_bb = game.stronger_enemy_bb[e_piece_type];

        // Try moving a dominant piece next to the epiece
        try_two_steps(game, p_stronger_bb, p_dest_bb, trap_id, extra_steps);

      }

    }

    // Case 3: There is no epiece on the trap AND
    // there is no epiece touching the trap AND
    // there are epiece(s) 2 steps from the trap

    // THIS CASE IS COMPLETE!!!!!
    else if ((enemy_bb & TOUCH2_TRAP[trap_id]) != 0) {

      // Try and drag an epiece closer to the trap
      // Only works if there are at least 4 steps available
      if (steps_available >= 4) {
        long e_start_bb = TOUCH2_TRAP[trap_id];
        long e_dest_bb = TOUCH_TRAP[trap_id];
        try_drag_moves(game, e_start_bb, e_dest_bb, trap_id);
      }
    }

  }


  // Try a two step move
  private void try_two_steps(GameState initial_position, long p_start_bb,
    long p_dest_bb, int trap_id, int extra_steps) {

    stack_depth++;

    assert (initial_position.getStepsRemaining() >= 3);
    assert (extra_steps == 1 || extra_steps == 2);

    move_list_stack[stack_depth].clear();

    if (extra_steps == 2) {
      gen_steps.genTwoStepNF(initial_position, move_list_stack[stack_depth],
        p_start_bb, p_dest_bb);
    } else {
      gen_steps.genOneStepNF(initial_position, move_list_stack[stack_depth],
        p_start_bb, p_dest_bb);

    }

    for (ArimaaMove move : move_list_stack[stack_depth]) {
      assert (move.steps == 2 || move.steps == 1);
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, initial_position);

      if (createText) {
        System.out.println("2 Step\n" + initial_position);
        System.out.println(move);
        System.out.println(new_position);
      }

      // Test the trap again, but with reduced steps
      test_trap(new_position, trap_id);

    }
    stack_depth--;

  }


  // Try to drag a piece
  private void try_drag_moves(GameState initial_position, long e_start_bb,
    long e_dest_bb,
    int trap_id) {

    stack_depth++;

    assert (initial_position.getStepsRemaining() >= 2);

    move_list_stack[stack_depth].clear();
    initial_position.genDragMoves(move_list_stack[stack_depth],
      FULL_BB, FULL_BB, e_start_bb, e_dest_bb);

    for (ArimaaMove move : move_list_stack[stack_depth]) {

      // Play the move
      assert (move.steps == 2);
      GameState new_position = gs_stack[stack_depth];
      new_position.playFull(move, initial_position);

      if (createText) {
        System.out.println("Drag\n" + initial_position);
        System.out.println(move);
        System.out.println(new_position);
      }

      // If the move captured an enemy piece, record the event
      if (new_position.is_enemy_piece_captured(initial_position)) {
        this.record_move(new_position);
      }

      // Test the trap again, but with reduced steps
      int steps_remaining = saved_initial_position.getStepsRemaining()
        + saved_initial_position.total_steps - new_position.total_steps;
      if (steps_remaining >= 2) {
        test_trap(new_position, trap_id);
      }

    }
    stack_depth--;

  }

  //changed to protected by Neema
  protected boolean complete_turn;

  private void record_move(GameState new_position) {
    long hash_code = new_position.getPositionHash();
    if (!repetition.isRepetition(hash_code)) { // Has side effects
      ArimaaMove move = move_data.getMove();
      move.difference(saved_initial_position, new_position);
      assert (move.steps <= saved_initial_position.getStepsRemaining());
      if ( complete_turn ) {
        move.steps = saved_initial_position.getStepsRemaining();
      }
      move.move_ordering_value = ArimaaEngine.ORDER_CAPTURE;
    }
  }


  public static void main(String args[]) {

    String text[] = {

      /*
          "27b %13 +-----------------+%138| r   r c c   r r |%137| d r r d   r     |%136| M e   H     m   |%135| h               |%134|     r     E     |%133|                 |%132| D   C   D h   H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13", // Naveed


          "27b %13 +-----------------+%138| r   r c c   r r |%137| d r   d   r     |%136| M e   H     m   |%135| h               |%134|     r     E     |%133|                 |%132| D   C   D h   H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13", // Naveed

          "27w %13 +-----------------+%138| r   r c c   r r |%137| d r   d   r     |%136| M e         m   |%135| h               |%134|     r H   E     |%133|                 |%132| D   C   D h   H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13", // Naveed
      */
      "27b %13 +-----------------+%138| r r r       r r |%137| C   c r       D |%136|   m   d M     R |%135| C     R   E h   |%134| R       H e     |%133|       h         |%132|             R   |%131|           R     |%13 +-----------------+%13   a b c d e f g h%13",
      /*
          "22b %13 +-----------------+%138| r r H r r r r r |%137| c   m E   d     |%136|   d R D h   c   |%135|             H   |%134| r M e           |%133| h               |%132| D             D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
          "22b %13 +-----------------+%138| r r H r r r r r |%137| c   m E   d     |%136|   d R D h   c   |%135|             H   |%134| r   e           |%133| h               |%132| D             D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
          "22b %13 +-----------------+%138| r r H r r r r r |%137| c   m E   d     |%136|   d     h   c   |%135|             H   |%134| r M e           |%133| h               |%132| D             D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
          "14b %13 +-----------------+%138|   r r   r r     |%137| r   d r H c     |%136| h H     c r h r |%135|     m   h       |%134|     E           |%133|                 |%132|         C D H C |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
          "14b %13 +-----------------+%138|   r r   r r     |%137| r   d r H c     |%136| h H E   c r h r |%135|     m   h       |%134|                 |%133|                 |%132|         C D H C |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
          "14b %13 +-----------------+%138|   r r   r r     |%137| r   d r H c     |%136| h M E   c r h r |%135|   m e           |%134|                 |%133|                 |%132|         C D H C |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",

          "16w %13 +-----------------+%138| r     r r   r r |%137|     r m   r     |%136|     d   E H h   |%135|         c       |%134|   r   M         |%133|   h   e         |%132|   H           C |%131| C R     D R R R |%13 +-----------------+%13   a b c d e f g h%13",

          "20b %13 +-----------------+%138| r r r r   r r r |%137| d       r c     |%136|   D c d m   h   |%135| R h E e         |%134|     H           |%133|   R   M H   C R |%132|         R       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

          "18w %13 +-----------------+%138| r r H r r r r r |%137| c   m     d c   |%136| d M r C h       |%135| h   E e         |%134|             H   |%133|                 |%132| D   C         D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",

          "17w %13 +-----------------+%138| r r H r r r r r |%137| c   m     d c   |%136| d r     h       |%135| h M E C         |%134|     e           |%133|                 |%132| D   C       H D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",

          "4b %13 +-----------------+%138| r r r r r r r r |%137| d h c   d c h   |%136|   H   E         |%135|       m         |%134|                 |%133|             e D |%132| D R C M   C H R |%131| R   R R R R R   |%13 +-----------------+%13   a b c d e f g h%13",

          "16b %13 +-----------------+%138| r h r r r r r r |%137|   H     d c h   |%136|   C   E   d     |%135|     R           |%134|         M     D |%133| D     R r   R R |%132| e         H     |%131| R   R     R     |%13 +-----------------+%13   a b c d e f g h%13",

          "21w %13 +-----------------+%138|                 |%137| r r r     H     |%136|   d     h   h R |%135| c   d   r r     |%134| r C       c r   |%133| D e   r   E C   |%132|     H M D m     |%131| R R R R R R R   |%13 +-----------------+%13   a b c d e f g h%13",

          "37b %13 +-----------------+%138|       r r   r   |%137|                 |%136|             c   |%135| r             r |%134|       E   e     |%133| r               |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
          "37b %13 +-----------------+%138|       r r   r   |%137|                 |%136|             c   |%135| r             r |%134|       E   e     |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
          "21w %13 +-----------------+%138|                 |%137|   r r     r     |%136| r d   r c   E r |%135| R   h r R e     |%134|       R R   R   |%133|                 |%132|   H D     H   r |%131| C R           C |%13 +-----------------+%13   a b c d e f g h%13",

          "16b %13 +-----------------+%138| r r c   r     r |%137| h d   r m c h R |%136|       e   r     |%135|   r   r   C H   |%134|     H R E M d   |%133| C D         D   |%132| R R R     R R   |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
          "24b %13 +-----------------+%138|     c r r   r C |%137| d R   r R c R m |%136| R r r         r |%135|   h H           |%134|   C e     E   h |%133|       H D     R |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
          "35b %13 +-----------------+%138|       r r   r   |%137|               r |%136|   d         c R |%135| r E             |%134|           e     |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

          "2w %13 +-----------------+%138|     c   r c   r |%137| h d   r r   R d |%136|     X   e X   h |%135|               M |%134|                 |%133|   c X E   X c   |%132| H D r M   C r H |%131| R R C R R       |%13 +-----------------+%13   a b c d e f g h%13",
          "2w %13 +-----------------+%138|     c   r c   r |%137| h d   r r   R d |%136|     X   e X   h |%135|               r |%134|                 |%133|   c X E   X c   |%132| H D r M   C r H |%131| R R C R R       |%13 +-----------------+%13   a b c d e f g h%13",
          "2w %13 +-----------------+%138|     c   r c   r |%137| h d   r r   R d |%136|     X   e X   h |%135|               C |%134|                 |%133|   c X E   X c   |%132| H D r M   C r H |%131| R R C R R       |%13 +-----------------+%13   a b c d e f g h%13",
          "27w %13 +-----------------+%138|   r   d H r r r |%137| r   r c E R   r |%136|         h       |%135|                 |%134|     m           |%133|       r         |%132|     M e   R     |%131| R     R D   R R |%13 +-----------------+%13   a b c d e f g h%13", // Game 11697
          "27w %13 +-----------------+%138|   r   d H r r r |%137| r   r c   R   r |%136|         h   E   |%135|                 |%134|     m           |%133|       r         |%132|     M e   R     |%131| R     R D   R R |%13 +-----------------+%13   a b c d e f g h%13", // Game 11697
          "23w %13 +-----------------+%138| r r r     r r r |%137| r         c     |%136|   C c d         |%135|   m E d         |%134|                 |%133|   D         r   |%132| R   D   R e     |%131|   R R R     R R |%13 +-----------------+%13   a b c d e f g h%13", // Game 14129
          "27b %13 +-----------------+%138|   r   d H r r r |%137| r   r c E R   r |%136|         h       |%135|                 |%134|     m           |%133|       r         |%132|     M e   R     |%131| R     R D   R R |%13 +-----------------+%13   a b c d e f g h%13", // Game 11697
          "23b %13 +-----------------+%138| r r r     r r r |%137| r         c     |%136|   C c d         |%135|   m E d         |%134|                 |%133|   D         r   |%132| R   D   R e     |%131|   R R R     R R |%13 +-----------------+%13   a b c d e f g h%13", // Game 14129

          "27w %13 +-----------------+%138| r   r c c   r r |%137| d r   d   r     |%136| M e         m   |%135| h               |%134|     r H   E     |%133|                 |%132| D   C   D h   H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13", // Naveed
          "27w %13 +-----------------+%138| r   r c c   r r |%137| d r   d   r     |%136| M e         m   |%135| h               |%134|     r     E     |%133| R               |%132| D   C   D h   H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13", // Naveed
          "27w %13 +-----------------+%138| r   r c c   r r |%137| d r   d   r     |%136| M e         m   |%135| h               |%134|     r     E     |%133|   R             |%132| D   C   D h   H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13", // Naveed
          "27w %13 +-----------------+%138| r   r c c   r r |%137| d r   d   r     |%136| M e         m   |%135| h               |%134|     r     E     |%133|         H       |%132| D   C   D h   H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13", // Naveed

          "3b %13 +-----------------+%138| r r r c m r r r |%137| r h c d   d   r |%136|                 |%135|           e H   |%134|         E       |%133|   D     h       |%132| R   H   M D   R |%131| R R R C C R R R |%13 +-----------------+%13   a b c d e f g h%13",
          "3w %13 +-----------------+%138| r r r c m r r r |%137| r h c d   d   r |%136|                 |%135|           e H   |%134|         E       |%133|   D     h       |%132| R   H   M D   R |%131| R R R C C R R R |%13 +-----------------+%13   a b c d e f g h%13",
          "3b %13 +-----------------+%138|   E m   r       |%137|     H           |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
          "3b %13 +-----------------+%138|     E m   r     |%137|     H           |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      */
    };

    GenCaptures test = new GenCaptures();

    // Create the result array
    MoveList move_data = new MoveList(1000);

    // Run test positions
    for (String pos_text : text) {

      // Output the test position
      GameState position = new GameState(pos_text);
      System.out.println(position);

      test.genCaptures(position, move_data,false);
      System.out.println("Total moves: " + move_data.size());

      // Output resulting positions
      GameState temp = new GameState();
      System.out.println(move_data);

    }
    System.out.println(test.getStats());

  }


}
