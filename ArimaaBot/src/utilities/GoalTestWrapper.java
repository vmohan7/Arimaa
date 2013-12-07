package utilities;

import arimaa3.GameState;

public class GoalTestWrapper extends arimaa3.TestForGoal 
							implements feature_extractor.FeatureConstants {

	public GoalTestWrapper() {
		super();
	}
	
	/** Determines whether the player whose turn it is in curr can win immediately (on this turn). */
	public boolean canWin(GameState curr) {
		return winInNumSteps(curr, NUM_STEPS_IN_MOVE);
	}
	
	/** Determines whether the player whose turn it is in curr can win in 
	 * less than or equal to nSteps steps. */
	public boolean winInNumSteps(GameState curr, int nSteps) {
		assert(nSteps >= 0 && nSteps <= NUM_STEPS_IN_MOVE);
		
		//separately check nSteps == 0, since Bacher's code doesn't seem to handle...
		if (nSteps == 0) return rabbitsOnEndRank(curr);
		
		return test_goal_squares(curr, nSteps);
	}
	
	/** Lightweight version of curr.isGameOver()--only checks rabbits. Ensures that
	 * this allows testing even when opponents doesn't have any rabbits (in which case
	 * isGameOver() returns true). */
	private boolean rabbitsOnEndRank(GameState curr) {
		int rabbitPlayer = curr.player; //rabbit boards correspond to player (0 or 1)
		long rankMask = rabbitPlayer == PL_WHITE ? RANK_8 : RANK_1;  
		return (curr.piece_bb[rabbitPlayer] & rankMask) != 0;
	}
	
	/** This method was copied and modified from TestForGoal (written by Jeff Bacher).
	 * @param game the current game board
	 * @param nSteps the number of steps available to make it to the goal. */
	private boolean test_goal_squares(GameState game, int nSteps) {
		// check first if rabbit can run to the end unassisted!
		if (can_rabbit_run(game, nSteps)) return true;
		
		game.compute_tertiary_bitboards();
		int start_index = (game.player == 0) ? 56 : 0;
		
		for (int i = start_index; i < start_index + 8; i++)
			if (test_individual_goal_square(game, i, nSteps))
	    	  return true;
	    
	    return false;
	}
	
}
