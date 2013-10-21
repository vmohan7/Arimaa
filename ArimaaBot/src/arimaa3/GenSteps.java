package arimaa3;

/**
 * Generates any one or two step moves that meet given criteria
 * Also generates moves that guarantee the piece is NOT FROZEN
 *
 */
public class GenSteps extends ArimaaBaseClass {
  public GenSteps() {
  }

  private static final boolean use_debug = false;

  // Working variables for gen_moves
  private MoveList move_list_stack[] = new MoveList[5];
  private GameState gs_stack[] = new GameState[5];
  {
    for (int i = 0; i < gs_stack.length; i++) {
      gs_stack[i] = new GameState();
    }
    for (int i = 0; i < move_list_stack.length; i++) {
      move_list_stack[i] = new MoveList(1000);
    }
  }


  /**
   * Helper function to get set of player's bad trap squares
   * @param position GameState
   * @return long
   */
  private long getBadTrapSquares(GameState position) {
    // Remove illegal dest squares due to trap suicide
    long bad_trap_bb = 0L;
    long player_bb = position.colour_bb[position.player];
    for (int i = 0; i <= 3; i++) {
      long ptest_bb = TOUCH_TRAP[i] & player_bb;
      if (atMostOneBitSet(ptest_bb)) {
        bad_trap_bb |= TRAP[i];
      }
    }
    return bad_trap_bb;
  }

  /**
   * Generate all one step moves from p_start_bb ending at p_dest_bb
   * Piece is guaranteed not to be self-captured at p_dest_bb
   * Piece could still be frozen at the destination.
   * However other pieces may be captured in the process
   * @param initial_position GameState
   * @param move_list ArimaaMove[]
   * @param start_index int
   * @param p_start_bb long
   * @param p_dest_bb long
   * @return int
   */

  public void genOneStep(GameState initial_position, MoveList move_list,
    long p_start_bb, long p_dest_bb) {

    // Remove bad trap squares
    p_dest_bb &= ~getBadTrapSquares(initial_position);

    // Destination squares have to be empty
    p_dest_bb &= initial_position.empty_bb;

    initial_position.genSlideMoves(move_list, p_start_bb, p_dest_bb);
  }


  /**
   * Generates 1 step NF moves
   * @param initial_position GameState
   * @param move_list ArimaaMove[]
   * @param start_index int
   * @param p_start_bb long
   * @param p_dest_bb long
   * @return int
   */
  public void genOneStepNF(GameState initial_position, MoveList move_list,
    long p_start_bb, long p_dest_bb) {

    // Case 1: Slide the piece

    initial_position.genSlideMovesF(move_list, p_start_bb, p_dest_bb, false);

    // Case 2:
    // A valid p_start_bb piece is already on p_dest_bb but is frozen
    // Try to slide a player piece to unfreeze it

    initial_position.compute_tertiary_bitboards();
    long temp_bb = p_start_bb & p_dest_bb & initial_position.frozen_pieces_bb;
    while (temp_bb != 0) {
      long lsb2_bb = temp_bb & -temp_bb;
      temp_bb ^= lsb2_bb; // Remove the piece

      long help_dest_bb = touching_bb(lsb2_bb);

      genOneStep(initial_position, move_list, FULL_BB, help_dest_bb);
    }

  }

  /**
   * Generates all one or two step moves to move a piece from p_start_bb
   * to p_dest_bb. If a supporting step is used, it must come from p_support_bb
   * Piece is guaranteed not to be self-captured at p_dest_bb
   * However other pieces may be captured in the process
   *
   * @param initial_position GameState
   * @param move_list ArimaaMove[]
   * @param start_index int
   * @param p_start_bb long
   * @param p_dest_bb long
   * @param p_support_bb long
   * @return int
   */

