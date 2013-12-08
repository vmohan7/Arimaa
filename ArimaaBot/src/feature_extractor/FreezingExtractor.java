package feature_extractor;

import java.util.BitSet;
import arimaa3.GameState;

public class FreezingExtractor extends AbstractExtractor {

	private static final int START_INDEX = FeatureRange.FREEZING_START;
	private static final int END_INDEX = FeatureRange.FREEZING_END;
	private static final int NUM_FREEZE_TYPES = 32;
	private static final int NUM_FREEZE_STATUSES = 2;
	
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
	
	/**
	 * <b>Assumption:</b>
	 * If the same location is unfrozen and frozen in the same move and with different piece types,
	 * this is captured in the feature vector. But if this happens with the same piece types
	 * and same locations, it is not recorded by the feature vector (eg. a rabbit is unfrozen and
	 * another rabbit of the same player is frozen at that location). If the same piece type is
	 * unfrozen at one location and frozen at another, this is also captured by the vector.
	 * @param player 1 - black and 0 - white
	 * @param piece_type 0-7 according to Wu
	 * @param currentlyFrozen bit board of currently frozen pieces in curr
	 * @param previouslyFrozen bit board of previously frozen pieces in prev
	 */
	private void setAllFreezeLocationsAndTypes(int player, byte piece_type, long currentlyFrozen, long previouslyFrozen) {
		
		long differences = currentlyFrozen ^ previouslyFrozen;
		long toFrozen = differences & currentlyFrozen;
		long toUnfrozen = differences & previouslyFrozen;

		for(int i = 0; i < Long.SIZE; i++) {
			if((toFrozen & (1L << i)) > 0)
				setFreezeLocation(player, FeatureExtractor.getLocation(i), FROZEN);
			else if((toUnfrozen & (1L << i)) > 0)
				setFreezeLocation(player, FeatureExtractor.getLocation(i), UNFROZEN);
		}
		
		if(toFrozen > 0)
			setFreezeType(player, piece_type, FROZEN);
		if(toUnfrozen > 0)
			setFreezeType(player, piece_type, UNFROZEN);
	}

	private void setFreezeType(int player, byte piece_type, int finalFrozenStatus) {
		int index = START_INDEX + player*NUM_PIECE_TYPES*NUM_FREEZE_STATUSES + piece_type*NUM_FREEZE_STATUSES + finalFrozenStatus;
		featureVector.set(index);
	}
	
	private void setFreezeLocation(int player, int location, int finalFrozenStatus) {
		int index = START_INDEX + NUM_FREEZE_TYPES + player*NUM_LOCATIONS*NUM_FREEZE_STATUSES + location*NUM_FREEZE_STATUSES + finalFrozenStatus;
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
