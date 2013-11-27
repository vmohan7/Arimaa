package feature_extractor;

import java.util.BitSet;

import feature_extractor.FeatureConstants.FeatureRange;

public class PreviousMovesExtractor extends AbstractExtractor {
	
	@Override
	public void updateBitSet(BitSet featureVector) {
		

	}

	@Override
	public int startOfRange() {
		return FeatureRange.PREV_MOVES_START;
	}

	@Override
	public int endOfRange() {
		return FeatureRange.PREV_MOVES_END;
	}

}