  public void genTwoStep(GameState initial_position, MoveList move_list,
    long p_start_bb, long p_dest_bb) {

    // Preconditions

    // Keep only starting squares with player pieces
    p_start_bb &= initial_position.colour_bb[initial_position.player];

    // Case 1: Unassisted 1 step slide or 2 step push
    // Remove bad trap squares
    long bad_trap_bb = getBadTrapSquares(initial_position);
    long temp_dest_bb = p_dest_bb & ~bad_trap_bb;

    // Destination squares have to be empty
    // NOT TRUE for push moves!!!

    if (use_debug) {
      print_bitboard(p_start_bb, "Start");
      print_bitboard(temp_dest_bb, "Case 1 Dest");
    }
    // Enemy can only be pushed off of dest squares
    // Bad trap only valid for single step slides/push/pull moves
    move_list.clear();
    initial_position.genAllMoves(move_list,
      p_start_bb, temp_dest_bb, temp_dest_bb, FULL_BB);

    // Case 2: Unassisted 2 step slide
    // TODO bad trap bb are wrong for this case!!!
    // Piece can move in from two steps away!!!
    long dest2_bb = p_dest_bb & initial_position.empty_bb;
    long inbetween_bb = touching_bb(p_start_bb) & touching_bb(dest2_bb) &
      ~bad_trap_bb;

    // Start piece locations that have a hope of reaching
    long temp_start_bb = touching_bb(inbetween_bb) & p_start_bb;

    if (use_debug) {
      System.out.println("Case 2: Unassisted 2 step slide");
      print_bitboard(dest2_bb, "dest2_bb");
      print_bitboard(inbetween_bb, "inbetween");
      print_bitboard(temp_start_bb, "temp_start_bb");
    }
    // Play the first slide move
    // TODO update bad trap bb???
    move_list_stack[0].clear();
    initial_position.genSlideMoves(move_list_stack[0], temp_start_bb,
      inbetween_bb);
    for (ArimaaMove move : move_list_stack[0]) {
      gs_stack[0].play(move, initial_position);

      // Setup for the second slide move
      long new_start_bb = move.getSetBits() & inbetween_bb;
      long illegal_dest_bb = move.getSetBits() & p_start_bb;
      long bad_trap_bb2 = getBadTrapSquares(gs_stack[0]);
      long td_bb = dest2_bb & ~illegal_dest_bb & ~bad_trap_bb2;

      if (use_debug) {
        System.out.println(move);
        print_bitboard(new_start_bb, "new_start_bb");
        print_bitboard(td_bb, "td_bb");
      }
      move_list_stack[1].clear();
      gs_stack[0].genSlideMoves(move_list_stack[1], new_start_bb, td_bb);
      for (ArimaaMove move2 : move_list_stack[1]) {
        // Combine the two moves and record the result
        move_list.getMove().add(move, move2);
      }
    }

    // Case Three: Assistance Required
    long temp_dest2_bb = touching_bb(p_start_bb) & p_dest_bb;
    long temp_start2_bb = touching_bb(p_dest_bb) & p_start_bb;

    if (use_debug) {
      print_bitboard(temp_start2_bb, "Start 3");
      print_bitboard(temp_dest2_bb, "Dest 3");
    }
    initial_position.compute_tertiary_bitboards();

    // Case Three A: vacate destination square
    move_list_stack[0].clear();
    initial_position.genSlideMoves(move_list_stack[0], temp_dest2_bb, FULL_BB);
    for (ArimaaMove move : move_list_stack[0]) {
      gs_stack[0].play(move, initial_position);

      // Setup for slide move
      long new_dest_bb = move.getSetBits() & ~initial_position.empty_bb;
      long new_start_bb = touching_bb(new_dest_bb) & p_start_bb; // TODO oscillation??

      move_list_stack[1].clear();
      gs_stack[0].genSlideMoves(move_list_stack[1], new_start_bb, new_dest_bb);
      for (ArimaaMove move2 : move_list_stack[1]) {
        // Combine the two moves and record the result
        move_list.getMove().add(move, move2);
      }

    }

    // Case 3b: support trap square
    // Figure out if a support move is even possible
    long supportable_trap_bb = bad_trap_bb & temp_dest2_bb;
    long temp_dest3_bb = touching_bb(supportable_trap_bb);

    move_list_stack[0].clear();
    initial_position.genSlideMoves(move_list_stack[0], FULL_BB, temp_dest3_bb);

    // Decided to allow all possible supporting moves

    for (ArimaaMove move : move_list_stack[0]) {
      gs_stack[0].play(move, initial_position);

      // Setup for slide move
      long temp7_bb = move.getSetBits() & initial_position.empty_bb;
      long new_dest_bb = touching_bb(temp7_bb) & TRAP_SQUARES;
      long new_start_bb = touching_bb(new_dest_bb) & p_start_bb;

      move_list_stack[1].clear();
      gs_stack[0].genSlideMoves(move_list_stack[1], new_start_bb, new_dest_bb);
      for (ArimaaMove move2 : move_list_stack[1]) {
        // Combine the two moves and record the result
        move_list.getMove().add(move, move2);
      }

    }

    // Case 3c: unfreeze frozen p_start_bb piece
    long temp_dest4_bb = p_dest_bb & initial_position.empty_bb;
    long temp_start4_bb = p_start_bb & touching_bb(temp_dest4_bb);
    long frozen_bb = temp_start2_bb & initial_position.frozen_pieces_bb;
    long support_dest_bb = touching_bb(frozen_bb) & initial_position.empty_bb;

    move_list_stack[0].clear();
    initial_position.genSlideMoves(move_list_stack[0], FULL_BB, support_dest_bb);
    for (ArimaaMove move : move_list_stack[0]) {
      gs_stack[0].play(move, initial_position);

      // Setup for slide move
      long temp7_bb = move.getSetBits() & initial_position.empty_bb;
      long new_start_bb = touching_bb(temp7_bb) & frozen_bb;
      long new_dest_bb = p_dest_bb;

      move_list_stack[1].clear();
      gs_stack[0].genSlideMoves(move_list_stack[1], new_start_bb, new_dest_bb);
      for (ArimaaMove move2 : move_list_stack[1]) {
        // Combine the two moves and record the result
        move_list.getMove().add(move, move2);
      }
    }

  }


