package feature_extractor;

import java.util.BitSet;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

public class SteppingOnTrapsExtractor extends AbstractExtractor {

	/** 0 = lower left, 1 = upper left, 2 = lower right, 3 = upper right */
	private boolean[] areTrapsSafe;
	
	/** The bit-boards of moved pieces (1s at locations where new pieces showed up)<br> 
	 * <i>[This description is awkward to emphasize that movedPieces does not sense (as currently
	 * implemented) replacements on a given square by a piece of the same type]</i>*/
	private long[] movedPieces;
	
	/* Excerpt from Wu on Stepping On Traps
	 * ------------------------------------
	 * Voluntarily stepping on a trap square can be dangerous when the trap has too few defenders.
	 * However, it can also be good to place a piece on a trap square because it allows that piece in
	 * the future to access any of the four surrounding squares for defending that trap, and a piece on
	 * a trap is hard to push away.
	 * 
	 * We heuristically define a "safe" trap for a player as one that is guarded either by two defenders
	 * of that player, or the elephant. Otherwise, the trap is "unsafe."
	 * 
	 * 		UNSAFE STEP ON TRAP(trap,type): Piece of type type (0-7) stepped on unsafe trap
	 * 			trap (0-3). (32 features)
	 * 		SAFE STEP ON TRAP(trap,type): Piece of type type (0-7) stepped on safe trap
	 * 			trap (0-3). (32 features)
	 */
	
	
	public long getMovedPieces(int pieceType) { //for testing
		return movedPieces[pieceType / 2];
	}


	public SteppingOnTrapsExtractor(GameState prev, GameState curr) {
		// player who played the move
		int player = prev.player;
		
		areTrapsSafe = new boolean[TRAP.length];
		for (int trap = 0; trap < areTrapsSafe.length; trap++) {
			// using curr, since safety should be judged at end of move (agreed?)
			areTrapsSafe[trap] = getTrapStatus(curr, trap, player) >= TrapStatus.ONE_E; 
			//TODO: merge with TrapExtractor's method?
		}
		
		ArimaaMove move = new ArimaaMove();
		move.difference(prev, curr);
		
		
		final long[] preMoveBBs = prev.piece_bb;
		final long[] postMoveBBs = curr.piece_bb;
		
		movedPieces = new long[6]; //12 / 2 -- num of player's boards
		
		for (int i = player; i < 12; i += 2) {
			/* Find the difference the move caused (~pieceBBBeforeMove has 1s exactly where
		     * there weren't pieces before)--this is per piece*/
			long pieceBBBeforeMove = preMoveBBs[i];
		    long pieceBBAfterMove = postMoveBBs[i];
		    
		    //NOTE: this does not capture whether a given piece type replaces that same type on the trap
			movedPieces[i/2] = pieceBBAfterMove & ~pieceBBBeforeMove;
		}
		
	}
	
	
	/** The portion of the BitSet that will be updated is STEPPING_TRAP_END - STEPPING_TRAP_START
	 *  + 1 (== 64) bits. These bits will be split up as follows:
	 *  <br> -a
	 *  <br> -b 
	 *  <br> -c
	 *  <br> -anything else?
	 *  */
	@Override
	public void updateBitSet(BitSet bitset) {
		int startOfRange = startOfRange();
		int numFeatures = endOfRange() - startOfRange + 1;
	}

	@Override
	public int startOfRange() {
		return FeatureRange.STEPPING_TRAP_START;
	}

	
	@Override
	public int endOfRange() {
		return FeatureRange.STEPPING_TRAP_END;
	}
	
	/** Returns a byte containing one of the TrapStatus.NUM_STATUSES possible
	 * trap statuses (number of pieces on trap, elephant?, etc).
	 * @param state The current board
	 * @param trapNum Lower left, upper left, lower right, upper right
	 * @param playerNum PL_WHITE, PL_BLACK */
	private byte getTrapStatus(GameState state, int trapNum, int playerNum) {
		long player_trap_bb = TOUCH_TRAP[trapNum] & state.colour_bb[playerNum]; // all white or black pieces touching trap #trapNum
		byte numPieces = FeatureExtractor.countOneBits(player_trap_bb);
		assert(numPieces <= 4); //shouldn't be more than 4 pieces touching a trap...
		
		boolean elephant = isElephantTouchingTrap(state, trapNum, playerNum);
		return TrapStatus.convertToStatus(numPieces, elephant);
	}
	
	/** Returns whether an elephant for the given player (in the current game state)
	 * is on any of the four spaces adjacent to the given trap. */
	private boolean isElephantTouchingTrap(GameState state, int trapNum, int playerNum) { //remove static once done testing, make private
		int boardNum = playerNum == PL_WHITE ? PT_WHITE_ELEPHANT : PT_BLACK_ELEPHANT;
		long elephant_bb = state.piece_bb[boardNum];
	
		return (elephant_bb & TOUCH_TRAP[trapNum]) != 0;
	}
	
	
	/** Returns a bit-board with 1s in positions where player <b>player</b> has a piece. */
	private long getPlayerPieceBB(GameState gs, int player) {
		assert(player == PL_WHITE || player == PL_BLACK);
		
		long bitboard = 0L;
		for (int i = player; i < 12; i+=2) //look only at bitboards for the player
			bitboard |= gs.piece_bb[i];
		
		return bitboard;
	}
}
