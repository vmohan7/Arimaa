package arimaa3;

import ai_util.*;
import java.util.*;

public class GameState extends ArimaaBaseClass {
  public GameState() {
  }


  /**
   * Piece ID's are as follows
   * 00 White Rabbit BB
   * 01 Black Rabbit BB
   * 02 White Cat BB
   * 03 Black Cat BB
   * 04 White Dog BB
   * 05 Black Dog BB
   * 06 White Horse BB
   * 07 Black Horse BB
   * 08 White Camel BB
   * 09 Black Camel BB
   * 10 White Elephant BB
   * 11 Black Elephant BB
   *
   * Player ID's are as follows
   * 00 White
   * 01 Black
   */

  // This is the entire gamestate
  // Stuff everybody can use
  public long piece_bb[] = new long[12];
  public long colour_bb[] = new long[2];
  public long stronger_enemy_bb[] = new long[12];
  public long empty_bb;
  public int total_steps;
  public int player;
  public int enemy;

  // These are only calculated if compute_tertiary_bitboards() is called
  public long frozen_pieces_bb;
  public long dominated_pieces_bb;
  public long barely_dominated_pieces_bb;

  public void copy(GameState orig) {
    for (int i = 0; i < 12; i++) {
      piece_bb[i] = orig.piece_bb[i];
      stronger_enemy_bb[i] = orig.stronger_enemy_bb[i];
    }
    colour_bb[0] = orig.colour_bb[0];
    colour_bb[1] = orig.colour_bb[1];
    empty_bb = orig.empty_bb;
    total_steps = orig.total_steps;
    player = orig.player;
    enemy = orig.enemy;
    frozen_pieces_bb = orig.frozen_pieces_bb;
    dominated_pieces_bb = orig.dominated_pieces_bb;
  }

  public void playPASS(GameState orig) {
    copy(orig);
    this.total_steps += getStepsRemaining();
    player ^= 0x01;
    enemy ^= 0x01;
  }

  /**
   * Reflects position about vertical axis
   * Side to move remains the same
   * @param orig GameState
   */
  public void mirror() {

    // Mirror the position
    for (int i = 0; i < 12; i++) {
      long temp_bb = piece_bb[i];
      piece_bb[i] = 0;

      while (temp_bb != 0) {
        long lsb_bb = temp_bb & -temp_bb;
        temp_bb ^= lsb_bb;
        int index = Util.FirstOne(lsb_bb);
        piece_bb[i] |= 1L << getMirrorIndex(index);
      }
    }

    compute_secondary_bitboards();
  }

  /**
   * Computes the mirrored index
   * Reflects board about vertical axis
   * @param index int
   * @return int
   */
  private int getMirrorIndex(int index) {
    assert (index >= 0 && index <= 63);
    int row = index >> 3;
    int col = index & 0x07;
    int result = row * 8 + (7 - col);
    return result;
  }


  /**
   * Computes the rotated index
   * Rotates board 180 degrees
   * @param index int
   * @return int
   */
  private int getRotatedIndex(int index) {
    assert (index >= 0 && index <= 63);
    int row = index >> 3;
    int col = index & 0x07;
    int result = (7 - row) * 8 + (7 - col);
    return result;
  }


  /**
   * Rotate the board 180 degrees
   * Changes side to move
   * Swaps all piece colours
   * @param orig GameState
   */
  public void rotate() {
    // Change side to move
    total_steps = total_steps ^ 0x04;

    // Rotate the position
    for (int i = 0; i < 12; i += 2) {

      long temp_bb = piece_bb[i];
      long temp2_bb = piece_bb[i + 1];
      piece_bb[i] = 0;
      piece_bb[i + 1] = 0;

      while (temp_bb != 0) {
        long lsb_bb = temp_bb & -temp_bb;
        temp_bb ^= lsb_bb;
        int index = Util.FirstOne(lsb_bb);
        piece_bb[i + 1] |= 1L << getRotatedIndex(index);
      }

      while (temp2_bb != 0) {
        long lsb_bb = temp2_bb & -temp2_bb;
        temp2_bb ^= lsb_bb;
        int index = Util.FirstOne(lsb_bb);
        piece_bb[i] |= 1L << getRotatedIndex(index);
      }
    }

    compute_secondary_bitboards();
  }


  // Creates the position based on the setup text strings that start the games
  public GameState(String white_setup_text, String black_setup_text) {

    this.total_steps = 16;

    // Clear the piece bitboards
    for (int i = 0; i <= 11; i++) {
      piece_bb[i] = 0;
    }


    String full = white_setup_text + " " + black_setup_text;
    StringTokenizer tokenizer = new StringTokenizer(full);

    while (tokenizer.hasMoreTokens()) {
      String piece_token = tokenizer.nextToken();

      // Ignore move count tokens
      if (piece_token.equals("1w") || piece_token.equals("1b")) {
        continue;
      }

      // Convert the text move to numbers
      int piece_type = getPieceType(piece_token.substring(0, 1));
      String col_text = "abcdefgh";
      int col = col_text.indexOf(piece_token.substring(1, 2));
      int row = Integer.parseInt(piece_token.substring(2, 3)) - 1;

      // Populate the bitboard
      int bb_index = row * 8 + col;
      piece_bb[piece_type] ^= 1L << bb_index;

    }

    compute_secondary_bitboards();

  }



  /**
   * Constructs the game state from text string
   * Format is same as used by bot interface program
   * @param text String
   */
  public GameState(String text) {

    int space_pos = text.indexOf(" ", 0);
    int ppos = text.indexOf("%", 0);

    String turn_text = text.substring(0, space_pos - 1);
    String player_text = text.substring(space_pos - 1, space_pos);
    String move_text = text.substring(space_pos, ppos);
    String board_text = text.substring(ppos, text.length());

    // TODO: Handle partial move text

    // Determine total steps
    int temp_turn = Integer.parseInt(turn_text);
    int temp_player = (player_text.equals("w")) ? 0 : 1;
    this.total_steps = temp_turn * 8 + temp_player * 4;

    // Clear the piece bitboards
    for (int i = 0; i <= 11; i++) {
      piece_bb[i] = 0;
    }

    // Populate the piece bitboards
    for (int row = 7; row >= 0; row--) {
      for (int col = 0; col <= 7; col++) {
        int bb_index = row * 8 + col;
        int text_index = 29 + 2 * col + (7 - row) * 23;
        String letter = board_text.substring(text_index, text_index + 1);
        int piece_type = getPieceType(letter);
        if (piece_type != -1) {
          piece_bb[piece_type] ^= 1L << bb_index;
        }
      }
    }

    compute_secondary_bitboards();

    assert (clear_traps() == false);
  }


  public int getIndex(int row, int col) {
    return row * 8 + col;
  }

  /**
   * Sets a square to designated piece type
   * Note: -1 is empty
   */
  public void setPieceType(int index, int piece_type) {

    assert (index >= 0 && index <= 63);
    assert (piece_type >= -1 && piece_type <= 11);

    // Clear the square
    long index_bb = 1L << index;
    for (int i = 0; i <= 11; i++) {
      piece_bb[i] &= ~index_bb;
    }

    // Set the square
    if (piece_type != -1) {
      piece_bb[piece_type] |= index_bb;
    }

    compute_secondary_bitboards();
    clear_trap_squares();

  }


  /**
   * Removes all hanging pieces from trap squares
   */
  public void clear_trap_squares() {
    for (int i = 0; i < 4; i++) {
      for (int c = 0; c < 2; c++) {
        if ((TRAP[i] & colour_bb[c]) != 0 &&
          (TOUCH_TRAP[i] & colour_bb[c]) == 0) {
          int pt = getPieceType(TRAP[i]);
          piece_bb[pt] &= ~TRAP[i];
        }
      }
    }

    compute_secondary_bitboards();
  }

  /**
   * Get the piece type of a square
   * @param index int
   * @return int
   */
  public int getPieceType(int index) {
    return getPieceType(1L << index);
  }


