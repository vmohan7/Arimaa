package arimaa3;

import ai_util.*;
import java.util.*;

public class ArimaaMove extends ArimaaBaseClass implements Comparable {

  public long piece_bb[] = new long[12];
  public int steps;
  public int move_ordering_value;
  
  /* Arzav's code:
   * Array of bytes to hold the source indices (0-63) for at most 4 
   * steps in the current turn/move.
   */
  public byte[] stepSources = {-1, -1, -1, -1};
  /* ------ */

  public ArimaaMove() {
  }

  public int compareTo(Object o) {
    ArimaaMove that = (ArimaaMove) o;
    return that.move_ordering_value - this.move_ordering_value;
  }

  /**
   * Clears the move
   */
  public void clear() {
    piece_bb[0] = 0;
    piece_bb[1] = 0;
    piece_bb[2] = 0;
    piece_bb[3] = 0;
    piece_bb[4] = 0;
    piece_bb[5] = 0;
    piece_bb[6] = 0;
    piece_bb[7] = 0;
    piece_bb[8] = 0;
    piece_bb[9] = 0;
    piece_bb[10] = 0;
    piece_bb[11] = 0;
    steps = 0;
    move_ordering_value = 0;
  }

  public void add(ArimaaMove one, ArimaaMove two) {
    for (int i = 0; i < 12; i++) {
      piece_bb[i] = one.piece_bb[i] ^ two.piece_bb[i];
    }
    steps = one.steps + two.steps;
    move_ordering_value = one.move_ordering_value + two.move_ordering_value;
  }

  public void copy(ArimaaMove one) {
    for (int i = 0; i < 12; i++) {
      piece_bb[i] = one.piece_bb[i];
    }
    steps = one.steps;
    move_ordering_value = one.move_ordering_value;
  }

  /**
   * Takes the two game states and returns the move to get from start to end.
   * None of the secondary or tertiary bitboards are computed
   * @param start GameState
   * @param end GameState
   */
  public void difference(GameState start, GameState end) {
    for (int i = 0; i < 12; i++) {
      piece_bb[i] = start.piece_bb[i] ^ end.piece_bb[i];
    }
    steps = end.total_steps - start.total_steps;
    move_ordering_value = 0;
  }


  private static final String piece_text[] = {
    "R", "r", "C", "c", "D", "d", "H", "h", "M", "m", "E", "e"};

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
   * Returns all the set bits
   * ie Any squares involved in the move
   * @return long
   */
  public long getSetBits() {
    long result = 0;
    for (int i = 0; i < 12; i++) {
      result |= piece_bb[i];
    }
    return result;
  }


  public long getSetBits(int player) {
    assert( player == 0 || player ==1 );
    long result = 0;
    for (int i = player; i < 12; i+=2) {
      result |= piece_bb[i];
    }
    return result;
  }


  public boolean equals(Object o) {
    if (!(o instanceof ArimaaMove)) {
      return false;
    }
    ArimaaMove that = (ArimaaMove) o;

    if (this.steps != that.steps) {
      return false;
    }

    for (int i = 0; i < 12; i++) {
      if (this.piece_bb[i] != that.piece_bb[i]) {
        return false;
      }
    }
    return true;
  }


  /**
   * Dumps out a fancy board representation of the move
   * @return String
   */
  public String toBoardString() {
    String result = "";

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

    return result;

  }


  // Storage for compressed move
  private long low_compressed;
  private long high_compressed;

  public long getLowCompressed() {
    return low_compressed;
  }

  public long getHighCompressed() {
    return high_compressed;
  }

  /**
   * Creates the compressed arimaa move
   * Access results with getLowCompressed and getHighCompressed
   */
  public void computeCompressed() {
    // Clear storage
    low_compressed = 0;
    high_compressed = 0;

    // Process all the piece locations
    long total_bits = 0;

    for (int piece_type = 0; piece_type < 12; piece_type++) {
      long temp_bb = piece_bb[piece_type];
      while (temp_bb != 0) {
        long lsb_bb = temp_bb & ~temp_bb;
        temp_bb ^= lsb_bb;

        int index = Util.FirstOne(lsb_bb);

        if (total_bits < 6) {
          // Place in low word
          low_compressed |= piece_type << (4 + 10 * total_bits);
          low_compressed |= index << (10 + 10 * total_bits);
        } else {
          // Place in high word
          high_compressed |= piece_type << (4 + 10 * (total_bits - 6));
          high_compressed |= index << (10 + 10 * (total_bits - 6));
        }

        total_bits++;
      }
    }

    // Fill in step count and total bits
    low_compressed |= total_bits;
    high_compressed |= steps;
  }

