package feature_extractor;

import java.util.BitSet;

import utilities.GoalTestWrapper;
import arimaa3.GameState;

public class GoalThreatsExtractor extends AbstractExtractor {	
	
	private GameState curr;
	
	/** @param curr is the board after the move has been played. */
	public GoalThreatsExtractor(GameState curr) {
		this.curr = curr;
	}
	
	/** The portion of the BitSet that will be updated is GOAL_THREATS_END - GOAL_THREATS_START
	 *  + 1 (== 6) bits. The first (5) bits will be dedicated to "threatening win in 0-4 steps."
	 *  The last (6th) bit determines whether we allowed our opponent to win. */
	@Override
	public void updateBitSet(BitSet featureVector) {
		GoalTestWrapper gtw = new GoalTestWrapper();
		
		//check if we allowed opponent to win
		boolean allowWin = gtw.canWin(curr);
		if (allowWin) featureVector.set(endOfRange()); //sets last bit
		
		//play a pass move for the opponent...
		GameState currOppPass = new GameState();
		currOppPass.playPASS(curr);
		
		//...to see if we can win in numSteps moves
		for (int numSteps = 0; numSteps <= NUM_STEPS_IN_MOVE; numSteps++) {
			if (gtw.winInNumSteps(currOppPass, numSteps)) { 
				featureVector.set(startOfRange() + numSteps);
				break; // don't try more steps (since winning in e.g. 1 means you can win in >= 1)
			}
		}
	}
	
	@Override
	public int startOfRange() {
		return FeatureRange.GOAL_THREATS_START;
	}

	@Override
	public int endOfRange() {
		return FeatureRange.GOAL_THREATS_END;
	}

}
