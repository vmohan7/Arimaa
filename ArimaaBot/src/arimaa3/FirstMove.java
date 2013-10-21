package arimaa3;

import java.util.*;
import ai_util.*;

// Generates the first move for each side
public class FirstMove extends ArimaaBaseClass {
  public FirstMove() {
  }

  private Random random = null;
  private arimaa3.GameState position = null;

  // Generates the initial setup
  public String getFirstMove(arimaa3.GameState start_position, long random_seed) {
    String result = "";
    random = new Random(random_seed);
    this.position = start_position;
    InternalClass temp = new InternalClass();

    if (position.getSideToMove()==PL_WHITE) {
      result = temp.getFirstMoveWhite();
    }
    if (position.getSideToMove()==PL_BLACK) {
      result = temp.getFirstMoveBlack();
    }
    return result;

  }

  // I did this because int[] gets overwritten creating the position
  // and this simplified things
  class InternalClass {

    private final boolean F = false;
    private final boolean T = true;

    // Valid starting piece locations
    boolean first_elephant[] = {
      F, F, F, T, T, F, F, F, F, F, F, F, F, F, F, F};
    boolean first_camel[] = {
      F, F, F, T, T, F, F, F, F, F, F, T, T, F, F, F};
    boolean first_horse[] = {
      F, T, F, F, F, F, T, F, F, F, F, F, F, F, F, F};
    boolean first_dog[] = {
      F, F, T, T, T, T, F, F, F, F, F, T, T, F, F, F};
    boolean first_cat[] = {
      F, F, T, T, T, T, F, F, F, F, F, T, T, F, F, F};
    boolean first_rabbit[] = {
      T, F, T, T, T, T, F, T, T, T, T, T, T, T, T, T};

    boolean first_all[][] = {
      first_elephant, first_camel, first_horse, first_dog, first_cat,
      first_rabbit};

    // returns index as a string
    private final String col_text[] = {
      "a", "b", "c", "d", "e", "f", "g", "h"};

    // returns a random selection from the valid set of columns
    // There are many better ways to do this
    private int getValidIndex(boolean valid_set[]) {
      int index = -1;
      while (index == -1) {
        int temp = random.nextInt(valid_set.length);
        if (valid_set[temp] == true) {
          index = temp;
        }
      }
      return index;
    }

    private void setIndexFalse(boolean data[][], int column) {
      for (int i = 0; i < data.length; i++) {
        boolean piece[] = data[i];
        piece[column] = false;
      }
    }

    private String getIndexText(int index, int colour) {
      assert (colour == PL_WHITE || colour == PL_BLACK);
      assert (index >= 0 && index <= 15);

      String result = "";

      // Get the column
      result += col_text[index % 8];

      // Get the row
      if (colour == PL_WHITE) {
        result += (index <= 7) ? "2" : "1";
      }
      if (colour == PL_BLACK) {
        result += (index <= 7) ? "7" : "8";
      }
      return result;
    }

    private String getRabbitLocations(int colour) {
      String result = "";
      String rabbit_letter = (colour == PL_WHITE) ? "R" : "r";
      for (int i = 0; i < first_rabbit.length; i++) {
        if (first_rabbit[i] == true) {
          result += " " + rabbit_letter + getIndexText(i, colour);
        }
      }
      return result;
    }

    private String getFirstMoveWhite() {
      String result = "";

      // Position the elephant
      int column = getValidIndex(first_elephant);
      result += "E" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      // Position the Camel
      column = getValidIndex(first_camel);
      result += " M" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      // Position the Horses
      column = getValidIndex(first_horse);
      result += " H" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      column = getValidIndex(first_horse);
      result += " H" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      // Position the Dogs
      column = getValidIndex(first_dog);
      result += " D" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      column = getValidIndex(first_dog);
      result += " D" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      // Position the Cats
      column = getValidIndex(first_cat);
      result += " C" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      column = getValidIndex(first_cat);
      result += " C" + getIndexText(column, PL_WHITE);
      setIndexFalse(first_all, column);

      // Position the rabbits
      result += getRabbitLocations(PL_WHITE);

      return result;
    }