  /**
   * Generates all two steps moves
   * Target piece is not captured on dest square
   * Target piece is not frozen on dest square
   *
   * @param initial_position GameState
   * @param move_list ArimaaMove[]
   * @param start_index int
   * @param p_start_bb long
   * @param p_dest_bb long
   * @return int
   */
  public void genTwoStepNF(GameState initial_position, MoveList move_list,
    long p_start_bb, long p_dest_bb) {

    // Case 1:
    // A valid p_start_bb piece is already on p_dest_bb but is frozen
    // Try to slide a player piece to unfreeze it

    initial_position.compute_tertiary_bitboards();
    long temp_bb = p_start_bb & p_dest_bb & initial_position.frozen_pieces_bb;
    while (temp_bb != 0) {
      long lsb2_bb = temp_bb & -temp_bb;
      temp_bb ^= lsb2_bb; // Remove the piece

      long help_dest_bb = touching_bb(lsb2_bb);

      genTwoStep(initial_position, move_list, FULL_BB, help_dest_bb);
    }

    // Case 2:
    // Use one step to move piece, other step for support piece, order does matter
    GameState temp_gs = gs_stack[2];
    move_list_stack[2].clear();
    initial_position.genSlideMovesF(move_list_stack[2], p_start_bb, p_dest_bb, true);
    for (ArimaaMove move : move_list_stack[2]) {
      temp_gs.play(move, initial_position);

      // All squares touching the slide piece destination square
      long slide_dest_bb = move.getSetBits() & initial_position.empty_bb;
      long help_dest_bb = touching_bb(slide_dest_bb);

      move_list_stack[3].clear();
      genOneStep(temp_gs, move_list_stack[3], FULL_BB, help_dest_bb);
      for (ArimaaMove move2 : move_list_stack[3]) {
        // Combine the two moves and record the result
        move_list.getMove().add(move, move2);
      }

      // Special case to consider
      // Sometimes helper piece has to slide first
      // h x -
      //   R C d
      // For the Rabbit to get to x, unfrozen, the C has to move first

      // Candidates must be
      // 1) touching start square
      // 2) Unfrozen before slide
      // 3) Frozen after slide
      temp_gs.compute_tertiary_bitboards();

      long slide_start_bb = move.getSetBits() & ~initial_position.empty_bb;
      long help_start_bb = touching_bb(slide_start_bb) &
        ~initial_position.frozen_pieces_bb &
        temp_gs.frozen_pieces_bb &
        initial_position.colour_bb[initial_position.player];
      // This is really rare, so speed is not so critical
      if (help_start_bb != 0) {

//        print_bitboard(help_start_bb, "help_start_bb");
//        print_bitboard(help_dest_bb, "help_dest_bb");
//        print_bitboard(slide_start_bb, "slide_start_bb");
//       print_bitboard(slide_dest_bb, "slide_dest_bb");

        // 1) Generate the helper slide
        move_list_stack[3].clear();
        initial_position.genSlideMoves(move_list_stack[3], help_start_bb,
          help_dest_bb);
        GameState temp2_gs = gs_stack[3];

        for (ArimaaMove move6 : move_list_stack[3]) {
          temp2_gs.play(move6, initial_position);

          // Generate the initial slide (May NOT be legal anymore!!!)
          move_list_stack[4].clear();
          temp2_gs.genSlideMoves(move_list_stack[4], slide_start_bb,
            slide_dest_bb);
          for (ArimaaMove move7 : move_list_stack[4]) {
            // Combine the two moves and record the result
            move_list.getMove().add(move6, move7);
          }

        }
      }
    }

    // Case 3:
    // Use both steps to move piece
    // For now call basic two step routine until it is fully debugged
    move_list_stack[2].clear();
    genTwoStep(initial_position, move_list_stack[2], p_start_bb, p_dest_bb);
    for (ArimaaMove move : move_list_stack[2]) {
      temp_gs.play(move, initial_position);
      temp_gs.compute_tertiary_bitboards();
      // For now, move is ok, if a non frozen piece is in the dest set
      if ((~temp_gs.frozen_pieces_bb & p_dest_bb & move.getSetBits()) != 0) {
//        System.out.println("Case 3\n" + move);
        move_list.getMove().copy(move);
      }

    }

  }

