package feature_extractor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.BitSet;

import org.junit.Test;

import utilities.GoalTestWrapper;
import arimaa3.ArimaaMove;
import arimaa3.Constants;
import arimaa3.GameState;

public class FeatureExtractorTest implements Constants, FeatureConstants {

	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   e             |%135| R               |%134|                 |%133| R               |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   E             |%135| r               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| r               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135| c               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| m E             |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| c               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
	};
	
	@Test
	public void testMovementFeatures1() {
		
		// data from first game in "games" relation
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState startState = new GameState(white,black);
	    FeatureExtractor fe = new FeatureExtractor(startState, null, null, null, null);
	    BitSet bs = fe.extractFeatures(new ArimaaMove("Db2n Ha2n Ha3n Hh2n"));
	    
//	    System.out.println(fe.featureVector.toString());

	    assertTrue(bs.get(620));
	    assertTrue(bs.get(68));
	    assertTrue(bs.get(101));
	    assertTrue(bs.get(586));
	    assertTrue(bs.get(590));
	    
//	    System.out.println(startState.toBoardString());
//	    System.out.println(fe.curr.toBoardString());
	}
	
	@Test
	public void testMovementFeatures2() {
	    GameState startState = new GameState(tests[2]);
	    FeatureExtractor fe = new FeatureExtractor(startState, null, null, null, null);
	    BitSet bs = fe.extractFeatures(new ArimaaMove("db6e dc6x ce5s ce4s ce3s"));

	    assertTrue(bs.get(339));
	    assertTrue(bs.get(341));
	    assertTrue(bs.get(849));
	    assertTrue(bs.get(874));
	    
//	    System.out.println(fe.featureVector.toString());
//	    System.out.println(startState.toBoardString());
//	    System.out.println(fe.curr.toBoardString());
	}
	
	@Test
	public void testLocationMappings(){

		assertTrue(FeatureExtractor.getLocation(0) == 0); 
		assertTrue(FeatureExtractor.getLocation(5) == 2);
		assertTrue(FeatureExtractor.getLocation(10) == 6);
		assertTrue(FeatureExtractor.getLocation(15) == 4);
		assertTrue(FeatureExtractor.getLocation(57) == 29);
	}
	
	
	@Test
	public void testElephantTouchingTraps() {
		long neighboringTrapSpots = 0;
		for (int trap = 0; trap < TOUCH_TRAP.length; trap++) 
			neighboringTrapSpots |= TOUCH_TRAP[trap];
		
		GameState gsWhite = new GameState();
		GameState gsBlack = new GameState();
		
		for (int board = 0; board < 64; board++) { //check 64 boards, elephant in different spot each time
			gsWhite.piece_bb[PT_WHITE_ELEPHANT] = 1L << board;
			gsBlack.piece_bb[PT_BLACK_ELEPHANT] = 1L << board;
			
			boolean whiteEleph = false, blackEleph = false;
			for (int trap = 0; trap < 4; trap++) {
				whiteEleph = whiteEleph || AbstractTrapExtractor.isElephantTouchingTrap(gsWhite, trap, PL_WHITE);
				blackEleph = blackEleph || AbstractTrapExtractor.isElephantTouchingTrap(gsBlack, trap, PL_BLACK);
			}
			
			boolean onTrap = (neighboringTrapSpots & (1L << board)) != 0;
			assertTrue(onTrap == whiteEleph);
			assertTrue(onTrap == blackEleph);
		}
	}
	
	@Test
	public void testGetTrapStatus() {
		// String array containing whiteMove, blackMove pairs (indices 2k, 2k+1)
		String[] whitesBlacks = {  
				"Eb3 Rc2", "ed3 rc4", //expect TWO_E == 4, //expect TWO_E == 4
				"Rf6 Df5 Cf7", "rf6 df5 cf7", //expect TWO_NE == 3, //expect TWO_NE == 3
				"Ec7 Dd6 Cc5", "rb6" //expect THREE_E == 6, //expect ONE_NE == 1
		};
		assertTrue(whitesBlacks.length % 2 == 0);
		
		byte[][] whitesBlacksStatuses = {
				{TrapStatus.TWO_E, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO}, {TrapStatus.TWO_E, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO},
				{TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.TWO_NE}, {TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.TWO_NE},
				{TrapStatus.ZERO, TrapStatus.THREE_E, TrapStatus.ZERO, TrapStatus.ZERO}, {TrapStatus.ZERO, TrapStatus.ONE_NE, TrapStatus.ZERO, TrapStatus.ZERO}
		};
		assertTrue(whitesBlacksStatuses.length == whitesBlacks.length);
		
		ArrayList<GameState> gsArr = new ArrayList<GameState>();
		for (int i = 0; i < whitesBlacks.length / 2; i++)
			gsArr.add(new GameState(whitesBlacks[i * 2], whitesBlacks[i * 2 + 1]));
		
		int game = 0;
		for (GameState gs : gsArr) {
			int gameW = game * 2;
			int gameB = gameW + 1;
			for (int trap = 0; trap < 4; trap++) {
				byte wStatus = AbstractTrapExtractor.getTrapStatus(gs, trap, PL_WHITE);
				byte bStatus = AbstractTrapExtractor.getTrapStatus(gs, trap, PL_BLACK);
				
				assertTrue(wStatus == whitesBlacksStatuses[gameW][trap]);
				assertTrue(bStatus == whitesBlacksStatuses[gameB][trap]);
			}
			game++;
		}
	}
	
	@Test
	public void testBitCodesForTrapStatus() { //TODO: Set the bit indices for the games--try different games?
		BitSet bitset = new BitSet(FeatureRange.TRAP_STATUS_END + 1);
		
		String[] whitesBlacks = {  
				"Eb3 Rc2", "ed3 rc4", //expect TWO_E == 4, //expect TWO_E == 4
				"Rf6 Df5 Cf7", "rf6 df5 cf7", //expect TWO_NE == 3, //expect TWO_NE == 3
				"Ec7 Dd6 Cc5", "rb6" //expect THREE_E == 6, //expect ONE_NE == 1
		};
		assertTrue(whitesBlacks.length % 2 == 0);
		
		int[][] bitIndices = { //each int[] should have 8 bits set, //white, black in each row
				{/* 8 indices */}, {/* 8 indices */}, //DO MATH for transition from game 1 to game 2
				{/* 8 indices */}, {/* 8 indices */}  //DO MATH for transition from game 2 to game 3
		};
		assertTrue(bitIndices.length == whitesBlacks.length - 2);
				
		ArrayList<GameState> gsArr = new ArrayList<GameState>();
		for (int i = 0; i < whitesBlacks.length / 2; i++)
			gsArr.add(new GameState(whitesBlacks[i * 2], whitesBlacks[i * 2 + 1]));
		
		
		int bitOffset = FeatureRange.TRAP_STATUS_START;
		int game = 0;
		GameState prev = null;
		for (GameState gs : gsArr) {
			//if prev is not set, use the first game and start back at the top
			if (prev == null) {
				prev = gs;
				continue;
			}
			int gameW = game * 2;
			int gameB = gameW + 1;
			
			TrapExtractor te = new TrapExtractor(prev, gs);
			te.updateBitSet(bitset);
			assertTrue(bitset.cardinality() == 8);
			
			//check whiteBits
			for (int bit = 0; bit < bitIndices[gameW].length; bit++) {
				assertTrue(bitset.get(bitOffset + bitIndices[gameW][bit]));
			}
			//check blackBits
			for (int bit = 0; bit < bitIndices[gameB].length; bit++) {
				assertTrue(bitset.get(bitOffset + bitIndices[gameB][bit]));
			}
			
			//handle increments / resets
			bitset.clear();
			prev = gs;
			game++;
		}
		
	}

	

	// -------- STEPPING ON TRAPS EXTRACTOR (BEGIN) ------- //
	
	@Test
	public void testSteppingOnTraps() {
		// data from first game in "games" relation -- copied from testMovementFeatures1
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState startState = new GameState(white,black);
	    
	    
	    //test bit updating (first, even though it is logically after tests below)
	    updateBitsTestManager();
	    
	    
	    //test simple move (with no replacements)
	    long moveBitBoard1 = (1L << startState.getIndex(2, 1)) //b3 -- zero indexed
				  | (1L << startState.getIndex(3, 0)) //a4
				  | (1L << startState.getIndex(2, 7)); //h3
	    
	    testMoveCorrectness("Db2n Ha2n Ha3n Hh2n", moveBitBoard1, startState);
	    
	    
	    //test replacement of piece by another piece type
	    startState.playPASS(startState); //make it black's turn
	    long moveBitBoard2 = (1L << startState.getIndex(4, 4)) //e5 (5-1, e-1)
				  | (1L << startState.getIndex(6, 4)) //e7
				  | (1L << startState.getIndex(5, 7)); //h6
	    
	    testMoveCorrectness("ee7s ee6s re8s rh7s", moveBitBoard2, startState);
	    
	    	    
	    //test replace of piece by another of same piece type
	    String whiteReplaceTest = "1w Ee1 Ee2";
	    String blackReplaceTest = "1b ee8 ee7";
	    startState = new GameState(whiteReplaceTest, blackReplaceTest);
	    
//	    long moveBitBoardReplaceTest = (1L << startState.getIndex(2, 4)) //e3
//	    							| (1L << startState.getIndex(1, 4)); //e2
	    //TODO: detect replacement, uncomment this. (is this reasonable to do?)		
	    //testMoveCorrectness("Ee2n Ee1n", moveBitBoardReplaceTest, startState);
	}
	
	private void updateBitsTestManager() {
		/* e.g. if type3 steps unsafely on the lower left trap, then the bit set is:
	   	 *  <br> - ->      0 * 32 + 0 * 8 + 3 = 3 [where the second 0 is the number of the lower left trap]*/
		
		String[] whiteStrings = { 
				"1w Dc2 Rb3", //ensure dog isn't eaten by trap
				"1w Dc2 Rb3 Rd3", //ensure dog isn't eaten by trap, and is safe
				"1w Df2 Ee3" //try elephant safety at a different trap
		};
		
		String[] blackStrings = {
		   /* Note that black has a safe trap, but this shouldn't matter since it's white to play */
				"1b hd7 ef7 mg7", // note 3 stronger pieces (horse, elephant, camel) --> white dog type 3
				"1b hd7 ef7 mg7", // note 3 stronger pieces (horse, elephant, camel)
				"1b hd7 ef7 mg7" // note 3 stronger pieces (horse, elephant, camel)
		};
		
		String[] whiteMoves = {
				"Dc2n",
				"Dc2n",
				"Df2n"
		};
		
		int[][] bitIndicesArray = {
			{ FeatureRange.STEPPING_TRAP_START + 3 },
			{ FeatureRange.STEPPING_TRAP_START + 3 + 32 },
			{ FeatureRange.STEPPING_TRAP_START + 3 + 32 + 2 * 8 } //trap #2
		};
		
		boolean[][] trapSafetyArray = {
			{ false, false, false, false }, //no one is safe :O
			{ true, false, false, false }, //lower left safe
			{ false, false, true, false } //lower right safe
		};
		
		assertTrue(whiteStrings.length == blackStrings.length 
				&& bitIndicesArray.length == trapSafetyArray.length
				&& whiteStrings.length == trapSafetyArray.length
				&& whiteMoves.length == whiteStrings.length); //ensure all arrays same length;
		
		testBitUpdateCorrectnessForAllGames(whiteMoves, whiteStrings, blackStrings, bitIndicesArray, trapSafetyArray);
	}
	
	private void testBitUpdateCorrectnessForAllGames(String[] whiteMoves, String[] whiteStarts, 
			String[] blackStarts, int[][] bitIndicesArray, boolean[][] trapSafetyArray) {
	
		int numGames = whiteStarts.length;
		
		for (int game = 0; game < numGames; game++) {
			GameState startState = new GameState(whiteStarts[game], blackStarts[game]);
			testBitUpdateCorrectness(whiteMoves[game], startState, bitIndicesArray[game], trapSafetyArray[game]);
		}
	}

	private void testBitUpdateCorrectness(String moveString, GameState start, int[] bitIndices, 
											boolean[] trapSafety) {
		BitSet bitset = new BitSet(FeatureRange.STEPPING_TRAP_END + 1);
		SteppingOnTrapsExtractor sOTE = getNewSOTE(moveString, start);
	    sOTE.updateBitSet(bitset);
	    
	    for (int trap = 0; trap < trapSafety.length; trap++) 
	    	assertTrue(trapSafety[trap] == sOTE.getTrapSafety(trap));
	    
	    for (int i = 0; i < bitIndices.length; i++)
	    	assertTrue(bitset.get(bitIndices[i]));
	}
	
	private void testMoveCorrectness(String moveString, long moveBitBoard, GameState start) {
		String NEEMA_ERROR_STRING = "** Error in testMoveCorrectness"
				+ " (perhaps a piece was replaced by another on the same turn) **"
				+ "\n[Please note this shouldn't matter for SteppingOnTrapsExtractor since the piece"
				+ " strength on the trap has not changed...]";
	    
	    SteppingOnTrapsExtractor sOTE = getNewSOTE(moveString, start);
	    
	    long getMovedPieces = sOTE.getMovedPieces();
	    if (moveBitBoard != getMovedPieces) { //to print out discrepancies before assertion
	    	System.out.println(NEEMA_ERROR_STRING);
	    	System.out.println("Bit board should be: " + Long.toBinaryString(moveBitBoard) +
	    					 "\nBut actually was   : " + Long.toBinaryString(getMovedPieces));
	    	assertTrue(moveBitBoard == getMovedPieces);
	    }
	    
	}
	
	private SteppingOnTrapsExtractor getNewSOTE(String moveString, GameState start) {
		ArimaaMove move = new ArimaaMove(moveString);
		GameState next = new GameState();
	    next.copy(start); //play move from start...
	    next.playFullClear(move, start);
	    
	    byte[] pieceTypes = new byte[12];
	    calculatePieceTypes(next, pieceTypes); //modify pieceTypes in place
	    
	    return new SteppingOnTrapsExtractor(start, next, pieceTypes);
	}
	
	/* Helper method copy-pasted (and then modified to fix compiler issues only) from FeatureExtractor
	 * for use in SteppingOnTrapsExtractor testing */
	private static void calculatePieceTypes(GameState curr, byte[] piece_types){
		assert(piece_types.length == 12);
		
		for (int i = 0; i < 2; i++){ // calculate for rabbits 
			byte numStronger = FeatureExtractor.countOneBits(curr.stronger_enemy_bb[i]);
			if (numStronger < 5)
				piece_types[i] = 7;
			else if (numStronger < 7)
				piece_types[i] = 6;
			else
				piece_types[i] = 5;
		}
		
		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
			byte numStronger = FeatureExtractor.countOneBits(curr.stronger_enemy_bb[i]);
			switch (numStronger) {
				case 0: piece_types[i] = 0; break;
				case 1: piece_types[i] = 1; break;
				case 2: piece_types[i] = 2; break;
				case 3: case 4: piece_types[i] = 3; break;
				default: piece_types[i] = 4; break;
			}
		}
	}
	
	// -------- STEPPING ON TRAPS EXTRACTOR (END) ------- //

	
	
	
	@Test
	public void testFreezing1() {
		GameState prev = new GameState(tests[3]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("Ra5n"));
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("Ra5n"), prev);
//		System.out.println(prev.toBoardString());
//		System.out.println(curr.toBoardString());
		assertTrue(featureVector.get(1567));
		assertTrue(featureVector.get(1625));
	}

	@Test
	public void testFreezing2() {
		GameState prev = new GameState(tests[4]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("ra5n"));
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("ra5n"), prev);
//		System.out.println(prev.toBoardString());
//		System.out.println(curr.toBoardString());
		assertTrue(featureVector.get(1583));
		assertTrue(featureVector.get(1689));
	}
	
	@Test
	public void testFreezing3() {
		GameState prev = new GameState(tests[5]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("ra6n ra7n ra4n"));
//		System.out.println(prev.toBoardString());
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("ra6n ra7n ra4n"), prev);
//		System.out.println(curr.toBoardString());
//		System.out.println(featureVector);
		assertTrue(featureVector.get(1582));
		assertTrue(featureVector.get(1688));
	}
	
	@Test
	public void testFreezing4a() {
		GameState prev = new GameState(tests[6]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("ra6n ra7n ca5n"));
//		System.out.println(prev.toBoardString());
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("ra6n ra7n ca5n"), prev);
//		System.out.println(curr.toBoardString());
//		System.out.println(featureVector);
		assertTrue(featureVector.get(1571));
		assertTrue(featureVector.get(1689));
	}
	
	@Test
	public void testFreezing4b() {
		GameState prev = new GameState(tests[6]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("ra6n ca5n"));
//		System.out.println(prev.toBoardString());
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("ra6n ca5n"), prev);
//		System.out.println(curr.toBoardString());
//		System.out.println(featureVector);
		assertTrue(featureVector.nextSetBit(FeatureRange.FREEZING_START) > FeatureRange.FREEZING_END
				|| featureVector.nextSetBit(FeatureRange.FREEZING_START) == -1);
	}
	
	@Test
	public void testFreezing5() {
		GameState prev = new GameState(tests[5]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("ra6n ra7n ra4n ra5n"));
//		System.out.println(prev.toBoardString());
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("ra6n ra7n ra4n ra5n"), prev);
//		System.out.println(curr.toBoardString());
//		System.out.println(featureVector);
		assertTrue(featureVector.nextSetBit(FeatureRange.FREEZING_START) > FeatureRange.FREEZING_END
				|| featureVector.nextSetBit(FeatureRange.FREEZING_START) == -1);
	}
	
	@Test
	public void testFreezing6() {
		GameState prev = new GameState(tests[7]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("ma6n ma7e"));
//		System.out.println(prev.toBoardString());
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("ma6n ma7e"), prev);
//		System.out.println(curr.toBoardString());
//		System.out.println(featureVector);
		assertTrue(featureVector.get(1570));
		assertTrue(featureVector.get(1571));
		assertTrue(featureVector.get(1688));
		assertTrue(featureVector.get(1699));
	}
	
	@Test
	public void testFreezing7() {
		GameState prev = new GameState(tests[8]);
		prev.compute_tertiary_bitboards();
		FeatureExtractor fe = new FeatureExtractor(prev, null, null, null, null);
		BitSet featureVector = fe.extractFeatures(new ArimaaMove("ra6n ra7n ca4n ca5n"));
//		System.out.println(prev.toBoardString());
		GameState curr = new GameState();
		curr.playFull(new ArimaaMove("ra6n ra7n ca4n ca5n"), prev);
//		System.out.println(curr.toBoardString());
//		System.out.println(featureVector);
		assertTrue(featureVector.get(1571));
		assertTrue(featureVector.get(1688));
		assertTrue(featureVector.get(1582));
		assertTrue(featureVector.get(1689));
	}
	
	/** Tests different portions of CaptureThreats one at a time...*/
	@Test
	public void testCaptureThreats() {
		testHelperMethodsInCaptureThreats();
		testThreatenCapture();
	}
	
	private void testHelperMethodsInCaptureThreats() {
		//test nearestTrap
		GameState helperGS = new GameState();
		assertTrue(CaptureThreatsExtractor.nearestTrap(helperGS.getIndex(0, 0)) == 0); //LL
		assertTrue(CaptureThreatsExtractor.nearestTrap(helperGS.getIndex(3, 3)) == 0); //LL
		assertTrue(CaptureThreatsExtractor.nearestTrap(helperGS.getIndex(4, 3)) == 1); //UL
		assertTrue(CaptureThreatsExtractor.nearestTrap(helperGS.getIndex(3, 4)) == 2); //LR
		assertTrue(CaptureThreatsExtractor.nearestTrap(helperGS.getIndex(4, 4)) == 3); //UR
		assertTrue(CaptureThreatsExtractor.nearestTrap(helperGS.getIndex(5, 7)) == 3); //UR
	}
	
	//testing THREATEN CAP
	private void testThreatenCapture() {
		//set up valid start game -- no captures possible
		String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
		String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
		GameState curr = new GameState(white, black);
		CaptureThreatsExtractor cTE = new CaptureThreatsExtractor(null, curr);
		
		BitSet bs = new BitSet(NUM_FEATURES);
		cTE.updateBitSet(bs);
		assertTrue(bs.cardinality() == 0);
		
		// capture possible -- assume black has just moved to "create a threat", white to play
		String whiteCap = "1w Me2"; //can be pulled/pushed into trap at f3 by ee3 (trap #2) -- 2 ways -- both are the same?
		String blackCap = "1b md3 ee3"; //opponent's threatened piece (M) has 1 piece stronger (namely e)
		curr = new GameState(whiteCap, blackCap);
		cTE = new CaptureThreatsExtractor(null, curr);
		
		bs.clear();
		cTE.updateBitSet(bs);
		assertTrue(bs.cardinality() == 1);
		assertTrue(bs.get(CaptureThreats.THREATENS_CAP_OFFSET + 32 * 2 + 8 * 3 + 1)); // requires 4 moves -- step takes value 3
		
		// capture possible -- capturing two pieces in multiple ways (TODO: change trap calc)
		String whiteCap2 = "1w Me3 Dd6";
		String blackCap2 = "1b ee2 md5"; // each piece can kill/trap the corresponding white one in 2 ways each
										 // and they can both kill in one turn (but doesn't matter)
		curr = new GameState(whiteCap2, blackCap2);
		cTE = new CaptureThreatsExtractor(null, curr);
		
		bs.clear();
		cTE.updateBitSet(bs);
		assertTrue(bs.cardinality() == 4);
		
		// killing the camel in trap 2 -- camel should be type 1.
		assertTrue(bs.get(CaptureThreats.THREATENS_CAP_OFFSET + 32 * 2 + 8 * 3 + 1)); //roundabout kill
		assertTrue(bs.get(CaptureThreats.THREATENS_CAP_OFFSET + 32 * 2 + 8 * 1 + 1)); //direct
		
		// killing the dog in trap 1 -- dog should be type 2.
		assertTrue(bs.get(CaptureThreats.THREATENS_CAP_OFFSET + 32 * 1 + 8 * 3 + 2)); //roundabout kill
		assertTrue(bs.get(CaptureThreats.THREATENS_CAP_OFFSET + 32 * 1 + 8 * 1 + 2)); //direct
		
		//TODO: testing once trap-approximation is fixed (if ever) (currently assumes closest trap
		//		gives the kill)
	}
	
	@Test
	public void testPrevMoves1() {
		GameState prevPrevSource = new GameState(tests[2]);
		ArimaaMove prevPrevMove = new ArimaaMove("Rh1n Rh2n Rh3n Rh4w");
//		System.out.println(prevPrevSource.toBoardString());
		GameState prevSource = new GameState();
		prevSource.playFull(prevPrevMove, prevPrevSource);
		ArimaaMove prevMove = new ArimaaMove("rg7s eb7w pass");
//		System.out.println(prevSource.toBoardString());
		GameState currSource = new GameState();
		currSource.playFull(prevMove, prevSource);
//		System.out.println(currSource.toBoardString());
		ArimaaMove currMove = new ArimaaMove("Rd1n Rd2w Cd7s");
		PreviousMovesExtractor pme = new PreviousMovesExtractor(prevPrevSource, prevPrevMove, prevSource, prevMove, currSource, currMove);
		assertArrayEquals(new byte[] {7, 6, 14, 22}, pme.prevPrevStepSources);
		assertArrayEquals(new byte[] {52, 54, 53, 49}, pme.prevStepSources);
		assertArrayEquals(new byte[] {3, 2, 51, -1}, pme.currStepSources);
		BitSet fv = new BitSet();
		pme.updateBitSet(fv);
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START + 3 + 1 + 2 + 2));
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START + FeatureConstants.NUM_CLOSENESS_SCORES + 1));
	}
	
	@Test
	public void testPrevMoves2() {
		GameState prevPrevSource = new GameState(tests[2]);
		ArimaaMove prevPrevMove = new ArimaaMove("Rh1n Rh2n Rh3n Rc5n Rc6x");
//		System.out.println(prevPrevSource.toBoardString());
		GameState prevSource = new GameState();
		prevSource.playFull(prevPrevMove, prevPrevSource);
		ArimaaMove prevMove = new ArimaaMove("db5s eb7w pass");
//		System.out.println(prevSource.toBoardString());
		GameState currSource = new GameState();
		currSource.playFull(prevMove, prevSource);
//		System.out.println(currSource.toBoardString());
		ArimaaMove currMove = new ArimaaMove("Rh4n Cd7s Rd1n");
		PreviousMovesExtractor pme = new PreviousMovesExtractor(prevPrevSource, prevPrevMove, prevSource, prevMove, currSource, currMove);
		assertArrayEquals(new byte[] {7, 15, 23, 34}, pme.prevPrevStepSources);
		assertArrayEquals(new byte[] {52, 33, 53, 49}, pme.prevStepSources);
		assertArrayEquals(new byte[] {3, 31, 51, -1}, pme.currStepSources);
		BitSet fv = new BitSet();
		pme.updateBitSet(fv);
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START + 3 + 2 + 2));
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START + FeatureConstants.NUM_CLOSENESS_SCORES + 1 + 2 + 3 + 1));
	}
	
	@Test
	public void testPrevMoves3() {
		// data from first game in "games" relation
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState prevSource = new GameState(white,black);
		ArimaaMove prevMove = new ArimaaMove("Ee2n Md2n Hh2n pass");
		GameState currSource = new GameState();
		currSource.playFull(prevMove, prevSource);
		ArimaaMove currMove = new ArimaaMove("ra7s hb7s pass");
//		System.out.println(prevSource.toBoardString());
//		System.out.println(currSource.toBoardString());
		
		PreviousMovesExtractor pme = new PreviousMovesExtractor(null, null, null, null, prevSource, prevMove);
		BitSet fv = new BitSet();
		pme.updateBitSet(fv);
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START));
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START + FeatureConstants.NUM_CLOSENESS_SCORES));
		
		fv = new BitSet();
		pme = new PreviousMovesExtractor(null, null, prevSource, prevMove, currSource, currMove);
		pme.updateBitSet(fv);
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START));
		assertTrue(fv.get(FeatureRange.PREV_MOVES_START + FeatureConstants.NUM_CLOSENESS_SCORES));
	}
	
	@Test
	public void testGoalThreats() {
		//Set-up board for use
		String board = "12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13";
		GameState gsEasy = new GameState(board);
		
		String whitePull1 = "1w Dd1 Dc1";
		String blackPull1 = "1b rd2 ee1"; //elephant can pull dog d1 out of the way
		GameState gsPull1 = new GameState(whitePull1, blackPull1);
		gsPull1.playPASS(gsPull1); //testing black's ability to win
		
		String white = "1w Eb8 Rc6";
		String black = "1b ea6 ea7 ee6 ee7 mc8 ed8"; //elephants preventing rabbit from moving
		//this check could be more useful if winInNumSteps was strict ==, rather than <=
		GameState gsRestricted = new GameState(white, black);
		
		String whitePull2 = "1w Dd1 Dc1";
		String blackPull2 = "1b rd2 me2"; //camel can un-freeze rabbit
		GameState gsPull2 = new GameState(whitePull2, blackPull2);
		gsPull2.playPASS(gsPull2); //testing black's ability to win
		
		String whiteWinning = "1w Re8";
		String blackWinning = "1b re1";
		GameState gsWon = new GameState(whiteWinning, blackWinning);
		
		
		//run actual tests!
		GameState[] gsArr = { gsEasy, gsPull1, gsRestricted, gsPull2, gsWon };
		testGoalTestWrapper(gsArr);
		testGoalThreatsExtractor(gsArr);
	}
	
	private void testGoalTestWrapper(GameState[] gsArr) {		
		GameState gsEasy = gsArr[0];
		GameState gsPull1 = gsArr[1];
		GameState gsRestricted = gsArr[2];
		GameState gsPull2 = gsArr[3];
		GameState gsWon = gsArr[4];
		
		GoalTestWrapper gtw = new GoalTestWrapper();
		assertTrue(gtw.canWin(gsEasy));
		assertTrue(!gtw.winInNumSteps(gsEasy, 0));
		assertTrue(!gtw.winInNumSteps(gsEasy, 1));
		assertTrue(gtw.winInNumSteps(gsEasy, 2));
		assertTrue(gtw.winInNumSteps(gsEasy, 3));
		assertTrue(gtw.winInNumSteps(gsEasy, 4));
		
		GameState gsEasyPass =  new GameState();
		gsEasyPass.playPASS(gsEasy);
		assertTrue(gtw.canWin(gsEasyPass));
		assertTrue(!gtw.winInNumSteps(gsEasyPass, 1));
		assertTrue(gtw.winInNumSteps(gsEasyPass, 2));
		
				
		//test pulling to allow win		
		assertTrue(gtw.canWin(gsPull1));
		assertTrue(!gtw.winInNumSteps(gsPull1, 0));
		assertTrue(!gtw.winInNumSteps(gsPull1, 1));
		assertTrue(!gtw.winInNumSteps(gsPull1, 2));
		assertTrue(gtw.winInNumSteps(gsPull1, 3));
		assertTrue(gtw.winInNumSteps(gsPull1, 4));
		

		//test restricted pulling to allow win.
		assertTrue(!gtw.winInNumSteps(gsRestricted, 0));
		assertTrue(!gtw.winInNumSteps(gsRestricted, 1));
		assertTrue(!gtw.winInNumSteps(gsRestricted, 2));
		assertTrue(!gtw.winInNumSteps(gsRestricted, 3));
		assertTrue(gtw.winInNumSteps(gsRestricted, 4));
		GameState gsRestrictedPass = new GameState();
		gsRestrictedPass.playPASS(gsRestricted);
		assertTrue(!gtw.canWin(gsRestrictedPass));

		
		//test moving and pulling to allow win
		assertTrue(gtw.canWin(gsPull2));
		assertTrue(!gtw.winInNumSteps(gsPull2, 3));
		assertTrue(gtw.winInNumSteps(gsPull2, 4));
		
		
		//test 0 steps 
		assertTrue(gtw.winInNumSteps(gsWon, 0)); //white wins
		GameState gsWonPass = new GameState();
		gsWonPass.playPASS(gsWon);
		assertTrue(gtw.winInNumSteps(gsWonPass, 0)); //black wins
		
		
		//can't win any of the other games in 0 (after whatever passing--may be redundant)
		assertTrue(!gtw.winInNumSteps(gsEasy, 0));
		assertTrue(!gtw.winInNumSteps(gsRestricted, 0));
		assertTrue(!gtw.winInNumSteps(gsPull1, 0));
		assertTrue(!gtw.winInNumSteps(gsPull2, 0));
	}
	
	private void testGoalThreatsExtractor(GameState[] gsArr) {
		//Swap turns for each one, to simulate the fact that moves were *played* to generate the threat
		for (int gs = 0; gs < gsArr.length; gs++)
			gsArr[gs].playPASS(gsArr[gs]);
		
		GameState gsEasy = gsArr[0];
		GameState gsPull1 = gsArr[1];
		GameState gsRestricted = gsArr[2];
		GameState gsPull2 = gsArr[3];
		GameState gsWon = gsArr[4];
		
		GoalThreatsExtractor gteEasy = new GoalThreatsExtractor(gsEasy);
		GoalThreatsExtractor gtePull1 = new GoalThreatsExtractor(gsPull1);
		GoalThreatsExtractor gteRestricted = new GoalThreatsExtractor(gsRestricted);
		GoalThreatsExtractor gtePull2 = new GoalThreatsExtractor(gsPull2);
		GoalThreatsExtractor gteWon = new GoalThreatsExtractor(gsWon);
		
		int offset = FeatureRange.GOAL_THREATS_START;
		BitSet bs = new BitSet(NUM_FEATURES);
		
		gteEasy.updateBitSet(bs);
		assertTrue(bs.cardinality() == 2);
		assertTrue(bs.get(offset + 2));
		assertTrue(bs.get(offset + 5)); //allows win
		bs.clear();
		
		gtePull1.updateBitSet(bs);
		assertTrue(bs.cardinality() == 1);
		assertTrue(bs.get(offset + 3));
		bs.clear();
		
		gteRestricted.updateBitSet(bs);
		assertTrue(bs.cardinality() == 1);
		assertTrue(bs.get(offset + 4));
		bs.clear();
		
		gtePull2.updateBitSet(bs);
		assertTrue(bs.cardinality() == 1);
		assertTrue(bs.get(offset + 4));
		bs.clear();
		
		gteWon.updateBitSet(bs);
		assertTrue(bs.cardinality() == 2);
		assertTrue(bs.get(offset));
		assertTrue(bs.get(offset + 5)); //allows win
		bs.clear();
	}
	
}