  // This table tells if the game is over by rabbit goal
  // An enemy rabbit can be in the goal, if there are steps remaining!
  private static final boolean table_is_game_over[] = {
    //G4  G3  G2  G1  S4  S3  S2  S1
    FL, FL, FL, FL, FL, FL, FL, FL, // G=F, S=F
    TR, FL, FL, FL, TR, TR, TR, TR, // G=F, S=T
    TR, TR, TR, TR, TR, FL, FL, FL, // G=T, S=F
    TR, TR, TR, TR, TR, TR, TR, TR, // G=T, S=T
  };

  private static int NA = SCORE_UNKNOWN;
  private static int WN = +SCORE_MATE;
  private static int LS = -SCORE_MATE;

  private static final int table_get_game_result[] = {
    //G4  G3  G2  G1  S4  S3  S2  S1
    NA, NA, NA, NA, NA, NA, NA, NA, // G=F, S=F
    WN, NA, NA, NA, LS, WN, WN, WN, // G=F, S=T
    LS, WN, WN, WN, WN, NA, NA, NA, // G=T, S=F
    WN, WN, WN, WN, WN, WN, WN, WN, // G=T, S=T
  };

  /**
   * Considers only current gamestate, no previous move required
   * @return boolean
   */

  private static final boolean use_no_rabbits_is_a_loss = true;

  public boolean isGameOver() {

    // Test if there are no rabbits left
    if ((piece_bb[0] | piece_bb[1]) == 0) {
      return true;
    }

    // Test for rabbit goal / all rabbit capture
    int gold_goal, silver_goal;

    if (use_no_rabbits_is_a_loss) {
      gold_goal = (((piece_bb[0] & RANK_8) != 0) || (piece_bb[1] == 0)) ? 1 : 0;
      silver_goal = (((piece_bb[1] & RANK_1) != 0) || (piece_bb[0] == 0)) ? 1 :
        0;
    } else {
      gold_goal = ((piece_bb[0] & RANK_8) != 0) ? 1 : 0;
      silver_goal = ((piece_bb[1] & RANK_1) != 0) ? 1 : 0;
    }
    int steps = total_steps & 0x07;
    int index = steps + (gold_goal << 4) + (silver_goal << 3);
    return table_is_game_over[index];
  }

  /**
   * Returns result of game SCORE_MATE,SCORE_DRAW,-SCORE_MATE
   * From perspective of side to move at start of *previous* step
   * @return int
   */
  public int getGameResult() {
    assert (isGameOver() == true);

    // Test if there are no rabbits left
    // Rare case possible no rabbits could win!
    if (!use_no_rabbits_is_a_loss) {
      if ((piece_bb[0] | piece_bb[1]) == 0) {
        return SCORE_DRAW;
      }
    }

    // Test for rabbit goal / all rabbit capture
    int gold_goal, silver_goal;
    if (use_no_rabbits_is_a_loss) {
      gold_goal = (((piece_bb[0] & RANK_8) != 0) || (piece_bb[1] == 0)) ? 1 : 0;
      silver_goal = (((piece_bb[1] & RANK_1) != 0) || (piece_bb[0] == 0)) ? 1 :
        0;
    } else {
      gold_goal = ((piece_bb[0] & RANK_8) != 0) ? 1 : 0;
      silver_goal = ((piece_bb[1] & RANK_1) != 0) ? 1 : 0;
    }
    int steps = total_steps & 0x07;
    int index = steps + (gold_goal << 4) + (silver_goal << 3);

    assert (table_get_game_result[index] != NA);
    return table_get_game_result[index];

  }


  /**
   * Returns the side to move
   * @return int
   */
  public int getSideToMove() {
    return player;
  }

  /**
   * Returns the turn number
   * @return int
   */
  public int getTurnNumber() {
    return (total_steps / 8);
  }

  /**
   * Returns the steps remaining in the current turn
   * @return int
   */
  public int getStepsRemaining() {
    return 4 - (total_steps & 0x03);
  }

  /**
   * Plays the given move on the provided previous gamestate.
   * Also computes the tertiary bitboards.
   * This is alot slower than play() on its own.
   * @param move ArimaaMove
   * @param prev GameState
   */

  public void playFull(ArimaaMove move, GameState prev) {
    play(move, prev);
    compute_tertiary_bitboards();
  }

  /**
   * Plays the given move on the provided previous gamestate.
   * Also tests for any captured pieces
   * Also computes tertiary bitboards
   * @param move ArimaaMove
   * @param prev GameState
   */
  public void playFullClear(ArimaaMove move, GameState prev) {
    play(move, prev);
    boolean is_capture = clear_traps();
    if (is_capture) {
      compute_secondary_bitboards();
    }
    compute_tertiary_bitboards();
  }

  public void playFullClear(ArimaaMove move, ArimaaMove move2, GameState prev) {
    play(move, prev);
    boolean is_capture = clear_traps();
    if (is_capture) {
      compute_secondary_bitboards();
    }

    // Need to compute secondary bitboards each time
    play(move2, prev);
    is_capture = clear_traps();
    if (is_capture) {
      compute_secondary_bitboards();
    }

    compute_tertiary_bitboards();
  }

  /**
   * Tests if a piece has been captured
   * Returns true iff a piece has been captured
   * @return boolean
   */

  private boolean clear_traps() {
    boolean result = false;
    for (int i = 0; i < 4; i++) {
      for (int c = 0; c <= 1; c++) {
        if ((TRAP[i] & colour_bb[c]) != 0) {
          if ((TOUCH_TRAP[i] & colour_bb[c]) == 0) {
            // Piece has been captured
            result = true;
            int pt = this.getPieceType(TRAP[i]);
            piece_bb[pt] ^= TRAP[i];
          }
        }
      }
    }

    return result;
  }

  /**
   * Plays the given move on the provided previous gamestate
   * @param move ArimaaMove
   * @param prev GameState
   */
  public void play(ArimaaMove move, GameState prev) {

    assert (prev.getStepsRemaining() >= move.steps);

    // XOR all twelve piece BBs
    piece_bb[11] = prev.piece_bb[11] ^ move.piece_bb[11];
    piece_bb[10] = prev.piece_bb[10] ^ move.piece_bb[10];
    long c1 = piece_bb[11];
    long c0 = piece_bb[10];
    stronger_enemy_bb[8] = c1;
    stronger_enemy_bb[9] = c0;

    piece_bb[9] = prev.piece_bb[9] ^ move.piece_bb[9];
    piece_bb[8] = prev.piece_bb[8] ^ move.piece_bb[8];
    c1 |= piece_bb[9];
    c0 |= piece_bb[8];
    stronger_enemy_bb[6] = c1;
    stronger_enemy_bb[7] = c0;

    piece_bb[7] = prev.piece_bb[7] ^ move.piece_bb[7];
    piece_bb[6] = prev.piece_bb[6] ^ move.piece_bb[6];
    c1 |= piece_bb[7];
    c0 |= piece_bb[6];
    stronger_enemy_bb[4] = c1;
    stronger_enemy_bb[5] = c0;

    piece_bb[5] = prev.piece_bb[5] ^ move.piece_bb[5];
    piece_bb[4] = prev.piece_bb[4] ^ move.piece_bb[4];
    c1 |= piece_bb[5];
    c0 |= piece_bb[4];
    stronger_enemy_bb[2] = c1;
    stronger_enemy_bb[3] = c0;

    piece_bb[3] = prev.piece_bb[3] ^ move.piece_bb[3];
    piece_bb[2] = prev.piece_bb[2] ^ move.piece_bb[2];
    c1 |= piece_bb[3];
    c0 |= piece_bb[2];
    stronger_enemy_bb[0] = c1;
    stronger_enemy_bb[1] = c0;

    piece_bb[1] = prev.piece_bb[1] ^ move.piece_bb[1];
    piece_bb[0] = prev.piece_bb[0] ^ move.piece_bb[0];
    c1 |= piece_bb[1];
    c0 |= piece_bb[0];

    colour_bb[0] = c0;
    colour_bb[1] = c1;

    // Update the non bitboard stuff
    total_steps = prev.total_steps + move.steps;

    empty_bb = ~(c0 | c1);

    player = (total_steps >> 2) & 0x01;
    enemy = player ^ 0x01;
  }


