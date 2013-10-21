package arimaa3;

import java.util.*;

//import ai_util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TestGameState implements Constants {

  public TestGameState() {
  }

  // Hold move generation data
  private final int max_depth = 10;
  private final int max_moves = 100;

  private GameState stack[] = new GameState[max_depth];
  {
    for (int i = 0; i < stack.length; i++) {
      stack[i] = new GameState();
    }
  }


  private ArimaaMove total_move_list[][] = new ArimaaMove[max_depth][max_moves];
  {
    for (int i = 0; i < max_depth; i++) {
      for (int j = 0; j < max_moves; j++) {
        total_move_list[i][j] = new ArimaaMove();
      }
    }
  }

  private MoveList total_move_list2[] = new MoveList[max_depth];
  {
    for (int i = 0; i < max_depth; i++) {
      total_move_list2[i] = new MoveList(max_moves);
    }
  }

  private long total_moves = 0;


  void try_moves2(int ply, GameState gs) {
    if (ply == 0) {
      return;
    }
    MoveList move_list = total_move_list2[ply];
    GameState new_gs = stack[ply];

//    gs.genSlideMoves(move_list,FULL_BB, FULL_BB);
    gs.genAllMoves(move_list, FULL_BB, FULL_BB, FULL_BB, FULL_BB);

    for (ArimaaMove move : move_list) {
      total_moves++;
      new_gs.play(move, gs);
      //     System.out.println(new_gs.toBoardString());
      try_moves2(ply - 1, new_gs);
    }
  }


  private void test_movegen() {

    long s_time = System.currentTimeMillis();
    for (int i = 0; i < text.length; i++) {
      String pos_text = text[i];

      System.out.println(pos_text);
      GameState initial_gs = new GameState(pos_text);
      System.out.println(initial_gs.toBoardString());

      long start_time = System.currentTimeMillis();

      for (int ply = 1; ply <= 6; ply++) {
        total_moves = 0;
        try_moves2(ply, initial_gs);
        long end_time = System.currentTimeMillis();
        String result = "Ply: " + ply;
        result += " Time: " + (end_time - start_time);
        result += " Moves: " + total_moves;
        result += " kNPS: " +
          total_moves / (end_time - start_time + 1);

        System.out.println(result);
      }
    }

    long e_time = System.currentTimeMillis();
    System.out.println("Total Time: " + (e_time - s_time));

  }

  private void test_compress_move() {

    for( String pos_text : text ) {
      GameState gs = new GameState(pos_text);

      System.out.println(gs);
      MoveList move_list = total_move_list2[0];
      move_list.clear();
      gs.GENERATE_MOVES(move_list, FULL_BB, FULL_BB);
      for (ArimaaMove move : move_list) {
        System.out.println(move);
      }

    }
  }

  String text2[] = {
    "30b  rc5e rc5e rc5e%13 +-----------------+%138| r   E c r h r r |%137| R               |%136| C r r           |%135|       D         |%134|   H e           |%133|       H r   C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
    "30b  rc5e rc5e rc5e%13 +-----------------+%138| r   E c r h r r |%137| R   r           |%136| C   r           |%135|       D         |%134|   H e           |%133|       H r   C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
    "30b  rc5e rc5e rc5e%13 +-----------------+%138| r   E c r h r r |%137| R   r           |%136| C   r           |%135|   e   D         |%134|   H             |%133|       H r   C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
    "30b  rc5e rc5e rc5e%13 +-----------------+%138| r   E c r h r r |%137| R               |%136| C r r           |%135|       D         |%134|   H e r         |%133|       H     C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
    "30b  rc5e rc5e rc5e%13 +-----------------+%138| r   E c r h r r |%137| R   r           |%136| C   r           |%135|       D         |%134|   H e r         |%133|       H     C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
    "30b  rc5e rc5e rc5e%13 +-----------------+%138| r   E c r h r r |%137| R   r           |%136| C   r           |%135|   e   D         |%134|   H   r         |%133|       H     C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
  };

  String text[] = {
    "16b %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
    "16w %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
    "30b %13 +-----------------+%138| r   E c r h r r |%137| R r             |%136| C   r           |%135|     e D         |%134|   H     r       |%133|       H     C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
    "35b %13 +-----------------+%138|           r r   |%137|               r |%136|           R E   |%135|   d             |%134|     r   r       |%133| r M     D   e   |%132| R h d     D R   |%131|     R R       m |%13 +-----------------+%13   a b c d e f g h%13",
    "41b %13 +-----------------+%138|                 |%137|         r       |%136|         e   r   |%135|   R         D r |%134|     r     E     |%133| r               |%132| R M d     m     |%131|   h R R     R   |%13 +-----------------+%13   a b c d e f g h%13",
    "45b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|     R           |%134|     e   r   r   |%133| r     r E   D r |%132| R M   R m       |%131|   h R         R |%13 +-----------------+%13   a b c d e f g h%13",
    "46b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|         r   r   |%133| r   r   E   D r |%132| R M e R m     R |%131|   h R           |%13 +-----------------+%13   a b c d e f g h%13",
    "38w %13 +-----------------+%138|   c   r r       |%137| r   r       r   |%136|   r   H       d |%135| R h   h       R |%134| R   e         r |%133|           E C   |%132|                 |%131| R       R R   R |%13 +-----------------+%13   a b c d e f g h%13",
    "34w %13 +-----------------+%138|                 |%137|     r   M     r |%136|       H e   r R |%135|   h R     R     |%134| r C       r     |%133|       D         |%132|           d E m |%131|     R       R R |%13 +-----------------+%13   a b c d e f g h%13",
    "40w %13 +-----------------+%138|         M       |%137|     r H e     r |%136|   h         r R |%135|                 |%134| r D     R d m   |%133|   C         E   |%132|             R   |%131| R             R |%13 +-----------------+%13   a b c d e f g h%13",
    "15w %13 +-----------------+%138|   c r r r       |%137| r   r     r     |%136|   h         d   |%135|         d E   r |%134|       R   h r c |%133|     H   e     H |%132| D   D M     m C |%131| R R R R R   R R |%13 +-----------------+%13   a b c d e f g h%13",
    "2b %13 +-----------------+%138| r r r r r r r r |%137| d m c e h c h d |%136|                 |%135|       E         |%134|                 |%133|                 |%132| D H C M   C H D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13"
  };

  /*
    public void test_genturn(boolean print_moves) {

      long move_data[] = new long[100000];
      long move_data2[] = new long[100000];

      // Run test positions
      GenAllMoves test = new GenAllMoves();

      long start_time = System.currentTimeMillis();
      for (int j = 2; j <= 2; j++) {
        ArimaaPosition position = new ArimaaPosition(text[j]);
        ArimaaZobristBoard zobrist = new ArimaaZobristBoard(position);

        System.out.println("Position: " + j + "\n" + position.toBoardString());

        int number = test.genMoves(zobrist.cur_game_state, move_data, 0);
        System.out.println("Total moves: " + number);

        for (int i = 0; i < number; i++) {
          zobrist.play_push_pull(move_data[i]);
          int n2 = test.genMoves(zobrist.cur_game_state, move_data2, 0);
          if ( (i%100) == 0 ) {
            System.out.print("x");
          }
          zobrist.undo();
        }

      }
      long end_time = System.currentTimeMillis();
      System.out.println("\n");
      System.out.println(test.getStats());
      System.out.println("Elapsed Time: " +(end_time-start_time) );
   }
   */

  private void test_GENERATE() {
    String text[] = {
      "2w rc5e rc5e %13 +-----------------+%138| r r e H   r r r |%137|   e r R r   r   |%136| d e r   r       |%135|         e       |%134|       R         |%133|                 |%132|           r     |%131|       R h H   R |%13 +-----------------+%13   a b c d e f g h%13TS: 18%13",
      "2w rc5e rc5e %13 +-----------------+%138| r r e   H r r r |%137|   e r R r   r   |%136| d e r   r       |%135|         e       |%134|       R         |%133|                 |%132|           r     |%131|       R h H   R |%13 +-----------------+%13   a b c d e f g h%13TS: 18%13",
    };

    for (String position_text : text) {
      GameState position = new GameState(position_text);
      position.total_steps = 19;
      System.out.println(position);
      MoveList move_list = total_move_list2[0];
      move_list.clear();
      position.GENERATE_MOVES(move_list, FULL_BB, FULL_BB);
      for (ArimaaMove move : move_list) {
        System.out.println(move);
      }
    }
  }

  private void test_goal() {

    String text[] = {

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

    };

    test.use_debug = false;
    long start_time = System.currentTimeMillis();

    for (int seed = 1; seed < 1000; seed++) {
      Random random = new Random(seed);
      System.out.println("Seed: " + seed);
      for (String position_text : text) {
//      System.out.println(position_text);
        for (int x = 0; x < 107; x++) {
          GameState position = new GameState(position_text);
          // Modify a random number of squares
          for (int i = -1; i <= random.nextInt(14); i++) {
            int index = random.nextInt(64);
            int piece_type = random.nextInt(13) - 1;
            set_board(position, index, piece_type);
          }
          if (test.use_debug) {
            System.out.println("Bob\n" + position);
            System.out.println("\"" + position.toEPDString() + "\",");
          }
          master_position(position);
        }
      }
    }

    System.out.println("Agree: " + agree);
    System.out.println("Disagree: " + disagree);
    System.out.println("Time: "+(System.currentTimeMillis()-start_time));

  }

  private void set_board(GameState position, int index, int piece_type) {

    // Do not allow rabbits in the goal
    if (index >= 0 && index <= 7) {
      if (piece_type == 1) {
        return;
      }
    }
    if (index >= 56 && index <= 63) {
      if (piece_type == 0) {
        return;
      }
    }

    position.setPieceType(index, piece_type);
  }

  private void master_position(GameState position) {

    boolean result = engine.can_player_goal(position);
 test_position(position, result);

// No need to try other positions. That code works now
/*
        position.mirror();
        test_position(position, result);
        position.rotate();
        test_position(position, result);
        position.mirror();
        test_position(position, result);
  */
  }

  private long agree = 0;
  private long disagree = 0;
  private TestForGoal test = new TestForGoal();
  private ArimaaEngine engine = new ArimaaEngine();
  private boolean show_tests = false;


  private void test_position(GameState position, boolean result2) {
// Run test positions
    if (show_tests) {
      System.out.println(position.toBoardString());
      System.out.println("\"" + position.toEPDString() + "\",");
    }
    boolean result = test.test(position);
    engine.resetStats();
    if (show_tests) {
      System.out.println("FINAL: " + result + " Engine: " + result2);
      System.out.println(engine.getStats());
      System.out.flush();
    }
    if (result == result2) {
      agree++;
    } else {
      System.out.println("\"" + position.toEPDString() + "\",");
      System.out.println("\"" + position.toBoardString() + "\",");
      System.out.println("\"" + position.toSetupString() + "\",");
      System.out.println("Engine: "+result2+"Code: "+result);
      disagree++;

    }
  }


  private void test_engine() {
    String tests[] = {
      "2w %13 +-----------------+%138| r r D   r r h r |%137| e e r R r   r   |%136|   D R d H       |%135|   e R M e     e |%134|       R         |%133|             M   |%132|             R   |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
      /*
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

           "2w %13 +-----------------+%138| r r   r r r r r |%137|   R     e       |%136|     h           |%135|     r C c       |%134|                 |%133| C     R   c   e |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e       |%136| E   h           |%135|     r C c       |%134|                 |%133| C     R   c   e |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r   r r r r r |%137|     R           |%136|     h           |%135|     r C d       |%134|                 |%133| C     R   c   e |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r   r r r r r |%137|     R           |%136|     h           |%135|     r           |%134|     C d         |%133|           c   e |%132|     R           |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r   r r r r r |%137|     R           |%136|     h           |%135|     r           |%134|   C   d         |%133|           c   e |%132|     R           |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

           "2b %13 +-----------------+%138| m r   H r d r r |%137|   e   C r C r   |%136|   e R           |%135|   e R M e       |%134|   r   R r   r   |%133|       E r     H |%132|       d R     r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
           "2w %13 +-----------------+%138| r r r   r r r r |%137|     R   e   h   |%136| E   h           |%135|     r R c       |%134|     R M         |%133| C     H   c   e |%132|               H |%131|       C       m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2b %13 +-----------------+%138| r r r H r r r r |%137|   e   C r   r   |%136|   e             |%135|   e R M e d     |%134|   r   c r       |%133|       r r r   E |%132|       d E     r |%131| R R R M   R   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
           "2b %13 +-----------------+%138| r r   H r r r r |%137|   e   C R   r   |%136| r e             |%135|   e D M e     c |%134|   r   R r       |%133|       R r r   E |%132|       E R H   r |%131| R R R R   R   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
           "33b %13 +-----------------+%138|         d r     |%137|   r     M r r   |%136|   d   c     R c |%135|       c   E   c |%134| r   H         r |%133| D R R   D   r C |%132| r D m   e     r |%131| R R     R   R R |%13 +-----------------+%13   a b c d e f g h%13TS: 268%13",
           "2w %13 +-----------------+%138| r r   r r r r r |%137| d R     e       |%136|     h R         |%135|   m r C c       |%134| c               |%133| H     R   c   e |%132|                 |%131|   H   R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| e r   e r r r r |%137|       r R h r   |%136|       C     D   |%135| D   R h         |%134|         C       |%133|                 |%132|                 |%131|     m           |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| m r r   r r r r |%137|     R   e       |%136|     e     c     |%135|       R e       |%134|       M     c   |%133|       R H       |%132|     h           |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|     R   e   h   |%136| E   E e         |%135|     r R c       |%134|     R M     M   |%133| m     H   c   e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R         |%136|         D m r C |%135|                 |%134|     R e     M   |%133| m     H   c   e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R r r r r |%136|         D m r C |%135|       r r E     |%134|     R e r   M   |%133| m     H   c   e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R         |%136|         D m r C |%135|                 |%134|     R e     M   |%133| m     H   c   e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r m e   r r |%137|     e R R       |%136|         D m r c |%135|         r       |%134|     R e     H   |%133| m     H   c   e |%132|     R     R   H |%131| e     C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137| r   e R     H   |%136|       m D m r C |%135|                 |%134|     R r     M   |%133| m   r H h c   e |%132|           R M   |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R     H   |%136|   e     D m r C |%135|         m     H |%134| h   d e     M   |%133| m     H   c   M |%132|           R   H |%131|       C m     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r r   E r r |%137| D   r r R r r   |%136|       h h R D   |%135|   d R M r r     |%134|                 |%133|                 |%132|                 |%131|       R     C R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|   C e R     C   |%136|     e   D m r H |%135|         e       |%134|     R e   m M   |%133| m R   M   c   e |%132|           R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e r r r |%137|     e R     H   |%136|         D m c C |%135|         m       |%134|     R e   C M   |%133| m     H   R   e |%132|   H       R   H |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2b %13 +-----------------+%138| r r   C r r r r |%137|   e   C R   r   |%136| r e             |%135|   e D M e H   c |%134| C r   R r       |%133|     h R C r   E |%132|       E R H   r |%131| R R   R   E   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
           "2w %13 +-----------------+%138| r r r h   r r r |%137|     r m R r r   |%136|     D M h C d   |%135| h e R M e D     |%134|       R         |%133|                 |%132|                 |%131| e     R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R     C   |%136|         D m r C |%135|         m     E |%134|     R e     M   |%133| m     H   c   e |%132|         R R   H |%131|     D   h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   e   r r |%137|     e R     H   |%136|           m r C |%135| E   c           |%134|     R e     M R |%133| m     H   c   e |%132|       r   R   C |%131|       C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r r   r   r   |%137| e   e R     D   |%136|         D m r C |%135|         m M     |%134|     H e     M   |%133| m     H   c   e |%132|           R   H |%131|       C R     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2b %13 +-----------------+%138| r r   C r r r r |%137|   e   C R   r   |%136| r e             |%135|   e D M e H   c |%134| C r   R r       |%133|     h R C r   E |%132|       E R H   r |%131| R R   R   E   R |%13 +-----------------+%13   a b c d e f g h%13TS: 20%13",
           "2w %13 +-----------------+%138| r r r m e   r r |%137|     e R R       |%136|         D m r c |%135|         r       |%134|     R e     H   |%133| m     H   c   e |%132|     R     R   H |%131| e     C h     m |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",

           "2w %13 +-----------------+%138| r r r   r r     |%137|     R   e       |%136| E   h           |%135|     r C c       |%134|                 |%133| C     R   c r m |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
           "2w %13 +-----------------+%138| r r e H   r r r |%137|   e r   r   r   |%136| d e r   r       |%135|       R e       |%134|       R         |%133|                 |%132|           r     |%131|       R h H   R |%13 +-----------------+%13   a b c d e f g h%13TS: 16%13",
      */
    };

    engine.resetStats();

    for (String text : tests) {
      GameState position = new GameState(text);
      boolean result = engine.can_player_goal(position);
      System.out.println("Engine: " + result);
    }
    System.out.println(engine.getStats());

  }

  public static void main(String args[]) {

    TestGameState test = new TestGameState();
    test.test_compress_move();

//    test.test_GENERATE();
//    test.test_engine();

 //     test.test_goal();

//    test.test_movegen();
//    test.test_genturn(false);
//    test.test_slide();

  }

}
