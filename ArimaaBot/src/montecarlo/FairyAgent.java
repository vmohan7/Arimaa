package montecarlo;

import utilities.helper_classes.ArimaaState;
import arimaa3.MoveList;

public class FairyAgent extends AlphaBetaSearchAgent {

	
	/**
	 * @param depth for AlphaBeta Search
	 */
	public FairyAgent(int depth) {
		super(null, false, depth);
	}

	@Override
	protected double getGameOverScore() {
		//TODO put the actual game over score
		return 0;
	}

	@Override
	protected double evaluation(ArimaaState state) {
		// TODO fill in evaluation function for Fairy given the Game State
		return 0;
	}

	@Override
	protected MoveList getMoves(ArimaaState state) {
		return engine.genRootMoves(state.getCurr());
	}

}