    // gets the column of the white elephant
    private int getWhiteElephantIndex() {
      int result = -1;
      for (int row = 0; row < 2; row++) {
        for (int col = 0; col < 8; col++) {
          int piece_type = position.getPieceType(position.getIndex(row, col));
          if (piece_type == PT_WHITE_ELEPHANT) {
            result = (1-row)*8+col;
          }
        }
      }
      return result;
    }

    // gets the column of the white camel
    private int getWhiteCamelIndex() {
      int result = -1;
      for (int row = 0; row < 2; row++) {
        for (int col = 0; col < 8; col++) {
          int piece_type = position.getPieceType(position.getIndex(row, col));
          if (piece_type == PT_WHITE_CAMEL) {
            result = (1-row)*8+col;
          }
        }
      }
      return result;
    }


    // gets the column of the white camel
    private int getWhiteHorseIndex1() {
      int result = -1;
      for (int row = 0; row < 2; row++) {
        for (int col = 0; col < 8; col++) {
          int piece_type = position.getPieceType(position.getIndex(row, col));
          if (piece_type == PT_WHITE_HORSE) {
            result = (1-row)*8+col;
          }
        }
      }
      return result;
    }

    private int getWhiteHorseIndex2() {
      int result = -1;
      for (int row = 1; row >= 0; row--) {
        for (int col = 7; col >= 0; col--) {
          int piece_type = position.getPieceType(position.getIndex(row, col));
          if (piece_type == PT_WHITE_HORSE) {
            result = (1-row)*8+col;
          }
        }
      }
      return result;
    }

    private String getFirstMoveBlack() {

      String result = "";

      int e1_index = this.getWhiteElephantIndex();
      int m1_index = this.getWhiteCamelIndex();
      int h1_index = this.getWhiteHorseIndex1();
      int h2_index = this.getWhiteHorseIndex2();

//      System.out.println(e1_index+" "+m1_index+" "+h1_index+" "+h2_index);

      // Rule 1: Do NOT line up black major piece with white elephant
      if ( e1_index <= 7 ) {
        first_elephant[e1_index] = false;
        first_camel[e1_index] = false;
        // Ok to line up the horse
//        first_horse[e1_index] = false;
      }


/*
      // Rule 2: Look for a juicy target for the elephant
      int camel_value[] = {50,40,0,15,15,0,40,50,0,0,0,0,0,0,0,0};
      int horse_value[] = {35,30,0,10,10,0,30,35,0,0,0,0,0,0,0,0};
      int best_index = -1;
      int best_value = -1;
      if ( camel_value[m1_index] > best_value ) {
        best_index = m1_index;
        best_value = camel_value[m1_index];
      }
      if ( horse_value[h1_index] > best_value ) {
        best_index = h1_index;
        best_value = horse_value[h1_index];
      }
      if ( horse_value[h2_index] > best_value ) {
        best_index = h2_index;
        best_value = camel_value[h2_index];
      }
*/

      // Position the black elephant
      int column;

      column = getValidIndex(first_elephant);
      result += "e" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);


      // Position the Horses
      column = getValidIndex(first_horse);
      result += " h" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);

      column = getValidIndex(first_horse);
      result += " h" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);

      // Position the Camel
      // Do this AFTER the horses!
      column = getValidIndex(first_camel);
      result += " m" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);

      // Position the Dogs
      column = getValidIndex(first_dog);
      result += " d" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);

      column = getValidIndex(first_dog);
      result += " d" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);

      // Position the Cats
      column = getValidIndex(first_cat);
      result += " c" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);

      column = getValidIndex(first_cat);
      result += " c" + getIndexText(column, PL_BLACK);
      setIndexFalse(first_all, column);

      // Position the rabbits
      result += getRabbitLocations(PL_BLACK);

      return result;
    }

  }


  public static void main(String args[]) {

    String text4[] = {
      "1w %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "1b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|                 |%133|                 |%132| C D H M E H D C |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "1b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|                 |%133|                 |%132| C D H C R H D M |%131| R R R E R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "1b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|                 |%133|                 |%132| M D H C E H D C |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
      "1b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|                 |%133|                 |%132| C M D H E D H C |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
    };

    FirstMove first_move = new FirstMove();

    for (String pos : text4) {
      arimaa3.GameState position = new arimaa3.GameState(pos);
      int seed = 1773;
      String result = first_move.getFirstMove(position, seed);
      System.out.println(position.toBoardString());
      System.out.println(result);
    }
  }
}
