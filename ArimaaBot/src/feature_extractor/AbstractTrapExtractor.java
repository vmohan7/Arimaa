package feature_extractor;

import arimaa3.GameState;


/** The abstract superclass of all extractors involving traps in some way
 *  <p> Extends AbstractExtractor */
public abstract class AbstractTrapExtractor extends AbstractExtractor {
	
	/* The two methods below are static since they are called by the test harness */
	/** Returns a byte containing one of the TrapStatus.NUM_STATUSES possible
	 * trap statuses (number of pieces on trap, elephant?, etc).
	 * @param state The current board
	 * @param trapNum Lower left, upper left, lower right, upper right
	 * @param playerNum PL_WHITE, PL_BLACK */
	protected static byte getTrapStatus(GameState state, int trapNum, int playerNum) {
		long player_trap_bb = TOUCH_TRAP[trapNum] & state.colour_bb[playerNum]; // all white or black pieces touching trap #trapNum
		byte numPieces = FeatureExtractor.countOneBits(player_trap_bb);
		assert(numPieces <= 4); //shouldn't be more than 4 pieces touching a trap...
		
		boolean elephant = isElephantTouchingTrap(state, trapNum, playerNum);
		return TrapStatus.convertToStatus(numPieces, elephant);
	}
	
	/** Returns whether an elephant for the given player (in the current game state)
	 * is on any of the four spaces adjacent to the given trap. */
	protected static boolean isElephantTouchingTrap(GameState state, int trapNum, int playerNum) { //remove static once done testing, make private
		int boardNum = playerNum == PL_WHITE ? PT_WHITE_ELEPHANT : PT_BLACK_ELEPHANT;
		long elephant_bb = state.piece_bb[boardNum];
	
		return (elephant_bb & TOUCH_TRAP[trapNum]) != 0;
	}
	
	
}
