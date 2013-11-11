package feature_extractor;
import java.util.BitSet;
import arimaa3.*;

public class FeatureExtractor implements Constants, FeatureConstants {
	
	private static final int NUM_FEATURES = FeatureRange.TRAP_STATUS_END + 1; //TODO update this as you add more features
	
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
	
}