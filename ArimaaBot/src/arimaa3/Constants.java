package arimaa3;

/**
 * Global constants used by arimaa3
 */
public interface Constants {

  public final int PL_WHITE = 0;
  public final int PL_BLACK = 1;


  // Permissable piece types
  public final int PT_WHITE_RABBIT = 0;
  public final int PT_BLACK_RABBIT = 1;
  public final int PT_WHITE_CAT = 2;
  public final int PT_BLACK_CAT = 3;
  public final int PT_WHITE_DOG = 4;
  public final int PT_BLACK_DOG = 5;
  public final int PT_WHITE_HORSE = 6;
  public final int PT_BLACK_HORSE = 7;
  public final int PT_WHITE_CAMEL = 8;
  public final int PT_BLACK_CAMEL = 9;
  public final int PT_WHITE_ELEPHANT = 10;
  public final int PT_BLACK_ELEPHANT = 11;


  // A bunch of useful bitboards
// Useful bitboards
  public static final long RANK_1 = 0x00000000000000FFL;
  public static final long RANK_2 = 0x000000000000FF00L;
  public static final long RANK_3 = 0x0000000000FF0000L;
  public static final long RANK_4 = 0x00000000FF000000L;
  public static final long RANK_5 = 0x000000FF00000000L;
  public static final long RANK_6 = 0x0000FF0000000000L;
  public static final long RANK_7 = 0x00FF000000000000L;
  public static final long RANK_8 = 0xFF00000000000000L;
  public static final long FILE_A = 0x0101010101010101L;
  public static final long FILE_H = 0x8080808080808080L;

// All kinds of stuff for the trap eval
  public static final long LL_TRAP = 0x0000000000040000L;
  public static final long UL_TRAP = 0x0000040000000000L;
  public static final long LR_TRAP = 0x0000000000200000L;
  public static final long UR_TRAP = 0x0000200000000000L;

  public static final long LL_TRAP_RING = 0x000000000E0A0E00L;
  public static final long UL_TRAP_RING = 0x000E0A0E00000000L;
  public static final long LR_TRAP_RING = 0x0000000070507000L;
  public static final long UR_TRAP_RING = 0x0070507000000000L;

  public static final long LL_TOUCH1_TRAP = GameState.touching_bb(LL_TRAP);
  public static final long UL_TOUCH1_TRAP = GameState.touching_bb(UL_TRAP);
  public static final long LR_TOUCH1_TRAP = GameState.touching_bb(LR_TRAP);
  public static final long UR_TOUCH1_TRAP = GameState.touching_bb(UR_TRAP);

  public static final long LL_TOUCH2_TRAP = GameState.touching_bb(
    LL_TOUCH1_TRAP) & ~LL_TRAP;
  public static final long UL_TOUCH2_TRAP = GameState.touching_bb(
    UL_TOUCH1_TRAP) & ~UL_TRAP;
  public static final long LR_TOUCH2_TRAP = GameState.touching_bb(
    LR_TOUCH1_TRAP) & ~LR_TRAP;
  public static final long UR_TOUCH2_TRAP = GameState.touching_bb(
    UR_TOUCH1_TRAP) & ~UR_TRAP;


  public static final long LL_CONTROL = LL_TOUCH1_TRAP & ~UL_TOUCH2_TRAP;
  public static final long UL_CONTROL = UL_TOUCH1_TRAP & ~LL_TOUCH2_TRAP;
  public static final long LR_CONTROL = LR_TOUCH1_TRAP & ~UR_TOUCH2_TRAP;
  public static final long UR_CONTROL = UR_TOUCH1_TRAP & ~LR_TOUCH2_TRAP;

  public static final long LL_TOUCH3_TRAP = GameState.touching_bb(
    LL_TOUCH2_TRAP) & ~LL_TOUCH1_TRAP;
  public static final long UL_TOUCH3_TRAP = GameState.touching_bb(
    UL_TOUCH2_TRAP) & ~LL_TOUCH1_TRAP;
  public static final long LR_TOUCH3_TRAP = GameState.touching_bb(
    LR_TOUCH2_TRAP) & ~LL_TOUCH1_TRAP;
  public static final long UR_TOUCH3_TRAP = GameState.touching_bb(
    UR_TOUCH2_TRAP) & ~LL_TOUCH1_TRAP;

