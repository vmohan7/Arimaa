package feature_extractor;

import java.util.BitSet;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

import feature_extractor.FeatureConstants.FeatureRange;

public class PreviousMovesExtractor extends AbstractExtractor {
	
	private ArimaaMove currMove;
	private ArimaaMove prevMove;
	private ArimaaMove prevPrevMove;
	private BitSet featureVector;
	
	public PreviousMovesExtractor(ArimaaMove prevPrevMove, ArimaaMove prevMove, ArimaaMove currMove) {
		
		this.currMove = currMove;
		this.prevMove = prevMove;
		this.prevPrevMove = prevPrevMove;
		featureVector = null;
	}

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