  /**
   * Creates a normal arimaa move from a compressed move
   * @param low_bits long
   * @param high_bits long
   */
  public void fromCompressed(long low_bits, long high_bits) {
    this.clear();

    this.steps = (int) (high_bits & 0x0F);

    // Get num set bits
    int num_set_bits = (int) (low_bits & 0xF);
    int low_words = (num_set_bits <= 6) ? num_set_bits : 6;
    int high_words = (num_set_bits <= 6) ? 0 : num_set_bits - 6;

    // Process the low word
    for (int i = 0; i < low_words; i++) {
      int piece_type = (int) ((low_bits >>> (4 + i * 10)) & 0xF);
      int index = (int) ((low_bits >>> (10 + i * 10)) & 0x3F);
      piece_bb[piece_type] |= 1L << index;
    }

    // Process the high word
    for (int i = 0; i < high_words; i++) {
      int piece_type = (int) ((high_bits >>> (4 + i * 10)) & 0xF);
      int index = (int) ((high_bits >>> (10 + i * 10)) & 0x3F);
      piece_bb[piece_type] |= 1L << index;
    }
  }




  /**
   * Dumps out a one line text representation of the move
   * This is NOT OFFICIAL ARIMAA NOTATION
   * Prints out number of steps,
   * then displays every set bit
   * @return String
   */



  public String toString() {
    String result = "{" + steps + "}";

    boolean is_pass_move = true;
    for (int i = 0; i < 12; i++) {
      if (piece_bb[i] != 0) {
        is_pass_move = false;
        String temp = piece_text[i];
        long temp_bb = piece_bb[i];
        while (temp_bb != 0) {
          int index = Util.FirstOne(temp_bb);
          temp_bb ^= 1L << index;
          temp += getStringIndex(index);
        }
        result += temp + " ";
      }
    }

    if (is_pass_move) {
      result += "pass";
    }

    return result;
  }


  /**
   * Converts piece text to piece id
   * @param text String
   * @return int
   */
  public static int piece_type_from_text(String text) {
    int result = -1;

    for (int i = 0; i < piece_text.length; i++) {
      if (text.equals(piece_text[i])) {
        result = i;
        break;
      }
    }
    assert (result != -1);
    return result;
  }


  /**
   * Creates a move assuming move_text is official arimaa notation
   *
   * @param move_text String
   */
  public ArimaaMove(String move_text) {

    // Clear the move
    clear();

    // Steps are always 4
    this.steps = 4;
    
    /* Arzav's code: */
    int stepNumber = 0;
    /* ------ */
    
    // Break the move into its component parts
    StringTokenizer tokenizer = new StringTokenizer(move_text);

    while (tokenizer.hasMoreTokens()) {
      String move = tokenizer.nextToken();

      if (move.equals("pass")) {
        break;
      }

      // Convert the text move to numbers
      int piece_type = piece_type_from_text(move.substring(0, 1));
      assert (piece_type >= 0 && piece_type <= 11);
      String col_text = "abcdefgh";
      int col = col_text.indexOf(move.substring(1, 2));
      int row = Integer.parseInt(move.substring(2, 3)) - 1;
      String dir_text = "nsewx";
      int direction = dir_text.indexOf(move.substring(3, 4));

      // Handles captures
      if (direction == 4) {
        int index = row * 8 + col;
        this.piece_bb[piece_type] ^= 1L << index;
      }

      if (direction != 4) {
        int from_index = row * 8 + col;
        int dir_offset[] = {8, -8, 1, -1, 0};
        int to_index = from_index + dir_offset[direction];
        this.piece_bb[piece_type] ^= 1L << from_index;
        this.piece_bb[piece_type] ^= 1L << to_index;
        
        /* Arzav's code: */
        stepSources[stepNumber++] = (byte) from_index;
        /* ------ */
      }

    }
  }


  // returns index as a string
  private static final String col_text[] = {
    "a", "b", "c", "d", "e", "f", "g",
    "h"};

  public static String getStringIndex(int index) {
    String result = "OB";
    if (index >= 0 && index <= 63) {
      int row = (index / 8) + 1;
      int col = (index % 8);
      result = col_text[col] + row;
    }
    return result;
  }

  // returns direction as a string, given from_index and to_index
  public String getStringDirection(int from_index, int to_index) {
    String result = "";
    if ((to_index - from_index) == 1) {
      result = "e";
    } else if ((to_index - from_index) == -1) {
      result = "w";
    } else if ((to_index - from_index) == 8) {
      result = "n";
    } else if ((to_index - from_index) == -8) {
      result = "s";
    } else {
      // Illegal from_index,to_index combination
      assert (false);
    }
    return result;
  }


