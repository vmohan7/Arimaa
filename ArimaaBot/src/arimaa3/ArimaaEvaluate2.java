
package arimaa3;


import ai_util.*;
import java.util.*;

public class ArimaaEvaluate2 extends ArimaaBaseClass {


  public ArimaaEvaluate2() {
  }

  // Globals used for statistics collection
  private static long eval_calls = 0;

  // Returns some statistics on the eval function
  public static String getStats() {
    String result = "";
    result += "EvCalls: " + eval_calls + " ";
    return result;
  }

  private String eval_text;
  private boolean createText;
  private int score_offset;

  public String getEvalText() {
    return eval_text;
  }

  /**
   * This must be called before any calls to evaluate
   * It fills in a bunch of piece square tables based on the root position
   * It is called by the search, with the root position before any searching
   * is done.
   *
   * @param root_gs GameState
   * @return int
   */

  public int PreProcessRootPosition(GameState root_gs) {
    root_gs.compute_tertiary_bitboards();
    this.createText = false;
    getDynamicPieceValues(root_gs);
    score_offset = getPieceLocationEvaluation(root_gs);
    return score_offset;
  }


  /**
   * This function is called by the search whenever an eval is required
   * @param gs GameState
   * @param createText boolean
   * @return int
   */
  public int Evaluate(GameState gs, boolean createText) {

    gs.compute_tertiary_bitboards();

    this.createText = createText;

    eval_calls++;

    // Everything is evaluated from white's point of view (ie gold)
    // The score is negated at the end, if it's black's turn (ie silver)
    int score = 0;

    if (createText) {
      eval_text = "Position Evaluation Results\n";
      eval_text += "Side to Move: " + gs.player + " \n";
    }

    score += getPieceLocationEvaluation(gs);

    // Pulls score back down to within mate bounds range
    // score_offset is computed in PreProcessRootPosition call
    // this just allows more effective range for eval values
    score -= score_offset;

    // Force score to be inside mate bounds
    // If we don't do this *HORRIBLE NASTY PROBLEMS* occur
    if (score >= 29990) {
      score = +29990;
    }
    if (score <= -29990) {
      score = -29990;
    }

    if (createText) {
      eval_text += "Score: " + score + " (White's POV)\n";
    }

    // Flip the score value if it's black's turn
    return (gs.player == PL_WHITE) ? score : -score;
  }

  private int dynamic_piece_value[] = new int[12];

  /**
   * Computes dynamic piece values based on given position
   * A piece is worth what the relative change in FAME is
   * if its captured.
   * @param gs GameState
   */
  private void getDynamicPieceValues(GameState gs) {
    int initial_score = getFAMEMaterialScore(gs, FULL_BB);
    for (int i = 0; i < 12; i++) {
      long lsb_bb = gs.piece_bb[i] & -gs.piece_bb[i];
      gs.piece_bb[i] ^= lsb_bb; // Remove the piece

      int final_score = getFAMEMaterialScore(gs, FULL_BB);
      dynamic_piece_value[i] = final_score - initial_score;

      gs.piece_bb[i] ^= lsb_bb; // Restore the piece
    }

    if (createText) {
      for (int i = 0; i < 12; i++) {
        eval_text += "Piece value " + i + " = " + dynamic_piece_value[i] + "\n";
      }
    }

  }


  /**
   * Computes material score from white's POV
   * Uses Fritzlein's FAME system
   * @return int
   */


  private int white_pieces[] = new int[16];
  private int black_pieces[] = new int[16];
  private static final int fame_value[] = {256, 85, 57, 38, 25, 17, 11, 7};


  /**
   * Computes FAME material score from white's POV
   * Uses Fritzlein's FAME system
   * @return int
   */