  public static void main(String args[]) {

    String text[] = {
      /*
         "16b %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
         "16w %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4

         "26b %13 +-----------------+%138| r r r r r   r r |%137| M E d h         |%136| h D C c c   R H |%135| R R R R r m   e |%134|             H R |%133|                 |%132|         R       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
         "46b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|         r   r   |%133| r   r   E   D r |%132| R M e R m     R |%131|   h R           |%13 +-----------------+%13   a b c d e f g h%13",
         "61w %13 +-----------------+%138| r H r           |%137|   m E           |%136|   r R d c   r   |%135|   R   R R   R r |%134| R D e R   r C r |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
         "58b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|   C         M r |%133| R e r E         |%132|     D r r r   R |%131|     R R R R R   |%13 +-----------------+%13   a b c d e f g h%13",
         "40b %13 +-----------------+%138|         r   r r |%137| r             c |%136|         D       |%135|   H   R         |%134|           M     |%133|   m           H |%132|   E e R       r |%131|             R R |%13 +-----------------+%13   a b c d e f g h%13",
         "63w %13 +-----------------+%138| r r     r   H   |%137|     c r R h   r |%136|   d       H R   |%135|             e   |%134|   h E       r C |%133|   d R c     R   |%132|     R           |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      */
      "16w %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|     e   M       |%134|                 |%133|   R   R         |%132|         d       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
      "16w %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|     e   M       |%134|                 |%133|         R        |%132|        d       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
      "16w %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|     e   M       |%134|       H         |%133|       d          |%132|        R       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
      "16w %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|           R     |%133|       d          |%132|        R C d   |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
      "16w %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|          R      |%133|       d          |%132|        R C     |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4

    };

    GenSteps test = new GenSteps();

    // Create the result array
    MoveList move_data = new MoveList(100000);

    // Run test positions
    for (String pos_text : text) {

      // Output the test position
      GameState position = new GameState(pos_text);
      System.out.println(position);

      move_data.clear();
      test.genTwoStep(position, move_data, FULL_BB, RANK_3);
      System.out.println("Total moves: " + move_data.size());

      // Output resulting positions
      for (ArimaaMove move : move_data) {
        System.out.println(move);
      }

    }

  }


}
