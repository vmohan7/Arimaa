package feature_extractor;

import java.util.BitSet;
import arimaa3.GameState;

public class TrapExtractor extends AbstractExtractor {

	private GameState prev, curr;
	
	public TrapExtractor(GameState prev, GameState curr) {
		this.prev = prev;
		this.curr = curr;
	}
	
	@Override
	public void updateBitSet(BitSet bitset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int startOfRange() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int endOfRange() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
