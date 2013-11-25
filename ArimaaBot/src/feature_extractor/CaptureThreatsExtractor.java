package feature_extractor;

import java.util.BitSet;
import arimaa3.GameState;

public class CaptureThreatsExtractor extends AbstractExtractor {

	private GameState prev, curr;
	
	public CaptureThreatsExtractor(GameState prev, GameState curr) {
		this.prev = prev;
		this.curr = curr;
	}
	
	
	/** The portion of the BitSet that will be updated is CAPTURE_THREATS_END - CAPTURE_THREATS_START
	 *  + 1 (== 704) bits. These bits will be split up as follows:
	 *  <br> -a
	 *  <br> -b 
	 *  <br> -c
	 *  <br> --anything to say?
	 *  */
	@Override
	public void updateBitSet(BitSet bitset) {
		int startOfRange = startOfRange();
		int numFeatures = endOfRange() - startOfRange + 1;
		
		//do stuff
	}

	@Override
	public int startOfRange() {
		return FeatureRange.CAPTURE_THREATS_START;
	}

	
	@Override
	public int endOfRange() {
		return FeatureRange.CAPTURE_THREATS_END;
	}
	
}