  public static final long LL_QUADRANT = 0x000000000F0F0F0FL;
  public static final long UL_QUADRANT = 0x0F0F0F0F00000000L;
  public static final long LR_QUADRANT = 0x00000000F0F0F0F0L;
  public static final long UR_QUADRANT = 0xF0F0F0F000000000L;


  public static final long TRAP[] = {
    LL_TRAP, UL_TRAP, LR_TRAP, UR_TRAP};
  public static final int TRAP_INDEX[] = {
    18, 42, 21, 45};
  public static final long TOUCH_TRAP[] = {
    LL_TOUCH1_TRAP, UL_TOUCH1_TRAP, LR_TOUCH1_TRAP, UR_TOUCH1_TRAP};
  public static final long TOUCH2_TRAP[] = {
    LL_TOUCH2_TRAP, UL_TOUCH2_TRAP, LR_TOUCH2_TRAP, UR_TOUCH2_TRAP};
  public static final long TOUCH3_TRAP[] = {
    LL_TOUCH3_TRAP, UL_TOUCH3_TRAP, LR_TOUCH3_TRAP, UR_TOUCH3_TRAP};
  public static final long TRAP_RING[] = {
    LL_TRAP_RING, UL_TRAP_RING, LR_TRAP_RING, UR_TRAP_RING};
  public static final long QUADRANT[] = {
    LL_QUADRANT, UL_QUADRANT, LR_QUADRANT, UR_QUADRANT};
  public static final long CONTROL[] = {
    LL_CONTROL, UL_CONTROL, LR_CONTROL, UR_CONTROL};


// Used to determine if a move can effect a trap square
  int trap_number[] = {
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, 00, -1, -1, 02, -1, -1,
    -1, 00, -1, 00, 02, -1, 02, -1,
    -1, -1, 00, -1, -1, 02, -1, -1,
    -1, -1, 01, -1, -1, 03, -1, -1,
    -1, 01, -1, 01, 03, -1, 03, -1,
    -1, -1, 01, -1, -1, 03, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
  };

  public static final long TRAP_SQUARES = 0x0000240000240000L;
  public static final long TOUCH_TRAPS = GameState.touching_bb(TRAP_SQUARES);
  public static final long SMALL_CENTRE = 0x0000001818000000L;
  public static final long LARGE_CENTRE = 0x00003C3C3C3C0000L;
  public static final long OUTSIDE_EDGE = 0xFF818181818181FFL;

  public static final long NOT_FILE_A = ~FILE_A;
  public static final long NOT_FILE_H = ~FILE_H;
  public static final long NOT_RANK_1 = ~RANK_1;
  public static final long NOT_RANK_2 = ~RANK_2;
  public static final long NOT_RANK_3 = ~RANK_3;
  public static final long NOT_RANK_4 = ~RANK_4;
  public static final long NOT_RANK_5 = ~RANK_5;
  public static final long NOT_RANK_6 = ~RANK_6;
  public static final long NOT_RANK_7 = ~RANK_7;
  public static final long NOT_RANK_8 = ~RANK_8;

  public static final long FULL_BB = ~0L;

  // Saves typing
  public static final boolean TR = true;
  public static final boolean FL = false;

  // Values used in alpha/beta search
  public static final int SCORE_UNKNOWN = 11111111;
  public static final int SCORE_INFINITY = 100000;
  public static final int SCORE_MATE = 32767; // 32768 or higher breaks hash table implementation
  public static final int SCORE_DRAW = 0;
  public static final int SCORE_FORCED_WIN = 30000;
  public static final int SCORE_FORCED_LOSS = -SCORE_FORCED_WIN;

  public static final int MAX_PLY = 100; // Maximum number of ply engine will search


}
