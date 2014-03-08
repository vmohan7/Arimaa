package fairy_agents;

import montecarlo.AbstractCombiner;
import montecarlo.AlphaBetaSearchAgent;
import game_phase.GamePhase;
import game_phase.GamePhaseHeuristicDiscriminator;
import utilities.helper_classes.ArimaaState;
import arimaa3.GameState;
import arimaa3.MoveList;

public class FairyAgent extends AlphaBetaSearchAgent {
	
	/** This is the original evaluation -- uses the default AbstractCombiner implementation. */
	protected class DefaultCombiner extends AbstractCombiner {

		public DefaultCombiner(GamePhase whichPhase) {
			super(whichPhase);
		}
		
	}
	
	
	private static final int GAME_OVER_SCORE = 500000;
	private DefaultCombiner dCombiner;
	
	/**
	 * @param depth for AlphaBeta Search
	 */
	public FairyAgent(int depth) {
		super(null, false, depth);
		dCombiner = new DefaultCombiner(GamePhase.AGNOSTIC);
	}

	@Override
	protected double getGameOverScore(GameState gs) {
		// subtract total steps to slightly favor shorter wins
		return GAME_OVER_SCORE - gs.total_steps;
	}


	@Override
	/**
	 * @return Evaluation score with respect to the current player
	 */
	protected double evaluation(ArimaaState state) {
		return FairyEvaluation.evaluate(state.getCurr(), dCombiner);
	}

	@Override
	protected MoveList getMoves(ArimaaState state) {
		return engine.genRootMoves(state.getCurr());
	}

}