  private int getFAMEMaterialScore(GameState gs, long mask_bb) {
    int score = 0;

    // Clear the data
    Arrays.fill(white_pieces, 0);
    Arrays.fill(black_pieces, 0);

    // Scan the board for white piece types
    int white_men_count = 0;
    int black_men_count = 0;

    for (int piece_type = 6; piece_type >= 1; piece_type--) {

      // Get white pieces
      long temp_piece_bb = gs.piece_bb[piece_type * 2 - 2] & mask_bb;
      int count = Util.PopCnt(temp_piece_bb);
      for (int i = 0; i < count; i++) {
        white_pieces[white_men_count++] = piece_type;
      }

      // Get black pieces
      temp_piece_bb = gs.piece_bb[piece_type * 2 - 1] & mask_bb;
      count = Util.PopCnt(temp_piece_bb);
      for (int i = 0; i < count; i++) {
        black_pieces[black_men_count++] = piece_type;
      }
    }

    // Run the comparisons
    int index = 0;

    // Only compare until pieces are finished
    while (white_pieces[index] > 1 || black_pieces[index] > 1) {
      if ( white_pieces[index] > black_pieces[index] ) {
        score += fame_value[index];
        if (createText) {
          eval_text += "Matchup: " + index + " Score: " + fame_value[index] + "\n";
        }
      }
      if ( black_pieces[index] > white_pieces[index] ) {
        score -= fame_value[index];
        if (createText) {
          eval_text += "Matchup: " + index + " Score: " + -fame_value[index] + "\n";
        }
      }

      index++;
    }

    // Get rabbit values
    int actual_white_rabbit_count = Math.max(Util.PopCnt(gs.piece_bb[0]), 1);
    int actual_black_rabbit_count = Math.max(Util.PopCnt(gs.piece_bb[1]), 1);
    int white_piece_count = white_men_count - actual_white_rabbit_count;
    int black_piece_count = black_men_count - actual_black_rabbit_count;
    int white_rabbit_value = 600 /
      (2 * black_piece_count + actual_black_rabbit_count);
    int black_rabbit_value = 600 /
      (2 * white_piece_count + actual_white_rabbit_count);

    int white_rabbits = white_men_count - index;
    int black_rabbits = black_men_count - index;

    score += white_rabbits * white_rabbit_value;
    score -= black_rabbits * black_rabbit_value;

    if (createText) {
      eval_text += "White Rabbits: " + white_rabbits + " Value: " +
        white_rabbit_value + "\n";
      eval_text += "Black Rabbits: " + black_rabbits + " Value: " +
        black_rabbit_value + "\n";
      eval_text += "FAME Score: " + score + "\n";
    }

    // We are done
    return score;
  }




  /**
   * For now just gets FAME value of position
   * @param gs GameState
   * @return int
   */
  private int getPieceLocationEvaluation(GameState gs) {

    int score = 0;

    // Constant is a factor to give some range for positional scores
    // Rabbit is worth 33 FAME at start of game, converts to bigger range eval points
    score += 80 * getFAMEMaterialScore(gs, FULL_BB);
    if (createText) {
      eval_text += "FAME Material Score: " + score + "\n";
    }

    return score;
  }


  public static void testFAME() {
    ArimaaEvaluate2 eval = new ArimaaEvaluate2();

    long total_start_time = System.currentTimeMillis();

    for (String temp : text) {
      GameState position = new GameState(temp);
      System.out.println(position.toBoardString());

      long start_time = System.currentTimeMillis();




      for ( int i=0; i<1000000; i++ ) {
      int score = eval.getFAMEMaterialScore(position, FULL_BB);
      }

/*
      eval.createText = true;
      eval.eval_text = "";

      int score = eval.getFAMEMaterialScore(position,FULL_BB);
      System.out.println("Returned: " + score);

      int new_score = eval.getFAMEMaterialScoreNew(position,FULL_BB);
      System.out.println("Returned: " + new_score);

      System.out.println(eval.getEvalText());
*/

      long elapsed_time = System.currentTimeMillis() - start_time;
      System.out.println("Time: "+elapsed_time);

    }

    long total_elapsed_time = System.currentTimeMillis() - total_start_time;
    System.out.println("Time: "+total_elapsed_time);


  }


  private static void testEval() {
    ArimaaEvaluate2 eval = new ArimaaEvaluate2();

    for (String temp : text) {
      GameState position = new GameState(temp);

      System.out.println(position.toBoardString());

      eval.PreProcessRootPosition(position);

      int score = eval.Evaluate(position, true);
      System.out.println(eval.getEvalText());

      int score2 = eval.Evaluate(position, false);

      position.mirror();
      eval.PreProcessRootPosition(position);
      int score3 = eval.Evaluate(position, false);

      position.rotate();
      eval.PreProcessRootPosition(position);
      int score4 = eval.Evaluate(position, false);

      position.mirror();
      eval.PreProcessRootPosition(position);
      int score5 = eval.Evaluate(position, false);

      assert (score == score2);
      assert (score == score3);
      assert (score == score4);
      assert (score == score5);

//      eval.test_immobilization();


      System.out.println("Returned: " + score);
    }

  }


