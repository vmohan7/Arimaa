package fairy_agents;

import game_phase.GamePhase;
import game_phase.GamePhaseHeuristicDiscriminator;
import montecarlo.AbstractCombiner;
import montecarlo.AlphaBetaSearchAgent;
import utilities.helper_classes.ArimaaState;
import arimaa3.GameState;
import arimaa3.MoveList;

/** 
 * FairyAgent using heuristic game phase discrimination and hard-coded
 * combiner weights.
 */
public class FairyAgentHeuristicHardcoded extends AlphaBetaSearchAgent {

	/** Uses hard-coded coefficients to linearly combine values. */
	protected class HardcodedCombiner extends AbstractCombiner {
		
		/** Factor by which a value (e.g. rabbit value, trap value, 
		 * material value) is favored depending on the game phase.
		 */
		
		private static final double FAVOR_WEIGHT = 1.2;

		public HardcodedCombiner(GamePhase whichPhase) {
			super(whichPhase);
		}

		/**
		 * Combine scores using hard-coded weights. If the game is in the beginning,
		 * then favor trap value; if middle, then favor material value; if end, then
		 * favor rabbit value.
		 */		
		@Override
		public int combineScore(int materialValue, int trapValue, int rabbitValue) {
			switch (phase) {
				case BEGINNING:
					return (int) (materialValue + (FAVOR_WEIGHT * trapValue) + rabbitValue);
				case MIDDLE:
					return (int) ((FAVOR_WEIGHT * materialValue) + trapValue + rabbitValue);
				case END:
				default:
					return (int) (materialValue + trapValue + (FAVOR_WEIGHT * rabbitValue));
			}
		}
		
	}
	
	private static final int GAME_OVER_SCORE = 500000;
	private HardcodedCombiner hCombiner;
	
	/**
	 * @param depth for AlphaBeta Search
	 */
	public FairyAgentHeuristicHardcoded(int depth) {
		super(null, false, depth);
		hCombiner = new HardcodedCombiner(null); //expect the null to be overwritten before use	
	}

	@Override
	protected double getGameOverScore(GameState gs) {
		// subtract total steps to slightly favor shorter wins
		return GAME_OVER_SCORE - gs.total_steps;
	}

	@Override
	protected double evaluation(ArimaaState state) {
		GameState curr = state.getCurr();
		hCombiner.setGamePhase(GamePhaseHeuristicDiscriminator.getStrictGamePhase(curr));
		return FairyEvaluation.evaluate(curr, hCombiner);
	}

	@Override
	protected MoveList getMoves(ArimaaState state) {
		return engine.genRootMoves(state.getCurr());
	}

}
