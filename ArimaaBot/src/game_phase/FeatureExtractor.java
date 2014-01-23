package game_phase;
import java.util.BitSet;

import utilities.helper_classes.Utilities;
import arimaa3.*;

public class FeatureExtractor implements FeatureConstants {
	
	/* Starting game state from which we play current_move */
	private GameState prev;
	private GameState prev_prev;
	private GameState prev_prev_prev;
	private ArimaaMove prevMove;
	private ArimaaMove prevPrevMove;

		
	/**
	 * 
	 * @param prev
	 * @param prev_prev
	 * @param prev_prev_prev
	 * @param prevMove move (by expert) to get to prev game state
	 * @param prevPrevMove move (by expert) to get to prev_prev game state
	 */
	public FeatureExtractor(GameState prev, GameState prev_prev, GameState prev_prev_prev, ArimaaMove prevMove, ArimaaMove prevPrevMove) {
		this.prev = prev;
		this.prev_prev = prev_prev;
		this.prev_prev_prev = prev_prev_prev;
		this.prevMove = prevMove;
		this.prevPrevMove = prevPrevMove;
	}

	/*
	 * Extracts features for the resulting board after playing a possible legal move.
	 * current_board is the resulting board after playing current_move on prev game state.
	 */
	public BitSet extractFeatures(ArimaaMove current_move) {
		// Generate the current game state by applying move on the previous game state
		GameState currState = new GameState();
		currState.playFullClear(current_move, prev);
		return extractFeatures(current_move, currState);
	}
	
	/**
	 * 
	 * @param current_move
	 * @param curr The game state that results from playing current_move to prev
	 * @return
	 */
	public BitSet extractFeatures(ArimaaMove current_move, GameState curr) {
		BitSet featureVector = new BitSet(NUM_FEATURES);
		
		/* Piece id to type mapping for the current game state (curr). E.g. if piece_types[3] = 4, this means
		 * that piece id 3 (black cat) is of piece type 4 for the current GameState. This changes every time 
		 * extractFeatures() is called. */
		byte[] piece_types = Utilities.calculatePieceTypes(curr); //Neema made this local to merge methods across this and CaptureThreatsExtractor

		// feature extraction subroutine calls here
		
		(new PositionMovementExtractor(prev, curr, current_move, piece_types)).updateBitSet(featureVector);
		(new TrapExtractor(prev, curr)).updateBitSet(featureVector);
		(new FreezingExtractor(prev, curr, piece_types)).updateBitSet(featureVector);
		(new SteppingOnTrapsExtractor(prev, curr, piece_types)).updateBitSet(featureVector);
		//(new CaptureThreatsExtractor(prev, curr)).updateBitSet(featureVector);
		//(new PreviousMovesExtractor(prev_prev_prev, prevPrevMove, prev_prev, prevMove, prev, current_move)).updateBitSet(featureVector);
		//(new GoalThreatsExtractor(curr)).updateBitSet(featureVector);
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
	
	public static byte countOneBits(long n) {
		// Algorithm adapted from http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetKernighan
		byte c; // c accumulates the total bits set in v
		for (c = 0; n != 0; c++)
			n &= n - 1; // clear the least significant bit set
		return c;
	}
	
}