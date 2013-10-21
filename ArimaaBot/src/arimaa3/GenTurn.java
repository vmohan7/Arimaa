package arimaa3;

import ai_util.*;


/**
 * Generates <b>all</b> possible moves for the given position
 */

public class GenTurn implements Constants {

  public GenTurn() {
  }


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

  private MoveList move_data;
  private GameState saved_initial_position = null;
  private RepetitionHashTable repetition = new RepetitionHashTable(19);


  private boolean get_notation;
  private GameState desired_position = new GameState();
  private String notation_result = "";
  private boolean debug_flag;

  public String getOfficialArimaaNotation(GameState initial_position, ArimaaMove move ) {

    this.debug_flag = false;
    this.get_notation = true;
    this.saved_initial_position = initial_position;

//    System.out.println("Desired: "+move);
    try {
      this.desired_position.play(move, initial_position);
      this.repetition.increaseAge();
      gen_moves(initial_position);
    }
    catch (ArrayIndexOutOfBoundsException b) {
    };


    assert(debug_flag);
    return notation_result;
  }

  public void calculate_notation() {
    debug_flag = true;

    notation_result = "";

    GameState position = saved_initial_position;
    do {
      int steps_available = position.getStepsRemaining();
//      System.out.println(position);
      ArimaaMove move = move_list_stack[steps_available].getCurrentMove();
//      System.out.println(move);
      notation_result += move.toOfficialArimaaNotation(position) + " ";
      GameState new_position = gs_stack[steps_available];
      new_position.play(move, position);
      position = new_position;
    } while(position.getStepsRemaining()!=4);

    notation_result = notation_result.trim();
  }


  public void genAllTurns(GameState initial_position, MoveList move_list) {

    move_list.clear();
    this.move_data = move_list;
    this.saved_initial_position = initial_position;
    this.gen_calls++;
    this.get_notation = false;

    this.repetition.increaseAge();

    // Generate the moves
    gen_moves(initial_position);

  }


  // internal worker function to gen moves
  private void gen_moves(GameState initial_position) {

    int steps_available = initial_position.getStepsRemaining();
    move_list_stack[steps_available].clear();
    // Generate all legal steps from current position
    initial_position.GENERATE_MOVES(move_list_stack[steps_available], FULL_BB,
      FULL_BB);

    for (ArimaaMove move : move_list_stack[steps_available]) {

      // Play the move
      GameState new_position = gs_stack[steps_available];
      new_position.play(move, initial_position);


      // Test for returning to initial position
      if (new_position.equals(this.saved_initial_position)) {
        initial_repeated_positions++;
        continue;
      }

      // Test for repetition of position
      long hash_code = new_position.getPositionHash();
      if (repetition.isRepetition(hash_code)) { // Has side effects
        repeated_positions++;
        continue;
      }


      // Check if we're getting official arimaa notation
      if ( get_notation && new_position.full_equals(desired_position) ) {
        calculate_notation();
        throw new ArrayIndexOutOfBoundsException();
      }


      // Base case for recursion
      if (new_position.getStepsRemaining() == 4) {
        if ( !get_notation ) {
          total_moves++;
          // Record the move
          ArimaaMove result = move_data.getMove();
          result.difference(saved_initial_position, new_position);
        }
      }

      else {
        // Generate the next steps
        gen_moves(new_position);
      }

    }
  }


  /**
   *
   * Reports statistics on move generation
   *
   * @return String  Interesting statistics
   */
  public String getStats() {
    String result = "";
    result += "GenTurn calls: " + gen_calls + "\n";
    result += "Total Moves: " + total_moves + "\n";
    result += "Initial Positions: " + this.initial_repeated_positions + "\n";
    result += "Repeated Positions: " + repeated_positions + "\n";
    return result;
  }

  /**
   * Resets statistics
   */

  public void resetStats() {
    total_moves = 0;
    repeated_positions = 0;
    initial_repeated_positions = 0;
    gen_calls = 0;
  }

  // Statistics collection stuff
  private static long total_moves = 0;
  private static long repeated_positions = 0;
  private static long initial_repeated_positions = 0;
  private static long gen_calls = 0;


  public static void main(String args[]) {

    String text[] = {
//      "20w %13 +-----------------+%138| r r   m h   r   |%137| r   M     r   r |%136| r     E         |%135|   R           H |%134|               e |%133| D       C     R |%132|     H C   R     |%131| R R R       R R |%13 +-----------------+%13   a b c d e f g h%13",

      "16w %13 +-----------------+%138|     e           |%137| R               |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
      "16b %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
      "16w %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
/*
      "26b %13 +-----------------+%138| r r r r r   r r |%137| M E d h         |%136| h D C c c   R H |%135| R R R R r m   e |%134|             H R |%133|                 |%132|         R       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "46b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|         r   r   |%133| r   r   E   D r |%132| R M e R m     R |%131|   h R           |%13 +-----------------+%13   a b c d e f g h%13",
      "61w %13 +-----------------+%138| r H r           |%137|   m E           |%136|   r R d c   r   |%135|   R   R R   R r |%134| R D e R   r C r |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "58b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|   C         M r |%133| R e r E         |%132|     D r r r   R |%131|     R R R R R   |%13 +-----------------+%13   a b c d e f g h%13",
      "40b %13 +-----------------+%138|         r   r r |%137| r             c |%136|         D       |%135|   H   R         |%134|           M     |%133|   m           H |%132|   E e R       r |%131|             R R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138| r r     r   H   |%137|     c r R h   r |%136|   d       H R   |%135|             e   |%134|   h E       r C |%133|   d R c     R   |%132|     R           |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137|   E   M   C     |%136|                 |%135|     D   H   D   |%134|       H   C     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137|   E   H   C     |%136|                 |%135|     D   H   D   |%134|       M   C     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137|   C   H   E     |%136|                 |%135|     D   H   D   |%134|       M   C     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137|   C   H   E     |%136|                 |%135|     D   H   D   |%134|       M   C     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137|   C   H   E     |%136|                 |%135|     D   M   D   |%134|       H   C     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137|   C   H   E     |%136|                 |%135|     D   M   D   |%134|       C   H     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137|   E   H   C     |%136|                 |%135|     D   M   D   |%134|       C   H     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
      "63w %13 +-----------------+%138|               r |%137| E     H   C     |%136|                 |%135|     D   M   D   |%134|       C   H     |%133|   R             |%132|     R   R   R   |%131|   R   R   R   R |%13 +-----------------+%13   a b c d e f g h%13",
*/
    };

    GenTurn test = new GenTurn();

    // Create the result array
    MoveList result = new MoveList(400000);

    // Run test positions
    for (String pos_text : text) {

      long start_time = System.currentTimeMillis();
      // Output the test position
      GameState position = new GameState(pos_text);
      System.out.println(position);
      System.out.println(position.toSetupString());

      test.genAllTurns(position, result);
      System.out.println("Total moves: " + result.size());
      long elapsed_time = System.currentTimeMillis() - start_time;
      System.out.println("Elapsed time: " + elapsed_time + "ms");


      // Output resulting positions
      int count = 0;
      for (ArimaaMove move : result) {
        System.out.println(count+" "+test.getOfficialArimaaNotation(position,move)+"*");
        count++;
      }

    }
      System.out.println(test.getStats());


  }
}
