package arimaa3;

/**
 * Utility functions that all arimaa classes can use
 */
public class ArimaaBaseClass implements Constants {
  protected ArimaaBaseClass() {
  }

  /**
   * Returns set of all squares touching target squares
   * @param target long
   * @return long
   */
  public static long touching_bb(long target) {
    return ((target & NOT_FILE_H) << 1) | ((target & NOT_FILE_A) >>> 1) |
      (target << 8) | (target >>> 8);
  }

  /**
   * Returns set of all squares piece can potentially move to
   * @param target long
   * @param piece_type int
   * @return long
   */
  public static long touching_bb(long target, int piece_type) {
    long result = ((target & NOT_FILE_H) << 1) | ((target & NOT_FILE_A) >>> 1);
    result |= (piece_type == 0) ? 0 : (target >>> 8); // White rabbits can't go south
    result |= (piece_type == 1) ? 0 : (target << 8); // Black rabbits can't go north
    return result;
  }


  /**
   * Returns target bb shifted right one file
   * @param target long
   * @return long
   */
  public static long right_bb(long target) {
      return (target & NOT_FILE_H) << 1;
  }

  /**
   * Returns target bb shifted left one file
   * @param target long
   * @return long
   */
  public static long left_bb(long target) {
    return (target & NOT_FILE_A) >>> 1;

  }


  /**
   * Gets the piece type of the given text
   * Returns -1 if text is not a piece
   * @param text String
   * @return int
   */
  public static int getPieceType(String text) {
    int result = -1;
    for (int i = 0; i < 12; i++) {
      if (text.equals(piece_text[i])) {
        result = i;
        break;
      }

    }
    return result;
  }

  protected static final String piece_text[] = {
    "R", "r", "C", "c", "D", "d", "H", "h", "M", "m", "E", "e"};


  protected static final String col_text[] = {
    "a", "b", "c", "d", "e", "f", "g", "h"};

  /**
   * returns index as a string
   */
  public static String getStringIndex(int index) {
    assert (index >= 0 && index <= 63);
    String result = "OB";
    if (index >= 0 && index <= 63) {
      int row = (index / 8) + 1;
      int col = (index % 8);
      result = col_text[col] + row;
    }
    return result;
  }


  public static void print_bitboard(long value) {
    print_bitboard(value,"");
  }


  /**
   * Returns a bitboard as a nicely formatted string
   * @param value long
   * @param text String
   * @return String
   */
  public static void print_bitboard(long value, String text) {
    // make a title
    String result = text;
    // display the board
    result += "\n +-----------------+\n";
    for (int row = 7; row >= 0; row--) {
      result += (row + 1) + "|";

      for (int col = 0; col <= 7; col++) {
        int board_index = row * 8 + col;
        boolean is_set = (value & (1L << board_index)) == (1L << board_index);
        String piece_text = is_set ? "1" : "0";
        result += " " + piece_text;
      }
      result += " |\n";

    }
    result += " +-----------------+\n";
    result += "   a b c d e f g h\n";

    System.out.println(result);
  }


  /**
   * Returns true iff exactly zero or one bits are set
   * @param test_bb long
   * @return boolean
   */

  public static boolean atMostOneBitSet(long test_bb) {
    return ((test_bb & (test_bb - 1)) == 0);
  }

  public static boolean moreThanOneBitSet(long test_bb) {
    return !((test_bb & (test_bb - 1)) == 0);
  }

  /**
   * Returns true iff exactly one bit is set
   * @param test_bb long
   * @return boolean
   */
  public static boolean exactlyOneBitSet(long test_bb) {
    return (((test_bb & (test_bb - 1)) == 0) && (test_bb != 0));
  }

  // Test out some stuff
  public static void main(String args[]) {
    for (int i = -10; i <= 10; i++) {
      System.out.println(i + " " + atMostOneBitSet(i) + " " +
        exactlyOneBitSet(i));
    }

    long test_bb = -1;
    print_bitboard(test_bb,"Start");
    print_bitboard(left_bb(test_bb),"Left");
    print_bitboard(right_bb(test_bb),"Right");
  }

}
