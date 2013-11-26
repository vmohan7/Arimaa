package feature_extractor;

import java.util.BitSet;

import arimaa3.*;

public class PositionMovementExtractor extends AbstractExtractor implements FeatureConstants {

	private static final int START_INDEX = FeatureRange.POS_MVMT_START;
	private static final int END_INDEX = FeatureRange.POS_MVMT_END;
	private static final int NUM_SRC_MVMT = 512;
	private static final int DEFAULT_CAPTURE_LOCATION = 32;
	
	private GameState curr;
	private GameState prev;
	private ArimaaMove current_move;
	private BitSet featureVector;
	private byte[] piece_types;
	
	public PositionMovementExtractor(GameState prev, GameState curr, ArimaaMove current_move, byte[] piece_types) {

		this.curr = curr;
		this.prev = prev;
		this.current_move = current_move;
		featureVector = null;
		this.piece_types = piece_types;
	}
	
	@Override
	public void updateBitSet(BitSet featureVector) {
		this.featureVector = featureVector;
		long[] move_bb = current_move.piece_bb;
		for(int i = 0; i < 12; i++) {
			long source = prev.piece_bb[i] & move_bb[i]; // this long encodes all the source locations of pieces that moved this turn
			long dest = source ^ move_bb[i]; // this long encodes all destination locations of pieces that moved this turn
			int player = i & 0x01; // player is 0 (white) if piece_id = i is even
			updateBitSetMovementFeatures(source, dest, piece_types[i], player);
		}
	}

	private void updateBitSetMovementFeatures(long source, long dest, byte piece_type, int player) {
		// higher rows are at the most significant bits		
		// Loop across each of the 64 bits in source and dest, and set features on featureVector accordingly. 
		for(int i = 0; i < Long.SIZE; i++) {
			if((source & (1L << i)) > 0) { // a piece moved from location 'i'
				setSrcMovementFeature(player, piece_type, FeatureExtractor.getLocation(i));
			}
			if((dest & (1L << i)) > 0) { // a piece moved to location 'i'
				setDestMovementFeature(player, piece_type, FeatureExtractor.getLocation(i));
			}
		}
		if(FeatureExtractor.countOneBits(source) > FeatureExtractor.countOneBits(dest)) { // piece has been captured
			setDestMovementFeature(player, piece_type, DEFAULT_CAPTURE_LOCATION);
		}
	}
	
	private void setSrcMovementFeature(int player, int piece_type, int location) {
		int index = START_INDEX + player*NUM_PIECE_TYPES*NUM_LOCATIONS + piece_type*NUM_LOCATIONS + location;
		featureVector.set(index);
	}
	
	private void setDestMovementFeature(int player, int piece_type, int location) {
		int index = START_INDEX + NUM_SRC_MVMT + player*NUM_PIECE_TYPES*(NUM_LOCATIONS+1) + piece_type*(NUM_LOCATIONS+1) + location;
		featureVector.set(index);
	}
	
	@Override
	public int startOfRange() {
		return START_INDEX;
	}

	@Override
	public int endOfRange() {
		return END_INDEX;
	}
	
}
