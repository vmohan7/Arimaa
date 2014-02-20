package montecarlo;

import utilities.helper_classes.ArimaaState;
import arimaa3.GameState;
import arimaa3.MoveList;

public class FairyAgent extends AlphaBetaSearchAgent {
	
	private static final int GAME_OVER_SCORE = 500000;
	
	/**
	 * @param depth for AlphaBeta Search
	 */
	public FairyAgent(int depth) {
		super(null, false, depth);
	}

	@Override
	protected double getGameOverScore(GameState gs) {
		// subtract total steps to slightly favor shorter wins
		return GAME_OVER_SCORE - gs.total_steps;
	}

	@Override
	protected double evaluation(ArimaaState state) {
		return FairyEvaluation.evaluate(state.getCurr());
	}

	@Override
	protected MoveList getMoves(ArimaaState state) {
		return engine.genRootMoves(state.getCurr());
	}

}