  private static String text[] = {
    "12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
    "12w %13 +-----------------+%138|                 |%137| R               |%136|                 |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
    "12w %13 +-----------------+%138|                 |%137|   R             |%136|                 |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",



    "6b %13 +-----------------+%138| r r r r r r r r |%137|     d     m     |%136| h c         h d |%135|                 |%134|           E     |%133|       D e c H   |%132| R   D   C C   R |%131| R R R M   R R R |%13 +-----------------+%13   a b c d e f g h%13",

    "2w %13 +-----------------+%138| r r r r r r r r |%137| h d c e m c d h |%136|                 |%135|                 |%134|                 |%133|                 |%132| R H C D H C E R |%131| R R R M D R R R |%13 +-----------------+%13   a b c d e f g h%13",

    "4b %13 +-----------------+%138| r r r m   r r r |%137| r h d d c c   r |%136|       e     h   |%135|       E         |%134|                 |%133|   H         H   |%132| R D   M C D   R |%131| R R R C   R R R |%13 +-----------------+%13   a b c d e f g h%13",

    "6w %13 +-----------------+%138| r r r r r r r r |%137|     h     d h   |%136|   c     m   c   |%135|                 |%134|     E           |%133| d H   e     H   |%132| R     C D C   R |%131| R R R D M R R R |%13 +-----------------+%13   a b c d e f g h%13",

    "4b %13 +-----------------+%138| r r r   m r r r |%137|   r c d   d h r |%136|   h     E e H   |%135|         c       |%134|                 |%133|                 |%132| R H C M   C   R |%131| R R R D D R R R |%13 +-----------------+%13   a b c d e f g h%13",

    "4b %13 +-----------------+%138| r r r m d r r r |%137| r   c     c   r |%136|   h         h   |%135|                 |%134|       e E       |%133|         d   H   |%132| R H C M   C   R |%131| R R R D D R R R |%13 +-----------------+%13   a b c d e f g h%13",

    /*
        "36w %13 +-----------------+%138|   r r           |%137| r M r h   d r r |%136|   e R E c       |%135|                 |%134|                 |%133| D       H     R |%132|       C         |%131| R R     R R R   |%13 +-----------------+%13   a b c d e f g h%13",
        "36w %13 +-----------------+%138|     r           |%137| r M r h   d r r |%136|   e R E c       |%135|                 |%134|                 |%133| D       H     R |%132|       C         |%131| R R     R R R   |%13 +-----------------+%13   a b c d e f g h%13",
        "36w %13 +-----------------+%138|                 |%137| r M r h   d r r |%136|   e R E c       |%135|                 |%134|                 |%133| D       H     R |%132|       C         |%131| R R     R R R   |%13 +-----------------+%13   a b c d e f g h%13",
        "36w %13 +-----------------+%138|                 |%137| r M r     d r r |%136|   e R E c       |%135|                 |%134|                 |%133| D       H     R |%132|       C         |%131| R R     R R R   |%13 +-----------------+%13   a b c d e f g h%13",
        "2w %13 +-----------------+%138|   r r r r r r r |%137| h d c e m c d h |%136|                 |%135|                 |%134|                 |%133|                 |%132| R H C D H C E R |%131| R R R M D R R R |%13 +-----------------+%13   a b c d e f g h%13",
        "2w %13 +-----------------+%138| r r r r r r r r |%137| h d   e m c d h |%136|                 |%135|                 |%134|                 |%133|                 |%132| R H C D H C E R |%131| R R R M D R R R |%13 +-----------------+%13   a b c d e f g h%13",
        "2w %13 +-----------------+%138| r r r r r r r r |%137| h   c e m c d h |%136|                 |%135|                 |%134|                 |%133|                 |%132| R H C D H C E R |%131| R R R M D R R R |%13 +-----------------+%13   a b c d e f g h%13",
        "2w %13 +-----------------+%138| r e             |%137|                 |%136|                 |%135|                 |%134|                 |%133|                 |%132| R H C D H C E R |%131| R R R M D R R R |%13 +-----------------+%13   a b c d e f g h%13",
        "72b %13 +-----------------+%138| r   r c       r |%137|   r r   E       |%136|       r r       |%135| h               |%134|               d |%133|             C R |%132| H   e R   D   R |%131|   R             |%13 +-----------------+%13   a b c d e f g h%13",
        "6w %13 +-----------------+%138| r r r r r r r   |%137|   c d     h     |%136|   h   d E   c   |%135|             m r |%134|         e       |%133|   H         H   |%132| R D C     C R   |%131| R R R M D R R R |%13 +-----------------+%13   a b c d e f g h%13",

        "22b %13 +-----------------+%138| r r r m   r r r |%137| r   M   c c     |%136|   R   E         |%135|     d H d     r |%134| R C H h       D |%133|   e         h   |%132|     R       D   |%131| R R R       R   |%13 +-----------------+%13   a b c d e f g h%13",
        "22w %13 +-----------------+%138| r r r         r |%137|     d       r r |%136|       c E r e M |%135|     c   d H   C |%134|     r           |%133|   h   h     H   |%132|       m   D   R |%131| R   D R     R R |%13 +-----------------+%13   a b c d e f g h%13",
    */

    "19b %13 +-----------------+%138| r r r   d   r r |%137| r   c     m r r |%136|       d E H h   |%135|           e R c |%134|     D         M |%133|   H         C   |%132| R     h   D     |%131| R R R C   R R R |%13 +-----------------+%13   a b c d e f g h%13",
    // Elephant blockade

    "20b %13 +-----------------+%138| r r   m r   r r |%137| r R E M r d   r |%136|   d   e D   h   |%135| D         H     |%134|                 |%133|   R         C   |%132|     R     H h   |%131|       R R   R R |%13 +-----------------+%13   a b c d e f g h%13",

    /*
       "28w %13 +-----------------+%138| r r r       r r |%137| C   c r       D |%136|       d M     R |%135| C R   R   E h   |%134| R       H e     |%133|       h         |%132|             R   |%131|           R     |%13 +-----------------+%13   a b c d e f g h%13",

       "22b %13 +-----------------+%138| r r H r r r r r |%137| c   m E   d     |%136|   d R D h   c   |%135|             H   |%134| r M e           |%133| h               |%132| D             D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",

       "10w %13 +-----------------+%138| r r r     r r r |%137| r   m c c h   r |%136|   d     E C d   |%135|     h       C   |%134|     D e D       |%133|                 |%132| H   M         H |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",

       "17b %13 +-----------------+%138| r r   c h r r r |%137|     m r       d |%136|   d         H   |%135|       r E D e C |%134|           H M h |%133|                 |%132| R D     R R   R |%131| R   R R     R   |%13 +-----------------+%13   a b c d e f g h%13",
       "9w %13 +-----------------+%138| r r r     r r r |%137|   r c d   h r   |%136| M     h d   c   |%135|                 |%134|             E m |%133|         H C C H |%132|         D D e   |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "9w %13 +-----------------+%138| r r r     r r r |%137|   r c d   h r   |%136| M     h d   c   |%135|                 |%134|             E m |%133|         H C C H |%132|     D     D e   |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "9w %13 +-----------------+%138| r r r r r r r r |%137| h M c m   E   d |%136|   e     d   h   |%135|     D     H     |%134|           c     |%133|   C             |%132|       H C     D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",

       "13b %13 +-----------------+%138| r     r r   r r |%137|     r     r     |%136|   d   m E   h   |%135|         c   H   |%134|     r M         |%133|   h   e         |%132| C   H   D     C |%131| R R   R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "13b %13 +-----------------+%138| r     r r   r r |%137|     r     r     |%136|   d   m E   h   |%135|         c   H   |%134|     r M         |%133|   h   e         |%132| C H R   D     C |%131| R R   R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "13w %13 +-----------------+%138| r     r r   r r |%137|     r     r     |%136|   d   m E   h   |%135|         c   H   |%134|     r M         |%133|   h   e         |%132| C H     D     C |%131| R R   R R R R R |%13 +-----------------+%13   a b c d e f g h%13",

       "20b %13 +-----------------+%138| r r r r   r r r |%137| d     r   c     |%136|   D c d     h   |%135| R h E m         |%134|     H e         |%133|   R   M H   C R |%132|         R       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
       "20b %13 +-----------------+%138| r r r r   r r r |%137| d       r c     |%136|   D c d m   h   |%135| R h E e         |%134|     H           |%133|   R   M H   C R |%132|         R       |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",

       "22w %13 +-----------------+%138| r   r     r     |%137|       r r d r   |%136|   h r c h       |%135|                 |%134|     e   E c     |%133|       d m   C r |%132|         R M H D |%131|         R R     |%13 +-----------------+%13   a b c d e f g h%13",

       "23w %13 +-----------------+%138| r   r     r     |%137|       r r d r   |%136|   h   c h       |%135|     r           |%134|     e   E c     |%133|       d   M C r |%132|       R   m H   |%131|         R R   D |%13 +-----------------+%13   a b c d e f g h%13",

       "23w %13 +-----------------+%138| r r H r r r r r |%137| c     m E d     |%136|   d     h   c   |%135|             H   |%134| r M e           |%133|   h             |%132| D   C         D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "22b %13 +-----------------+%138| r r H r r r r r |%137| c     m E d     |%136|   d     h   c   |%135|             H   |%134| r M e           |%133| h               |%132| D   C         D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "22w %13 +-----------------+%138| r r H r r r r r |%137| c   m E   d     |%136|   d     h   c   |%135|             H   |%134| r M e           |%133| h               |%132| D   C         D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "22b %13 +-----------------+%138| r r   c r r r r |%137|         h h d c |%136|   E             |%135|                 |%134|                 |%133| d D     M   D   |%132| H   C   e C     |%131| R R R R R   R R |%13 +-----------------+%13   a b c d e f g h%13",

       "5b %13 +-----------------+%138| r r r r r r r r |%137| d h c   d c h   |%136|   H             |%135|                 |%134|       E       D |%133|       m     e R |%132| D R C M   C H   |%131| R   R R R R R   |%13 +-----------------+%13   a b c d e f g h%13",
       "17b %13 +-----------------+%138| r h r r r r r r |%137|   H     d c h   |%136|   C   E         |%135|     R           |%134|         M d   D |%133| D       R r R R |%132| R e       H     |%131|     R     R     |%13 +-----------------+%13   a b c d e f g h%13",

       "21w %13 +-----------------+%138|                 |%137|   r r     r     |%136| r d   r c   E r |%135| R   h r R e     |%134|       R R   R   |%133|                 |%132|   H D     H   r |%131| C R           C |%13 +-----------------+%13   a b c d e f g h%13",

       "16b %13 +-----------------+%138| r r c   r     r |%137| h d   r m c h R |%136|       e   r     |%135|   r   r   C H   |%134|     H R E M d   |%133| C D         D   |%132| R R R     R R   |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
       "37b %13 +-----------------+%138|       r r   r   |%137|                 |%136|             c   |%135| r             r |%134|       E   e     |%133| r               |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
       "24b %13 +-----------------+%138|     c r r   r C |%137| d R   r R c R m |%136| R r r         r |%135|   h H           |%134|   C e     E   h |%133|       H D     R |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
       "37b %13 +-----------------+%138|       r r   r   |%137|                 |%136|             c   |%135| r             r |%134|       E   e     |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
       "35b %13 +-----------------+%138|       r r   r   |%137|               r |%136|   d         c R |%135| r E             |%134|           e     |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",


       "3b %13 +-----------------+%138| E       D       |%137| R R R           |%136| e           c c |%135| r r             |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
       "20w %13 +-----------------+%138|                 |%137|   r             |%136|   h   E M   r   |%135|   D d     H r   |%134|       H   c     |%133|       C     C   |%132| R r     D     R |%131| R R R     R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "3b %13 +-----------------+%138| r r r c m r r r |%137| r h c d   d   r |%136|         E       |%135|         h       |%134|             e   |%133|   D         H   |%132| R   H   M D   R |%131| R R R C C R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "4b %13 +-----------------+%138| r r r   m r r r |%137| r h c c       r |%136|       d     d   |%135|                 |%134|         E   e   |%133|   D     h   H   |%132| R   H   M D   R |%131| R R R C C R R R |%13 +-----------------+%13   a b c d e f g h%13",
       "5b %13 +-----------------+%138| r r r     r r r |%137| r h c c   m   r |%136|       d     d   |%135|                 |%134|   D   E         |%133|       h M H e   |%132| R   H     D   R |%131| R R R C C R R R |%13 +-----------------+%13   a b c d e f g h%13",
    */
  };


// Test stub for eval function
  public static void main(String args[]) {


    testEval();
    //testFAME();

  }

}