  /**
   * Computes the secondary bitboard
   * These are computed automatically by play().
   * This need to be called by the constructors.
   */
  private void compute_secondary_bitboards() {

    // Need to initialize once to prevent illegal array access
    for (int i = 0; i <= 3; i++) {
      TRAP_pt[i] = 0;
    }

    // create the colour bb
    colour_bb[0] = piece_bb[0] | piece_bb[2] | piece_bb[4] | piece_bb[6] |
      piece_bb[8] | piece_bb[10];
    colour_bb[1] = piece_bb[1] | piece_bb[3] | piece_bb[5] | piece_bb[7] |
      piece_bb[9] | piece_bb[11];

    // create white stronger enemy bb
    stronger_enemy_bb[10] = 0L;
    stronger_enemy_bb[8] = piece_bb[11];
    stronger_enemy_bb[6] = stronger_enemy_bb[8] | piece_bb[9];
    stronger_enemy_bb[4] = stronger_enemy_bb[6] | piece_bb[7];
    stronger_enemy_bb[2] = stronger_enemy_bb[4] | piece_bb[5];
    stronger_enemy_bb[0] = stronger_enemy_bb[2] | piece_bb[3];

    // create black stronger enemy bb
    stronger_enemy_bb[11] = 0L;
    stronger_enemy_bb[9] = piece_bb[10];
    stronger_enemy_bb[7] = stronger_enemy_bb[9] | piece_bb[8];
    stronger_enemy_bb[5] = stronger_enemy_bb[7] | piece_bb[6];
    stronger_enemy_bb[3] = stronger_enemy_bb[5] | piece_bb[4];
    stronger_enemy_bb[1] = stronger_enemy_bb[3] | piece_bb[2];

    empty_bb = ~(colour_bb[0] | colour_bb[1]);

    player = (total_steps >> 2) & 0x01;
    enemy = player ^ 0x01;

  }

  /**
   * Computes frozen and dominated piece bb
   * Also computes bad trap bb
   */
  public void compute_tertiary_bitboards() {
    frozen_pieces_bb = 0L; // All frozen pieces
    dominated_pieces_bb = 0L; // All dominated pieces
    barely_dominated_pieces_bb = 0L; // All dominated by just stronger piece

    for (int i = 0; i <= 9; i++) {

      // Compute dominated/frozen
      int colour = i & 0x01;
      long dominated_sq_bb = touching_bb(stronger_enemy_bb[i]);
      long frozen_sq_bb = ~touching_bb(colour_bb[colour]) &
        dominated_sq_bb;
      dominated_pieces_bb |= piece_bb[i] & dominated_sq_bb;
      frozen_pieces_bb |= piece_bb[i] & frozen_sq_bb;

      // Compute barely dominated pieces
      int pt = i + 3 - colour*2;
      long barely_dominated_sq_bb = touching_bb(piece_bb[pt]);
      barely_dominated_pieces_bb |= piece_bb[i] & barely_dominated_sq_bb;
    }


  }


  /**
   * Returns piece strength
   * 0 = Rabbit
   * 1 = Cat
   * 2 = Dog
   * 3 = Horse
   * 4 = Camel
   * 5 = Elephant
   * @param piece_type int
   * @return int
   */

  public int getPieceStrength(long target_bb) {
    assert (atMostOneBitSet(target_bb));
    int piece_type = getPieceType(target_bb);
    return getPieceStrength(piece_type);
  }

  public int getPieceStrength(int piece_type) {
    assert (piece_type >= 0 && piece_type <= 11);
    return piece_type >> 1;
  }

  public int getWeakestPieceStrength(long target_bb) {

    int weakest_strength = 20;

    while (target_bb != 0) {
      long lsb_bb = target_bb & -target_bb;
      target_bb ^= lsb_bb; // Remove the piece

      int piece_strength = getPieceStrength(lsb_bb);
      if (piece_strength < weakest_strength) {
        weakest_strength = piece_strength;
      }
    }

    assert (weakest_strength != 20);
    return weakest_strength;

  }


  /**
   * Returns the piece type of the target bb
   * @param target_bb long
   * @return int
   */
  public int getPieceType(long target_bb) {
    // Speed is critical. Anything to improve this function will help
    for (int i = 0; i <= 11; i++) {
      if ((piece_bb[i] & target_bb) != 0) {
        return i;
      }
    }

    return -1; // The square is empty
  }

  public int getPieceType(long target_bb, int colour) {
    // Speed is critical. Anything to improve this function will help
    for (int i = colour; i <= 11; i += 2) {
      if ((piece_bb[i] & target_bb) != 0) {
        return i;
      }
    }

    assert (false);
    return -1;
  }


  // Working variables for genMove routines
  private int TRAP_pt[] = new int[4]; // Type of piece *ON* the trap
  private long TRAP_protect[] = new long[4]; // Location of piece protecting trap
  private long player_suicide_bb; // Set of traps with one player piece touching
  private long enemy_suicide_bb; // Set of traps with one enemy piece touching

  /**
   * Determines all potential suicides for both colours
   */
  void calculate_trap_info() {
    player_suicide_bb = 0L; // All traps where player suicide is possible
    enemy_suicide_bb = 0L; // All traps where enemy suicide is possible

    for (int i = 0; i <= 3; i++) {
      // Clear protect_bb
      TRAP_protect[i] = 0L;

      long ptest_bb = TOUCH_TRAP[i] & colour_bb[player];
      if ((ptest_bb & (ptest_bb - 1)) == 0) { // Only 1 player piece
        // touching trap
        // Add trap to player suicide bb
        player_suicide_bb |= TRAP[i];

        // If trap contains player piece
        if ((TRAP[i] & colour_bb[player]) != 0) {
          TRAP_pt[i] = getPieceType(TRAP[i], player);
          TRAP_protect[i] = ptest_bb;
          // RULE: if test_bb moves then piece_type on TRAP[i] is
          // captured.
          // Note: The case for the other colour is mutually
          // exclusive
        }
      }

      long etest_bb = TOUCH_TRAP[i] & colour_bb[enemy];
      if ((etest_bb & (etest_bb - 1)) == 0) { // Only 1 enemy piece
        // touching trap
        // Add trap to enemy suicide bb
        enemy_suicide_bb |= TRAP[i];

        // If trap contains enemy piece
        if ((TRAP[i] & colour_bb[enemy]) != 0) {
          TRAP_pt[i] = getPieceType(TRAP[i], enemy);
          TRAP_protect[i] = etest_bb;
          // RULE: if test_bb moves then piece_type on TRAP[i] is
          // captured.
          // Note: The case for the other colour is mutually
          // exclusive
        }
      }

    }
  }

  /**
   * Generates all valid slide, push, pull and pass moves for current position
   * @param result ArimaaMove[]
   * @param cur_index int
   * @param p_start_bb long
   * @param p_dest_bb long
   * @return int
   */
  public void GENERATE_MOVES(MoveList result, long p_start_bb, long p_dest_bb) {
    int steps_remaining = getStepsRemaining();

    // Empty the move list
//    result.clear();

    // Handle pass move
    if (steps_remaining != 4) {
      ArimaaMove move = result.getMove();
      move.clear();
      move.steps = steps_remaining;
    }

    // Handle piece moves
    if (steps_remaining > 1) {
      genAllMoves(result, p_start_bb, p_dest_bb, FULL_BB, FULL_BB);
    } else {
      genSlideMoves(result, p_start_bb, p_dest_bb);
    }
  }

  // Working variables for genSlideMoves
  private long temp_player_bb[] = new long[6];