  /**
   * Converts a simple move to official notation
   * A simple move is a slide,push or a pull. Nothing else.
   * @param gs GameState
   * @return String
   */
  public String toOfficialArimaaNotation(GameState gs) {
    String result = "";

    // Handle pass move
    if ( getSetBits() == 0 ) {
      return "pass";
    }

    // Handle slide moves
    if (this.steps == 1) {
      long set_bits = getSetBits();
      result += calculate_step(gs,set_bits,gs.player);
    }

    // Handle push/pull moves
    // This does *NOT* handle two consecutive step moves
    if ( this.steps == 2 ) {

      // Figure out the common square
      long player_bb = getSetBits(gs.player);
      long enemy_bb = getSetBits(gs.enemy);
      long common_bb = player_bb & enemy_bb;

      if ( common_bb == 0 ) {
        // Must be a capture move, with common_bb on a trap
        common_bb = touching_bb(enemy_bb) & TRAP_SQUARES;
      }

      // Figure out if its a push or pull

      // Pull move
      if ( (common_bb & gs.colour_bb[gs.player]) != 0 ) {
        result += calculate_step(gs,player_bb,gs.player);
        result += " "+calculate_step(gs,enemy_bb,gs.enemy);
      }
      // Must be push move
      else {
        result += calculate_step(gs,enemy_bb,gs.enemy);
        result += " "+calculate_step(gs,player_bb,gs.player);
      }

    }

    return result;
  }


  /**
   * Calculates the notation for a single step
   * @param gs GameState
   * @param set_bits long
   * @return String
   */
  private String calculate_step(GameState gs,long set_bits,int colour) {
    String result = "";
    int num_set_bits = Util.PopCnt(set_bits);

    // No capture
    if (num_set_bits == 2) {
      int piece_type = gs.getPieceType(set_bits & ~gs.empty_bb,colour);

      int index1 = Util.FirstOne(set_bits);
      set_bits ^= 1L << index1;
      int index2 = Util.FirstOne(set_bits);

      // Figure out which direction the piece moved
      long test_bb = 1L << index2;
      int from_index = index1;
      int to_index = index2;

      if ((test_bb & gs.colour_bb[colour]) != 0) {
        from_index = index2;
        to_index = index1;
      }

      result += piece_text[piece_type];
      result += getStringIndex(from_index);
      result += getStringDirection(from_index, to_index);

    }

    // Suicide
    else if (num_set_bits == 1) {
      int from_index = Util.FirstOne(set_bits);
      int to_index = TRAP_INDEX[trap_number[from_index]];
      int piece_type = gs.getPieceType(set_bits,colour);

      result += piece_text[piece_type];
      result += getStringIndex(from_index);
      result += getStringDirection(from_index, to_index);

      result += " " + piece_text[piece_type];
      result += getStringIndex(to_index);
      result += "x";

    }

    // Captured piece (removal of protection)
    else if (num_set_bits == 3) {

      // Figure out which piece was captured
      long captured_bb = set_bits & TRAP_SQUARES;
      assert (Util.PopCnt(captured_bb) == 1);

      // Remove the captured piece from consideration
      set_bits ^= captured_bb;

      // Generate the slide move
      int piece_type = gs.getPieceType(set_bits & ~gs.empty_bb,colour);
      int index1 = Util.FirstOne(set_bits);
      set_bits ^= 1L << index1;
      int index2 = Util.FirstOne(set_bits);

      // Figure out which direction the piece moved
      long test_bb = 1L << index2;
      int from_index = index1;
      int to_index = index2;
      if ((test_bb & gs.empty_bb) == 0) {
        from_index = index2;
        to_index = index1;
      }

      result += piece_text[piece_type];
      result += getStringIndex(from_index);
      result += getStringDirection(from_index, to_index);

      // Process the captured piece
      int captured_piece_type = gs.getPieceType(captured_bb,colour);
      int captured_index = Util.FirstOne(captured_bb);
      result += " " + piece_text[captured_piece_type];
      result += getStringIndex(captured_index);
      result += "x";

    }
    return result;
  }


