package feature_extractor;
import java.util.ArrayList;
import java.util.BitSet;

import feature_extractor.FeatureConstants.TrapStatus;
import ai_util.LogFile;
import arimaa3.*;

public class FeatureExtractor implements Constants {
	
	private static final int NUM_FEATURES = 1040; //TODO update this as you add more features
	
	/* Move for which we are extracting features. This changes every time extractFeatures() is called. */
	private ArimaaMove current_move;
	
	/* Game state resulting from current_move. This changes every time extractFeatures() is called. */
	private GameState curr;
	
	/* Starting game state from which we play current_move */
	private GameState prev;
	
	private GameState prev_prev;
	
	/* Piece id to type mapping for the current game state (curr). E.g. if piece_types[3] = 4, this means
	 * that piece id 3 (black cat) is of piece type 4 for the current GameState. This changes every time 
	 * extractFeatures() is called. */
	private byte piece_types[];
	
	/* The feature vector corresponding to current_move. */ 
	BitSet featureVector;
		
	public FeatureExtractor(GameState prev, GameState prev_prev) {
		this.prev = prev;
		this.prev_prev = null;
		curr = null;
		featureVector = null;
		current_move = null;
		piece_types = null; 
	}

	/*
	 * Extracts features for the resulting board after playing a possible legal move.
	 * current_board is the resulting board after playing current_move on prev game state.
	 */
	public BitSet extractFeatures(ArimaaMove current_move){
		featureVector = new BitSet(NUM_FEATURES);
		piece_types = new byte[12];

		this.current_move = current_move;

		// Generate the current game state by applying move on the previous game state
		curr = new GameState();
		curr.playFullClear(current_move, prev);
		calculatePieceTypes();

		// feature extraction subroutine calls here
		
		(new PositionMovementExtractor(prev, curr, current_move, piece_types)).updateBitSet(featureVector);
		return featureVector;
	}
	
	/*
	 * Maps from board index between 0-63 to board index between 0-31, leveraging 
	 * the vertical symmetry of the board. 
	 */
	public static int getLocation(int index) {
		int row = index >> 3;
		index = index & 0x07; //reduces all rows to the same indices as first row
		index = (index > 3) ? 7 - index : index;
		return index + (row << 2);
	}