  // Generates all 1 step slide moves
  public void genSlideMoves(MoveList result, long p_start_bb, long p_dest_bb) {

    // Phase Zero
    // Create temp player piece bb
    temp_player_bb[0] = piece_bb[player + 0];
    temp_player_bb[1] = piece_bb[player + 2];
    temp_player_bb[2] = piece_bb[player + 4];
    temp_player_bb[3] = piece_bb[player + 6];
    temp_player_bb[4] = piece_bb[player + 8];
    temp_player_bb[5] = piece_bb[player + 10];

    // Phase 1
    // Determine all possible self captures on traps
    // Determine all traps where player suicide is possible
    player_suicide_bb = 0L;

    for (int i = 0; i <= 3; i++) {

      long ptest_bb = TOUCH_TRAP[i] & colour_bb[player];
      // if exactly one player piece touching trap
      if (((ptest_bb & (ptest_bb - 1)) == 0) && (ptest_bb != 0)) {

        // Add trap to player suicide bb
        player_suicide_bb |= TRAP[i];

        // If trap contains player piece
        // AND touching piece is member of p_start_bb
        if ((TRAP[i] & colour_bb[player]) != 0 && ((ptest_bb & p_start_bb) != 0)) {

          int trap_pt = getPieceType(TRAP[i], player);
          int player_pt = getPieceType(ptest_bb, player);

          // Need to remove piece from temp_player_bb
          temp_player_bb[player_pt >> 1] ^= ptest_bb;

          // RULE: if ptest_bb moves then piece_type on TRAP[i] is
          // captured.
          long dest_bb = touching_bb(ptest_bb,
            player_pt) & empty_bb & p_dest_bb;
          while (dest_bb != 0) {
            long lsb2_bb = dest_bb & -dest_bb;
            dest_bb ^= lsb2_bb; // Remove the piece

            // Record the move
            ArimaaMove move = result.getMove();
            move.clear();

            // Handles moving the piece
            move.piece_bb[player_pt] = ptest_bb | lsb2_bb;

            // Handles the captured piece, need to XOR in case
            // piece type is the same!
            move.piece_bb[trap_pt] ^= TRAP[i];

            // Record number of steps used
            move.steps = 1;

//            System.out.println("SLIDE DEBUG 1\n"+move);
          }
        }
      }
    }

    // Phase 2
    // Generate all remaining slide moves
    for (int pt = player, et = enemy; pt <= 11; pt += 2, et += 2) {

      // Get all unfrozen player pieces of type pt
      long temp_bb = temp_player_bb[pt >> 1] & p_start_bb;
      long unfrozen_sq_bb = touching_bb(colour_bb[player]) |
        ~touching_bb(stronger_enemy_bb[pt]);
      long unfrozen_pieces_bb = temp_bb & unfrozen_sq_bb;

      // Examine each player piece one at a time
      while (unfrozen_pieces_bb != 0) {
        long lsb_bb = unfrozen_pieces_bb & -unfrozen_pieces_bb;
        unfrozen_pieces_bb ^= lsb_bb; // Remove the piece

        // Get possible destination squares
        // Note special touching_bb to handle rabbit moves
        long temp_to_bb = touching_bb(lsb_bb, pt) & empty_bb & p_dest_bb;

        // Handle slide moves
        while (temp_to_bb != 0) {
          long lsb2_bb = temp_to_bb & -temp_to_bb;
          temp_to_bb ^= lsb2_bb; // Remove the piece

          // Record the move
          ArimaaMove move = result.getMove();
          move.clear();

          // Handles moving the piece
          move.piece_bb[pt] = lsb_bb | lsb2_bb;

          // Handles player suicide captures
          move.piece_bb[pt] ^= (lsb2_bb & player_suicide_bb);

          // Record number of steps used
          move.steps = 1;

//          System.out.println("SLIDE DEBUG 2\n"+move);

        }
      }
    }

  }


  /**
   * Tests if a piece will be frozen after completing the move
   * Assumes the piece slid from an adjacent square
   *
   * @param dest_bb long
   * @param piece_type int
   * @return boolean
   */

  private boolean is_dest_frozen(long dest_bb, int piece_type) {
    assert (exactlyOneBitSet(dest_bb));
    // Test if piece will be frozen after move
    boolean is_frozen = false;
    long temp_bb = touching_bb(dest_bb);
    if (atMostOneBitSet(temp_bb & colour_bb[player])) {
      if ((temp_bb & stronger_enemy_bb[piece_type]) != 0) {
        is_frozen = true;
      }
    }
    return is_frozen;
  }

  /**
   * Generate all 1 step slide moves.
   * Piece will not be frozen on dest square
   * Piece will not be self captured
   */
  public void genSlideMovesF(MoveList result,
    long p_start_bb, long p_dest_bb, boolean is_frozen) {

    // Phase Zero
    // Create temp player piece bb
    temp_player_bb[0] = piece_bb[player + 0];
    temp_player_bb[1] = piece_bb[player + 2];
    temp_player_bb[2] = piece_bb[player + 4];
    temp_player_bb[3] = piece_bb[player + 6];
    temp_player_bb[4] = piece_bb[player + 8];
    temp_player_bb[5] = piece_bb[player + 10];

    // Phase 1
    // Determine all possible self captures on traps
    // Determine all traps where player suicide is possible
    player_suicide_bb = 0L;

    for (int i = 0; i <= 3; i++) {

      long ptest_bb = TOUCH_TRAP[i] & colour_bb[player];
      // if exactly one player piece touching trap
      if (((ptest_bb & (ptest_bb - 1)) == 0) && (ptest_bb != 0)) {

        // Add trap to player suicide bb
        player_suicide_bb |= TRAP[i];

        // If trap contains player piece
        // AND touching piece is member of p_start_bb
        if ((TRAP[i] & colour_bb[player]) != 0 && ((ptest_bb & p_start_bb) != 0)) {

          int trap_pt = getPieceType(TRAP[i], player);
          int player_pt = getPieceType(ptest_bb, player);

          // Need to remove piece from temp_player_bb
          temp_player_bb[player_pt >> 1] ^= ptest_bb;

          // RULE: if ptest_bb moves then piece_type on TRAP[i] is
          // captured.
          long dest_bb = touching_bb(ptest_bb,
            player_pt) & empty_bb & p_dest_bb;
          while (dest_bb != 0) {
            long lsb2_bb = dest_bb & -dest_bb;
            dest_bb ^= lsb2_bb; // Remove the piece

            if (is_frozen == is_dest_frozen(lsb2_bb, player_pt)) {
              // Record the move
              ArimaaMove move = result.getMove();
              move.clear();
              move.piece_bb[player_pt] = ptest_bb | lsb2_bb; // Moving piece
              move.piece_bb[trap_pt] ^= TRAP[i]; // Captured piece XOR in case piece type is the same
              move.steps = 1;
            }
          }
        }
      }
    }

    // Remove bad trap squares, so suicide is not possible
    p_dest_bb &= ~player_suicide_bb;

    // Phase 2
    // Generate all remaining slide moves
    for (int pt = player, et = enemy; pt <= 11; pt += 2, et += 2) {

      // Get all unfrozen player pieces of type pt
      long temp_bb = temp_player_bb[pt >> 1] & p_start_bb;
      long unfrozen_sq_bb = touching_bb(colour_bb[player]) |
        ~touching_bb(stronger_enemy_bb[pt]);
      long unfrozen_pieces_bb = temp_bb & unfrozen_sq_bb;

      // Examine each player piece one at a time
      while (unfrozen_pieces_bb != 0) {
        long lsb_bb = unfrozen_pieces_bb & -unfrozen_pieces_bb;
        unfrozen_pieces_bb ^= lsb_bb; // Remove the piece

        // Get possible destination squares
        // Note special touching_bb to handle rabbit moves
        long temp_to_bb = touching_bb(lsb_bb, pt) & empty_bb & p_dest_bb;

        // Handle slide moves
        while (temp_to_bb != 0) {
          long lsb2_bb = temp_to_bb & -temp_to_bb;
          temp_to_bb ^= lsb2_bb; // Remove the piece

          if (is_frozen == is_dest_frozen(lsb2_bb, pt)) {
            // Record the move
            ArimaaMove move = result.getMove();
            move.clear();
            move.piece_bb[pt] = lsb_bb | lsb2_bb; // Moving piece
            //move.piece_bb[pt] ^= (lsb2_bb & player_suicide_bb);  // Not possible
            move.steps = 1;
          }
        }
      }
    }

  }


// Handles loss of protection captures for both colours
// The if statements are almost never true, so branches are predicted correctly
  void do_protection_captures(ArimaaMove move, long lsb_bb) {
    if ((lsb_bb & TRAP_protect[0]) != 0) {
      move.piece_bb[TRAP_pt[0]] ^= TRAP[0];
    }
    if ((lsb_bb & TRAP_protect[1]) != 0) {
      move.piece_bb[TRAP_pt[1]] ^= TRAP[1];
    }
    if ((lsb_bb & TRAP_protect[2]) != 0) {
      move.piece_bb[TRAP_pt[2]] ^= TRAP[2];
    }
    if ((lsb_bb & TRAP_protect[3]) != 0) {
      move.piece_bb[TRAP_pt[3]] ^= TRAP[3];
    }
  }


