package feature_extractor;

import java.util.BitSet;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

import feature_extractor.FeatureConstants.FeatureRange;

public class FreezingExtractor extends AbstractExtractor {

	private static final int START_INDEX = FeatureRange.FREEZING_START;
	private static final int END_INDEX = FeatureRange.FREEZING_END;
	
	/** Constant to denote status has changed to frozen */
	private static final int FROZEN = 1;
	
	/** Constant to denote status has changed to being unfrozen */
	private static final int UNFROZEN = 0;
	
	private GameState curr;
	private GameState prev;
	private BitSet featureVector;
	private byte[] piece_types;
	
	public FreezingExtractor(GameState prev, GameState curr, byte[] piece_types) {

		this.curr = curr;
		this.prev = prev;
		featureVector = null;
		this.piece_types = piece_types;
	}
	
	@Override
	public void updateBitSet(BitSet featureVector) {
		this.featureVector = featureVector;
		long currentlyFrozen = 0L;
		long previouslyFrozen = 0L;
		
		for(int i = 0; i <= PT_BLACK_CAMEL; i++) {
			int player = i & 0x01;
			currentlyFrozen = curr.piece_bb[i] & curr.frozen_pieces_bb;
			previouslyFrozen = prev.piece_bb[i] & prev.frozen_pieces_bb;
			if((currentlyFrozen ^ previouslyFrozen) > 0) //if frozen statuses for this piece id have changed
				setAllFreezeLocationsAndTypes(player, piece_types[i], currentlyFrozen, previouslyFrozen);
		}
	}
	
	private void setAllFreezeLocationsAndTypes(int player, byte piece_type, long currentlyFrozen, long previouslyFrozen) {
		
		boolean toFrozen = false;
		boolean toUnfrozen = false;
		
		for(int i = 0; i < Long.SIZE; i++) {
			long currBit = currentlyFrozen & (1L << i);
			long prevBit = previouslyFrozen & (1L << i);
			if(currBit != prevBit) {
				if(currBit > 0) {
					setFreezeLocation(player, FeatureExtractor.getLocation(i), FROZEN);
					toFrozen = true;
				}
				else {
					setFreezeLocation(player, FeatureExtractor.getLocation(i), UNFROZEN);
					toUnfrozen = true;
				}
			}
		}
		
		if(toFrozen)
			setFreezeType(player, piece_type, FROZEN);
		if(toUnfrozen)
			setFreezeType(player, piece_type, UNFROZEN);
	}

	private void setFreezeType(int player, byte piece_type, int finalFrozenStatus) {
		int index = 0;//START_INDEX + player*NUM_PIECE_TYPES*NUM_LOCATIONS + piece_type*NUM_LOCATIONS + location;
		featureVector.set(index);
	}
	
	private void setFreezeLocation(int player, int location, int finalFrozenStatus) {
		int index = 0;// START_INDEX + NUM_SRC_MVMT + player*NUM_PIECE_TYPES*(NUM_LOCATIONS+1) + piece_type*(NUM_LOCATIONS+1) + location;
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