	// Calculates the piece type (e.g. 3) for each piece id (e.g. black dog) for the current game state.
	private void calculatePieceTypes(){
		
		for (int i = 0; i < 2; i++){ // calculate for rabbits 
			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
			if (numStronger < 5)
				piece_types[i] = 7;
			else if (numStronger < 7)
				piece_types[i] = 6;
			else
				piece_types[i] = 5;
		}
		
		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
			switch (numStronger) {
				case 0: piece_types[i] = 0; break;
				case 1: piece_types[i] = 1; break;
				case 2: piece_types[i] = 2; break;
				case 3: case 4: piece_types[i] = 3; break;
				default: piece_types[i] = 4; break;
			}
		}
	}
	
	public static byte countOneBits(long n) {
		// Algorithm adapted from http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetKernighan
		byte c; // c accumulates the total bits set in v
		for (c = 0; n != 0; c++)
			n &= n - 1; // clear the least significant bit set
		return c;
	}
	
	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
	};

	private static void testMovementFeatures1() {
		
		// data from first game in "games" relation
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState startState = new GameState(white,black);
	    FeatureExtractor fe = new FeatureExtractor(startState, null);
	    fe.extractFeatures(new ArimaaMove("Db2n Ha2n Ha3n Hh2n"));
	    
	    System.out.println(fe.featureVector.toString());
	    
	    assert(fe.featureVector.get(620));
	    assert(fe.featureVector.get(68));
	    assert(fe.featureVector.get(101));
	    assert(fe.featureVector.get(586));
	    assert(fe.featureVector.get(590));
	    
	    System.out.println(startState.toBoardString());
	    System.out.println(fe.curr.toBoardString());
	}
	
	private static void testMovementFeatures2() {
	    GameState startState = new GameState(tests[2]);
	    FeatureExtractor fe = new FeatureExtractor(startState, null);
	    fe.extractFeatures(new ArimaaMove("db6e dc6x ce5s ce4s ce3s"));

	    assert(fe.featureVector.get(339));
	    assert(fe.featureVector.get(341));
	    assert(fe.featureVector.get(849));
	    assert(fe.featureVector.get(874));
	    
	    System.out.println(fe.featureVector.toString());
	    System.out.println(startState.toBoardString());
	    System.out.println(fe.curr.toBoardString());
	}
	
	private static void testLocationMappings(){
		// VM needs argument -ea for asserts to be enabled
		assert(getLocation(0) == 0); 
		assert(getLocation(5) == 2);
		assert(getLocation(10) == 6);
		assert(getLocation(15) == 4);
		assert(getLocation(57) == 29);
	}
	
	
	private static void testElephantTouchingTraps() {
		long neighboringTrapSpots = 0;
		for (int trap = 0; trap < TOUCH_TRAP.length; trap++) 
			neighboringTrapSpots |= TOUCH_TRAP[trap];
		
		GameState gsWhite = new GameState();
		GameState gsBlack = new GameState();
		
		for (int board = 0; board < 64; board++) { //check 64 boards, elephant in different spot each time
			//System.out.println("\n\n ----- BOARD " + board + " ----- \n");
			
			gsWhite.piece_bb[PT_WHITE_ELEPHANT] = 1L << board;
			gsBlack.piece_bb[PT_BLACK_ELEPHANT] = 1L << board;
			//System.out.println(gsWhite.toBoardString());
			//System.out.println(gsBlack.toBoardString());
			boolean whiteEleph = false, blackEleph = false;
			for (int trap = 0; trap < 4; trap++) {
				whiteEleph = whiteEleph || TrapExtractor.isElephantTouchingTrap(gsWhite, trap, PL_WHITE);
				blackEleph = blackEleph || TrapExtractor.isElephantTouchingTrap(gsBlack, trap, PL_BLACK);
			}
			
			boolean onTrap = (neighboringTrapSpots & (1L << board)) != 0;
			if (onTrap != whiteEleph)
				System.err.printf("White elephant: board %d, position %x. onTrap is %b\n", board, 1L << board, onTrap);
			if (onTrap != blackEleph)
				System.err.printf("Black elephant: board %d, position %x. onTrap is %b\n", board, 1L << board, onTrap);
		}
	}
	
	private static void testGetTrapStatus() {
		String white1 = "Eb3 Rc2"; //expect TWO_E == 4
		String black1 = "ed3 rc4"; //expect TWO_E == 4
		
		String white2 = "Rf6 Df5 Cf7"; //expect TWO_NE == 3
		String black2 = "rf6 df5 cf7"; //expect TWO_NE == 3
		
		String mixedW1 = "Ec7 Dd6 Cc5"; //expect THREE_E == 6
		String mixedB1 = "rb6"; //expect ONE_NE == 1
		
		GameState gs1 = new GameState(white1, black1);
		GameState gs2 = new GameState(white2, black2);
		GameState gsMixed = new GameState(mixedW1, mixedB1);
		
		ArrayList<GameState> arr = new ArrayList<GameState>();
		arr.add(gs1); arr.add(gs2); arr.add(gsMixed);
		
		int count = 1;
		for (GameState gs : arr) {
			for (int trap = 0; trap < 4; trap++) {
				byte wStatus = TrapExtractor.getTrapStatus(gs, trap, PL_WHITE);
				byte bStatus = TrapExtractor.getTrapStatus(gs, trap, PL_BLACK);
				
				System.out.printf("\nTrap %d, Game %d, Player WHITE, Status %d\n", trap, count, wStatus);
				System.out.printf("Trap %d, Game %d, Player BSTATUS, Status %d\n", trap, count, bStatus);
			}
			count++;
		}
	}
	
	private static void testBitCodes() {
		BitSet bitset = new BitSet(1552);
		
		String mixedW1 = "Ec7 Dd6 Cc5"; //expect THREE_E == 6
		String mixedB1 = "rb6"; //expect ONE_NE == 1
		
		GameState gsMixed = new GameState(mixedW1, mixedB1);
		
		TrapExtractor te = new TrapExtractor(gsMixed, gsMixed);
		te.updateBitSet(bitset);
		System.out.println(bitset);
		
	}
	
	public static void main(String[] args){
//		testLocationMappings();
//		testMovementFeatures1();
//		testMovementFeatures2();
		
		//vmnz tests
		testElephantTouchingTraps();
		testGetTrapStatus();
		testBitCodes();
	}

}