  /**
   * Generates all moves from the current position.
   * Piece captures are handled by this routine.
   *
   * @param   result long[][]     Moves are stored in this array
   * @param   cur_index int       Inclusive start of move list
   * @param   p_start_bb long     Set of possible origin squares
   * @param   p_dest_bb long      Set of possible destination squares
   * @param   e_start_bb long     Set of possible enemy origin squares
   * @param   e_dest_bb long      Set of possible enemy destination squares
   * @return  int                 Exclusive end of move list
   */



  public void
    genAllMoves(MoveList result,
    long p_start_bb, long p_dest_bb, long e_start_bb, long e_dest_bb) {

    //  result.clear();
    // Phase 1
    // Determine all possible suicides on traps
    calculate_trap_info();

    // Phase Two
    // Generate rabbit moves

    // Get all unfrozen player pieces of type pt
    long temp_bb = piece_bb[player] & p_start_bb;
    long unfrozen_sq_bb = touching_bb(colour_bb[player]) |
      ~touching_bb(stronger_enemy_bb[player]);
    long unfrozen_pieces_bb = temp_bb & unfrozen_sq_bb;

    // Examine each player piece one at a time
    while (unfrozen_pieces_bb != 0) {
      long lsb_bb = unfrozen_pieces_bb & -unfrozen_pieces_bb;
      unfrozen_pieces_bb ^= lsb_bb; // Remove the piece

      // Get possible destination squares
      // Note special touching_bb since rabbits can't go backwards
      long to_bb = touching_bb(lsb_bb, player) & empty_bb & p_dest_bb;

      while (to_bb != 0) {
        long lsb2_bb = to_bb & -to_bb;
        to_bb ^= lsb2_bb; // Remove the piece

        // Record the move
        ArimaaMove move = result.getMove();
        move.clear();

        // Handles moving the piece
        move.piece_bb[player] = lsb_bb | lsb2_bb;

        // Handles player suicide captures
        move.piece_bb[player] ^= (lsb2_bb & player_suicide_bb);

        // Handle loss of protection captures for *BOTH* colours
        do_protection_captures(move, lsb_bb);

        // Record number of steps used
        move.steps = 1;
      }
    }

    // Phase Three
    // Generate non rabbit moves
    long weaker_enemy_bb = piece_bb[enemy];

    for (int pt = player + 2, et = enemy + 2; pt <= 11; pt += 2, et += 2) {

      // Get all unfrozen player pieces of type pt
      temp_bb = piece_bb[pt] & p_start_bb;
      unfrozen_sq_bb = touching_bb(colour_bb[player]) |
        ~touching_bb(stronger_enemy_bb[pt]);
      unfrozen_pieces_bb = temp_bb & unfrozen_sq_bb;

      // Examine each player piece one at a time
      while (unfrozen_pieces_bb != 0) {
        long lsb_bb = unfrozen_pieces_bb & -unfrozen_pieces_bb;
        unfrozen_pieces_bb ^= lsb_bb; // Remove the piece

        // Get possible destination squares
        long to_bb = touching_bb(lsb_bb) & empty_bb & p_dest_bb;

        // Handle slide moves
        long temp_to_bb = to_bb;
        while (temp_to_bb != 0) {
          long lsb2_bb = temp_to_bb & -temp_to_bb;
          temp_to_bb ^= lsb2_bb; // Remove the piece

          // Record the move
          ArimaaMove move = result.getMove();
          move.clear();

          // Handles moving the piece
          move.piece_bb[pt] = lsb_bb | lsb2_bb;

          // Handles player suicide captures
          move.piece_bb[pt] ^= (lsb2_bb & player_suicide_bb);

          // Handle loss of protection captures for *BOTH* colours
          do_protection_captures(move, lsb_bb);

          // Record number of steps used
          move.steps = 1;
        }

        // Get possible victim squares
        long victim_bb = weaker_enemy_bb & touching_bb(lsb_bb) & e_start_bb;

        // Try all victim pieces one at a time
        while (victim_bb != 0) {
          long lsb3_bb = victim_bb & -victim_bb;
          victim_bb ^= lsb3_bb; // Remove the piece

          // Get the victim piece type
          int victim_pt = getPieceType(lsb3_bb, enemy);

          // Handle pull moves
          // Try all destination squares one at a time
          // Test if epiece can be pulled to desired location
          if ((lsb_bb & e_dest_bb) != 0) {
            temp_to_bb = to_bb;
            while (temp_to_bb != 0) {
              long lsb2_bb = temp_to_bb & -temp_to_bb;
              temp_to_bb ^= lsb2_bb; // Remove the piece

              // Record the move
              ArimaaMove move = result.getMove();
              move.clear();

              // Handles moving the pieces
              move.piece_bb[pt] = lsb_bb | lsb2_bb;
              move.piece_bb[victim_pt] = lsb3_bb | lsb_bb;

              // Handles suicide captures
              move.piece_bb[pt] ^= (lsb2_bb & player_suicide_bb);
              move.piece_bb[victim_pt] ^= (lsb_bb & enemy_suicide_bb);

              // Handle loss of protection captures for *BOTH* colours
              do_protection_captures(move, lsb_bb | lsb3_bb);

              // Record number of steps used
              move.steps = 2;
            }
          }
          // Handle push moves
          // Try all destination squares one at a time
          if ((lsb3_bb & p_dest_bb) != 0) {
            long victim_dest_bb = touching_bb(lsb3_bb) & empty_bb & e_dest_bb;
            while (victim_dest_bb != 0) {
              long lsb4_bb = victim_dest_bb & -victim_dest_bb;
              victim_dest_bb ^= lsb4_bb; // Remove the piece

              // Record the move
              ArimaaMove move = result.getMove();
              move.clear();

              // Handles moving the pieces
              move.piece_bb[pt] = lsb_bb | lsb3_bb;
              move.piece_bb[victim_pt] = lsb3_bb | lsb4_bb;

              // Handles suicide captures
              move.piece_bb[pt] ^= (lsb3_bb & player_suicide_bb);
              move.piece_bb[victim_pt] ^= (lsb4_bb & enemy_suicide_bb);

              // Handle loss of protection captures for *BOTH* colours
              do_protection_captures(move, lsb_bb | lsb3_bb);

              // Record number of steps used
              move.steps = 2;
            }
          }
        }
      }

      // Loop post operations
      weaker_enemy_bb |= piece_bb[et]; // Add enemy pieces of lesser value

    }

  }


  /**
   * Generates all moves NF
   * Moving player piece is not frozen at dest
   * Moving player piece is not suicided
   * @param result ArimaaMove[]
   * @param cur_index int
   * @param p_start_bb long
   * @param p_dest_bb long
   * @param e_start_bb long
   * @param e_dest_bb long
   * @return int
   */