  public static void main(String args[]) {

    String tests[] = {
      "2w %13 +-----------------+%138| r r D   r r h r |%137| e e r R r   r   |%136|   D R d H       |%135|   e R M e     e |%134|       R         |%133|             M   |%132|             R   |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
      "17b %13 +-----------------+%138| r r   c c r   r |%137|     r     d     |%136| r H             |%135|                 |%134|           E r   |%133| R H       R M d |%132|           e R r |%131|   R   R C h   R |%13 +-----------------+%13   a b c d e f g h%13", // Belbo
      "26w %13 +-----------------+%138|   r r d H r r r |%137| r     c E R   r |%136|   h             |%135|                 |%134|     m           |%133|       r         |%132|     M e   R     |%131| R     R D   R R |%13 +-----------------+%13   a b c d e f g h%13", // 99of9 vs RonWeasley Test position
      "2w %13 +-----------------+%138| r r         r   |%137| r R E   r   M r |%136| C m h     X E   |%135|   H   d c       |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   e r r r r |%137|       r r   r   |%136|     X C   X     |%135|     R           |%134|                 |%133|     X     X     |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r     r r r r |%137| e e r C r   r   |%136|   D R R   X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "33b %13 +-----------------+%138|     r r r r M r |%137| r         c r   |%136|       d d       |%135| h D c m         |%134|     E e         |%133|     r           |%132|   R h         H |%131| R R   H R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "33b %13 +-----------------+%138|         d r     |%137|   r     M r e   |%136|   d   c   R     |%135|       c   E   R |%134| r   H         r |%133| D h R   d   C   |%132| r D m   R   r C |%131| R R       R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "31b %13 +-----------------+%138|   c   d     c   |%137|         d r     |%136|               C |%135|   r             |%134|             r h |%133|   M   R   r r R |%132| R h   m E e H   |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "33b %13 +-----------------+%138|         d r     |%137|   r     M r e   |%136|   d   c     R   |%135|       c   E   R |%134| r   H         r |%133| D h R   d   r C |%132| r D m   C       |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13",
      "33w %13 +-----------------+%138|               H |%137| r         M e   |%136|   d   c     R   |%135|       c     E R |%134| r   H         r |%133| D h R   d   r C |%132| r D m         C |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13",
      "2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   e X     X     |%135|   e R M e       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
      "38b %13 +-----------------+%138| r   r   r d r r |%137| d           R   |%136|       E e       |%135|       R   m     |%134|     D R     R r |%133|   R           R |%132| R               |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
      "38w %13 +-----------------+%138| r r r r r r r r |%137| R R R R R   C h |%136|         H   e M |%135|               R |%134|           E     |%133|             D   |%132|                 |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
      "20w %13 +-----------------+%138|                 |%137|   r             |%136|   h   E M   r   |%135|   D d     H r   |%134|       H   c     |%133|       C     C   |%132| R r     D     R |%131| R R R     R R R |%13 +-----------------+%13   a b c d e f g h%13", // Game Number: 21399
      "8b %13 +-----------------+%138| r   r   r d r r |%137| d           R   |%136|       E e       |%135|     D R   m     |%134|         R   R r |%133|   R           R |%132| R               |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
      "37w %13 +-----------------+%138| r   r   r d r r |%137| d           R   |%136|       E e       |%135|       R   m     |%134|     D R R     r |%133|   R           R |%132| R               |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
      "23b %13 +-----------------+%138| r r         r r |%137| r   r     r   r |%136|                 |%135|     m E   R     |%134|       C         |%133|         M   D   |%132|     e     H     |%131| R R       R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "37b %13 +-----------------+%138| r   r   r d r r |%137| d           R   |%136|       E e       |%135|     R     m     |%134|     D   R   R r |%133|   R           R |%132| R               |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
      "36b %13 +-----------------+%138| r r     r d r r |%137| d           R r |%136|       E e       |%135|       R   m     |%134|     D R R       |%133|   R           R |%132| R               |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
      "37b %13 +-----------------+%138| r r     r d r r |%137|   d         R   |%136|       E e       |%135|       R   m r   |%134|     D   R   R   |%133|   R           R |%132| R               |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
      "37b %13 +-----------------+%138| r r     r d r r |%137|   d         R   |%136|       E e       |%135|     R     m r   |%134|     D   R   R   |%133|   R           R |%132| R               |%131|   C             |%13 +-----------------+%13   a b c d e f g h%13",
    };


    MoveList move_list = new MoveList(500);

    for (String text : tests) {
      GameState gs = new GameState(text);
      move_list.clear();
      gs.genDragMoves(move_list,FULL_BB,FULL_BB,FULL_BB,FULL_BB);

      System.out.println(gs.toBoardString());

      for ( ArimaaMove move : move_list ) {
        System.out.println(move);
        System.out.println(move.toOfficialArimaaNotation(gs));
      }
    }
  }

}