  public void genAllMovesF(MoveList result,
    long p_start_bb, long p_dest_bb, long e_start_bb, long e_dest_bb,
    boolean is_frozen) {

    // Phase 1
    // Determine all possible suicides on traps
    calculate_trap_info();

    // Remove bad trap squares so moving player piece not captured
    p_dest_bb &= ~player_suicide_bb;

    // Phase Two
    // Generate rabbit moves

    // Get all unfrozen player pieces of type pt
    long temp_bb = piece_bb[player] & p_start_bb;
    long unfrozen_sq_bb = touching_bb(colour_bb[player]) |
      ~touching_bb(stronger_enemy_bb[player]);
    long unfrozen_pieces_bb = temp_bb & unfrozen_sq_bb;

    // Examine each player piece one at a time
    while (unfrozen_pieces_bb != 0) {
      long lsb_bb = unfrozen_pieces_bb & -unfrozen_pieces_bb;
      unfrozen_pieces_bb ^= lsb_bb; // Remove the piece

      // Get possible destination squares
      // Note special touching_bb since rabbits can't go backwards
      long to_bb = touching_bb(lsb_bb, player) & empty_bb & p_dest_bb;

      while (to_bb != 0) {
        long lsb2_bb = to_bb & -to_bb;
        to_bb ^= lsb2_bb; // Remove the piece

        if (is_frozen == is_dest_frozen(lsb2_bb, player)) {
          // Record the move
          ArimaaMove move = result.getMove();
          move.clear();
          move.piece_bb[player] = lsb_bb | lsb2_bb; // Moving piece
          //move.piece_bb[player] ^= (lsb2_bb & player_suicide_bb); // Player suicide captures not possible
          do_protection_captures(move, lsb_bb); // both colours
          move.steps = 1;
        }
      }
    }

    // Phase Three
    // Generate non rabbit moves
    long weaker_enemy_bb = piece_bb[enemy];

    for (int pt = player + 2, et = enemy + 2; pt <= 11; pt += 2, et += 2) {

      // Get all unfrozen player pieces of type pt
      temp_bb = piece_bb[pt] & p_start_bb;
      unfrozen_sq_bb = touching_bb(colour_bb[player]) |
        ~touching_bb(stronger_enemy_bb[pt]);
      unfrozen_pieces_bb = temp_bb & unfrozen_sq_bb;

      // Examine each player piece one at a time
      while (unfrozen_pieces_bb != 0) {
        long lsb_bb = unfrozen_pieces_bb & -unfrozen_pieces_bb;
        unfrozen_pieces_bb ^= lsb_bb; // Remove the piece

        // Get possible destination squares
        long to_bb = touching_bb(lsb_bb) & empty_bb & p_dest_bb;

        // Handle slide moves
        long temp_to_bb = to_bb;
        while (temp_to_bb != 0) {
          long lsb2_bb = temp_to_bb & -temp_to_bb;
          temp_to_bb ^= lsb2_bb; // Remove the piece

          if (is_frozen == is_dest_frozen(lsb2_bb, pt)) {
            // Record the move
            ArimaaMove move = result.getMove();
            move.clear();
            move.piece_bb[pt] = lsb_bb | lsb2_bb; // Moving piece
            //move.piece_bb[pt] ^= (lsb2_bb & player_suicide_bb); // Suicide
            do_protection_captures(move, lsb_bb);
            move.steps = 1;
          }
        }

        // Get possible victim squares
        long victim_bb = weaker_enemy_bb & touching_bb(lsb_bb) & e_start_bb;

        // Try all victim pieces one at a time
        while (victim_bb != 0) {
          long lsb3_bb = victim_bb & -victim_bb;
          victim_bb ^= lsb3_bb; // Remove the piece

          // Get the victim piece type
          int victim_pt = getPieceType(lsb3_bb, enemy);

          // Handle pull moves
          // Try all destination squares one at a time
          // Test if epiece can be pulled to desired location
          if ((lsb_bb & e_dest_bb) != 0) {
            temp_to_bb = to_bb;
            while (temp_to_bb != 0) {
              long lsb2_bb = temp_to_bb & -temp_to_bb;
              temp_to_bb ^= lsb2_bb; // Remove the piece

              if (is_frozen == is_dest_frozen(lsb2_bb, pt)) {
                // Record the move
                ArimaaMove move = result.getMove();
                move.clear();
                move.piece_bb[pt] = lsb_bb | lsb2_bb; // Moving piece
                move.piece_bb[victim_pt] = lsb3_bb | lsb_bb; // Moving piece
                //move.piece_bb[pt] ^= (lsb2_bb & player_suicide_bb); // Suicide
                move.piece_bb[victim_pt] ^= (lsb_bb & enemy_suicide_bb); // Suicide
                do_protection_captures(move, lsb_bb | lsb3_bb);
                move.steps = 2;
              }
            }
          }
          // Handle push moves
          // Try all destination squares one at a time
          if ((lsb3_bb & p_dest_bb) != 0) {
            long victim_dest_bb = touching_bb(lsb3_bb) & empty_bb & e_dest_bb;
            while (victim_dest_bb != 0) {
              long lsb4_bb = victim_dest_bb & -victim_dest_bb;
              victim_dest_bb ^= lsb4_bb; // Remove the piece

              if (is_frozen == is_dest_frozen(lsb3_bb, pt)) {
                // Record the move
                ArimaaMove move = result.getMove();
                move.clear();
                move.piece_bb[pt] = lsb_bb | lsb3_bb; // Moving piece
                move.piece_bb[victim_pt] = lsb3_bb | lsb4_bb; // Moving piece
                //move.piece_bb[pt] ^= (lsb3_bb & player_suicide_bb); // Suicide
                move.piece_bb[victim_pt] ^= (lsb4_bb & enemy_suicide_bb); // Suicide
                do_protection_captures(move, lsb_bb | lsb3_bb);
                move.steps = 2;
              }
            }
          }
        }
      }

      // Loop post operations
      weaker_enemy_bb |= piece_bb[et]; // Add enemy pieces of lesser value

    }

  }

  /**
   * Generates all push/pull moves from the current position.
   * Piece captures are handled by this routine.
   *
   * @param   result long[][]     Moves are stored in this array
   * @param   cur_index int       Inclusive start of move list
   * @param   p_start_bb long     Set of possible player origin squares
   * @param   p_dest_bb long      Set of possible player destination squares
   * @param   e_start_bb long     Set of possible enemy origin squares
   * @param   e_dest_bb long      Set of possible enemy destination squares
   * @return  int                 Exclusive end of move list
   */

  public void
    genDragMoves(MoveList result,
    long p_start_bb, long p_dest_bb, long e_start_bb, long e_dest_bb) {

    // Phase 1
    // Determine all possible suicides on traps
    calculate_trap_info();

    // Phase Three
    // Generate non rabbit moves
    long weaker_enemy_bb = piece_bb[enemy];

    for (int pt = player + 2, et = enemy + 2; pt <= 11; pt += 2, et += 2) {

      // Get all unfrozen player pieces of type pt
      long temp_bb = piece_bb[pt] & p_start_bb;
      long unfrozen_sq_bb = touching_bb(colour_bb[player]) |
        ~touching_bb(stronger_enemy_bb[pt]);
      long unfrozen_pieces_bb = temp_bb & unfrozen_sq_bb;

      // Examine each player piece one at a time
      while (unfrozen_pieces_bb != 0) {
        long lsb_bb = unfrozen_pieces_bb & -unfrozen_pieces_bb;
        unfrozen_pieces_bb ^= lsb_bb; // Remove the piece

        // Get possible destination squares
        long to_bb = touching_bb(lsb_bb) & empty_bb & p_dest_bb;

        // Get possible victim squares
        long victim_bb = weaker_enemy_bb & touching_bb(lsb_bb) & e_start_bb;

        // Try all victim pieces one at a time
        while (victim_bb != 0) {
          long lsb3_bb = victim_bb & -victim_bb;
          victim_bb ^= lsb3_bb; // Remove the piece

          // Get the victim piece type
          int victim_pt = getPieceType(lsb3_bb, enemy);

          // Handle pull moves
          // Try all destination squares one at a time
          // Test if epiece can be pulled to desired location
          if ((lsb_bb & e_dest_bb) != 0) {
            long temp_to_bb = to_bb;
            while (temp_to_bb != 0) {
              long lsb2_bb = temp_to_bb & -temp_to_bb;
              temp_to_bb ^= lsb2_bb; // Remove the piece

              // Record the move
              ArimaaMove move = result.getMove();
              move.clear();

              // Handles moving the pieces
              move.piece_bb[pt] = lsb_bb | lsb2_bb;
              move.piece_bb[victim_pt] = lsb3_bb | lsb_bb;

              // Handles suicide captures
              move.piece_bb[pt] ^= (lsb2_bb & player_suicide_bb);
              move.piece_bb[victim_pt] ^= (lsb_bb & enemy_suicide_bb);

              // Handle loss of protection captures for *BOTH* colours
              do_protection_captures(move, lsb_bb | lsb3_bb);

              // Record number of steps used
              move.steps = 2;
            }
          }

          // Handle push moves
          // Try all destination squares one at a time
          if ((lsb3_bb & p_dest_bb) != 0) {
            long victim_dest_bb = touching_bb(lsb3_bb) & empty_bb & e_dest_bb;
            while (victim_dest_bb != 0) {
              long lsb4_bb = victim_dest_bb & -victim_dest_bb;
              victim_dest_bb ^= lsb4_bb; // Remove the piece

              // Record the move
              ArimaaMove move = result.getMove();
              move.clear();

              // Handles moving the pieces
              move.piece_bb[pt] = lsb_bb | lsb3_bb;
              move.piece_bb[victim_pt] = lsb3_bb | lsb4_bb;

              // Handles suicide captures
              move.piece_bb[pt] ^= (lsb3_bb & player_suicide_bb);
              move.piece_bb[victim_pt] ^= (lsb4_bb & enemy_suicide_bb);

              // Handle loss of protection captures for *BOTH* colours
              do_protection_captures(move, lsb_bb | lsb3_bb);

              // Record number of steps used
              move.steps = 2;
            }
          }
        }
      }

      // Loop post operations
      weaker_enemy_bb |= piece_bb[et]; // Add enemy pieces of lesser value

    }

  }

  // Zobrist values for hash steps
  // Only do this once, it must be the same for everyone!
  static long hash_steps[] = new long[8];
  static {
    Random random = new Random(44);
    for (int i = 0; i < hash_steps.length; i++) {
      hash_steps[i] = random.nextLong();
    }
  }

  /**
   * Gets hash code for entire position
   * Uses zobrist for steps remaining and side to move
   * @return long
   */
  public long getPositionHash() {
    long result = getPieceHash();
    result ^= hash_steps[total_steps & 0x07];
    return result;
  }

  /**
   * Gets hash code for piece positions only
   * Uses modified sdbm algorithm.
   * There are likely better functions!!!
   *
   * @return long
   */
  public long getPieceHash() {
    long hash = 0;
    for (int i = 0; i < 12; i++) {
      //    hash = piece_bb[i] + (hash >> 11) + (hash << 6) + (hash << 16) - hash;

      hash = piece_bb[i] - (piece_bb[i] << 3) + (hash >> 13) + (hash >> 11) +
        (hash << 6) + (hash << 16) - hash;
    }
    return hash;
  }

  /**
   * Returns true if all pieces are in same position.
   * Side to move and steps remaining do not matter
   *
   * @param that GameState
   * @return boolean
   */
  public boolean equals(GameState that) {
    for (int i = 0; i < 12; i++) {
      if (this.piece_bb[i] != that.piece_bb[i]) {
        return false;
      }
    }
    return true;
  }


  /**
   * Returns true if all pieces in same position and number of steps the same
   * @param that GameState
   * @return boolean
   */
  public boolean full_equals(GameState that) {
    for (int i = 0; i < 12; i++) {
      if (this.piece_bb[i] != that.piece_bb[i]) {
        return false;
      }
    }
    if ( this.total_steps != that.total_steps) {
         return false;
    }
    return true;
  }

  /**
   * Returns piece type as a string
   * @param index int
   * @return String
   */
  public String getPieceText(int index) {
    assert (index >= 0 && index <= 63);

    // Default is no piece
    String result = " ";

    // Check for set bit in all the piece bitboards
    long piece_mask = 1L << index;
    for (int i = 0; i < 12; i++) {
      if ((piece_bb[i] & piece_mask) == piece_mask) {
        result = piece_text[i];
        break;
      }
    }

    return result;
  }

  /**
   * Tests if an enemy piece has been captured, by comparing
   * the current gamestate to the initial gamestate
   * @param initial GameState
   * @return boolean
   */

  public boolean is_enemy_piece_captured(GameState initial) {
    int initial_count = Util.PopCnt(initial.colour_bb[initial.enemy]);
    int current_count = Util.PopCnt(this.colour_bb[initial.enemy]);
    boolean result = (current_count == initial_count) ? false : true;
    return result;
  }

  /**
   *
   *  this routine will work for a piece of either colour
   *
   * @param piece_index individual piece's starting location
   * @param steps number of steps piece is allowed
   * @return bitboard of squares individual piece can reach unassisted
   */
  public long piece_can_reach(int piece_index, int steps) {

    // Create piece bitboard
    long piece_bb = 1L << piece_index;
    int piece_type = getPieceType(piece_bb);
    int piece_colour = piece_type & 0x01;

    // All player pieces except the moving piece
    long friend_bb = colour_bb[piece_colour] & ~piece_bb;

    // Check if moving piece is saving a player piece from being captured
    // This piece can't contribute to good squares, since it will be captured
    // However, it can unfreeze the piece before it has moved!!!!
    int trap_num = trap_number[piece_index];
    if (trap_num != -1) {
      if ((TRAP[trap_num] & friend_bb) != 0) {
        if ((TOUCH_TRAP[trap_num] & colour_bb[piece_colour]) == piece_bb) {
          friend_bb ^= TRAP[trap_num];
        }
      }
    }

    // All squares the piece can occupy and not be frozen
    // Second term required for the however comment above
    long unfrozen_sq_bb = (touching_bb(friend_bb) |
      (touching_bb(colour_bb[player]) & piece_bb) |
      ~touching_bb(stronger_enemy_bb[piece_type]));

    // Check if the piece is frozen
    if ((unfrozen_sq_bb & piece_bb) == 0) {
      return piece_bb;
    }

    // Check trap squares, if no same coloured pieces touches then piece can't cross
    long bad_trap_bb = 0L;
    for (int i = 0; i < 4; i++) {
      if ((TOUCH_TRAP[i] & friend_bb) == 0) {
        bad_trap_bb |= TRAP[i];
      }
    }

    // All empty squares the piece can be on and not be frozen
    long good_sq_bb = unfrozen_sq_bb & empty_bb & ~bad_trap_bb;

    // Flood fill, to see what squares the piece can reach in required steps
    long reached_bb = piece_bb;
    for (int i = 1; i < steps; i++) {
      reached_bb |= touching_bb(reached_bb, piece_type) & good_sq_bb;
    }

    // On final step, any empty square, not on an exposed trap, will do
    reached_bb |= touching_bb(reached_bb, piece_type) & empty_bb & ~bad_trap_bb;

    // Set of squares reachable in desired steps
    return reached_bb;

  }


  /**
   * Tests if an individual piece is a hostage
   * @param piece_index int
   * @return boolean
   */
  public boolean is_piece_a_hostage(int piece_index) {
    boolean result = false;

    // Create piece bitboard
    long lsb_bb = 1L << piece_index;

    assert ((lsb_bb & empty_bb) == 0);

    // Check if the piece is dominated
    if ((this.dominated_pieces_bb & lsb_bb) == 0) {
      return false;
    }

    int piece_type = getPieceType(piece_index);
    int piece_colour = piece_type & 0x01;

    long empty_touch_bb = touching_bb(lsb_bb) & empty_bb;
    long friend_touch_bb = touching_bb(lsb_bb) & colour_bb[piece_colour];

    // If two or more squares empty around a piece then not a hostage
    if (atMostOneBitSet(empty_touch_bb)) {
      if (friend_touch_bb == 0) {
        this.piece_bb[piece_colour] |= empty_touch_bb;
      }

      // If piece can't move, its a hostage
      // If it can move, its not a hostage
      result = !can_piece_move(piece_index);

      // Restore the board
      if (friend_touch_bb == 0) {
        this.piece_bb[piece_colour] &= ~empty_touch_bb;
      }

    }

    return result;
  }


  public boolean can_piece_move(int piece_index) {
    long lsb_bb = 1L << piece_index;
    return can_piece_move(lsb_bb);
  }

  /**
   *
   * Tests if an individual piece can move
   * Suicide does not count as a move
   * Useful for detecting framed pieces (usually on traps)
   *
   * @param piece_index index of piece to test
   * @return true iff piece can move, suicide doesn't count as a move
   */

  public boolean can_piece_move(long lsb_bb) {

    assert ((lsb_bb & empty_bb) == 0);

    // Check if the piece is frozen
    if ((this.frozen_pieces_bb & lsb_bb) != 0) {
      return false;
    }

    int piece_type = getPieceType(lsb_bb);
    int piece_colour = piece_type & 0x01;

    // All player pieces except the moving piece
    long player_bb = colour_bb[piece_colour] & ~lsb_bb;

    // Check trap squares, if no player pieces touches than piece can't cross
    // TODO this is only relevant if piece is touching a trap
    long illegal_trap_bb = 0L;
    for (int i = 0; i < 4; i++) {
      if ((TOUCH_TRAP[i] & player_bb) == 0) {
        illegal_trap_bb |= TRAP[i];
      }
    }

    // All squares piece can move to without being captured
    long good_sq_bb = empty_bb & ~illegal_trap_bb;

    // Compute set of squares piece can slide to in one step or less
    long reach_bb = touching_bb(lsb_bb, piece_type);

    // Check if a slide move is possible
    if ((reach_bb & good_sq_bb) != 0) {
      return true;
    }

    // All weaker enemy pieces, on good squares touching test square
    long weaker_enemy_bb = colour_bb[piece_colour ^
      0x01] & ~stronger_enemy_bb[piece_type] & ~piece_bb[piece_type ^ 0x01];
    long target_bb = weaker_enemy_bb & reach_bb;
    target_bb &= ~illegal_trap_bb;

    // Check if there is an empty square touching any piece that can be pushed
    if ((touching_bb(target_bb) & empty_bb) != 0) {
      return true;
    }

    // No point in trying pull moves, since they are same as sliding
    return false;
  }


  /**
   * Determines if the elephant can move
   * If an elephant can't move, it is a horrible disadvantage
   * Elephant suicide doesn't count as a move
   *
   * Uses pure bitboard methods, no FirstOne or PopCount
   */
  public boolean is_elephant_blockaded(int colour) {
    long elephant_bb = piece_bb[colour + 10];
    // All player pieces except the moving piece
    long player_bb = colour_bb[colour] & ~elephant_bb;

    // Check trap squares, if no player pieces touches than piece can't cross
    long illegal_trap_bb = 0L;
    for (int i = 0; i < 4; i++) {
      if ((TOUCH_TRAP[i] & player_bb) == 0) {
        illegal_trap_bb |= TRAP[i];
      }
    }
    long good_sq_bb = empty_bb & ~illegal_trap_bb;

    // Compute set of squares elephant can slide to in two steps or less
    long reach_bb = elephant_bb;
    reach_bb |= touching_bb(reach_bb) & good_sq_bb;
    reach_bb |= touching_bb(reach_bb) & good_sq_bb;

    long elephant_prison_bb = reach_bb; // Need to store this for later use

    // Precondition test passes if elephant can reach 4 or more squares
    assert (Util.PopCnt(reach_bb) <= 3);

    // All weaker enemy pieces, on good squares touching prison squares
    long weaker_enemy_bb = colour_bb[colour ^ 0x01] & ~piece_bb[colour ^
      0x01 + 10];

    long target_bb = weaker_enemy_bb & touching_bb(reach_bb);
    target_bb &= ~illegal_trap_bb;

    // Check if there is an empty square touching any piece that can be pushed
    if ((touching_bb(target_bb) & empty_bb & ~elephant_prison_bb) != 0) {
      return false;
    }

    // No point in trying pull moves, since they are same as sliding
    return true;

  }


  /**
   * Returns current gamestate as a string
   * @return String
   */
  public String toString() {
    String result = "";

    // Make first line
    result += getTurnNumber();
    result += (getSideToMove() == PL_WHITE) ? "w" : "b";

    // add DUMMY move to account for any partial moves played
    for (int i = 4; i > getStepsRemaining(); i--) {
      result += " rc5e";
    }

    // display the board
    result += " \n +-----------------+\n";
    for (int row = 7; row >= 0; row--) {
      result += (row + 1) + "|";

      for (int col = 0; col <= 7; col++) {
        int board_index = row * 8 + col;
        result += " " + getPieceText(board_index);
      }
      result += " |\n";

    }
    result += " +-----------------+\n";
    result += "   a b c d e f g h\n";

    result += "TS: " + total_steps + "\n";

    return result;
  }

  /**
   * Returns the current position as a board string
   * @return String
   */
  public String toBoardString() {
    String text = this.toString();
    return text;
  }

  /**
   * Returns the current position as an EPD string
   * @return String
   */
  public String toEPDString() {
    String text = this.toString();
    text = text.replaceAll("\n", "%13");
    return text;
  }


  /**
   * Returns current position as the first two moves of the game.
   * This can be pasted into the planning applet located at
   * http://arimaa.com/arimaa/games/planGame.cgi
   * @return String
   */
  public String toSetupString() {
    String result = "";

    //Get all the white pieces
    result += "1w ";
    for (int i = 0; i < 64; i++) {
      int type = getPieceType(i);
      if (type % 2 == 0) {
        result += getPieceText(i);
        result += getStringIndex(i);
        result += " ";
      }
    }
    result += "\n";

    // Get all the black pieces
    result += "1b ";
    for (int i = 0; i < 64; i++) {
      int type = getPieceType(i);
      if (type % 2 == 1) {
        result += getPieceText(i);
        result += getStringIndex(i);
        result += " ";
      }
    }
    result += "\n";

    // Add pass move if its black's turn
    if (this.player == 1) {
      result += "2w pass\n";
    }

    return result;
  }


  /**
   * Checks if given move is legal
   * Only checks push/pull/slide moves
   * NOTE: this routine is NOT THREAD SAFE
   * @param move ArimaaMove
   * @return boolean
   */
  private static MoveList m1 = new MoveList(1000);
  public boolean isLegalMove(ArimaaMove move) {

    // Generate all moves
    m1.clear();
    GENERATE_MOVES(m1, FULL_BB, FULL_BB);

    // Find candidate move
    for (ArimaaMove temp : m1) {
      if (temp.equals(move)) {
        return true;
      }
    }

    // Candidate move not found
    return false;
  }

  public static void main(String args[]) {

    String text[] = {
      "2b %13 +-----------------+%138| r r   C r r r r |%137|   e   C R   r   |%136| r e             |%135|   e D M e H   c |%134| C r   R r       |%133|     h R C r   E |%132|       E R H   r |%131| R R   R   E   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",

      "5w %13 +-----------------+%138|                 |%137|     D           |%136|     C           |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "5w %13 +-----------------+%138|                 |%137|     D           |%136| e   C           |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "5w %13 +-----------------+%138|     m           |%137|     D           |%136| e   C           |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
    };


    String white ="1w Ee2 Me1 Hg2 Hb2 Df2 Dd1 Cc2 Cd2 Ra2 Rh2 Ra1 Rb1 Rc1 Rf1 Rg1 Rh1";
    String black = "1b ha7 db7 cc7 md7 ee7 cf7 dg7 hh7 ra8 rb8 rc8 rd8 re8 rf8 rg8 rh8";

    GameState gs = new GameState(white,black);
    System.out.println(gs.toBoardString());
    System.out.println(gs.toEPDString());

  }

}
