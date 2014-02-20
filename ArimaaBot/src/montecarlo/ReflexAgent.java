package montecarlo;

import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaMove;
import arimaa3.MoveList;

/**
 * Searches for the best move one ply deep.
 * It uses the evaluation function learned from MCS.
 * @author vasanth
 *
 */
public class ReflexAgent extends AbstractAgent {

	public ReflexAgent(double[] weights, boolean training) {
		super(weights, training);
	}

	@Override
	public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves) {
		ArimaaMove bestMove = trainRandomly( moves );
		if (bestMove != null)
			return bestMove;
		
		double score = Double.NEGATIVE_INFINITY; //we want to maximize this value
		
		for (ArimaaMove possibleMove : moves){
			double evaluation = Utilities.logLinearEvaluation(arimaaState, possibleMove, weights);
			if (evaluation > score){
				score = evaluation;
				bestMove = possibleMove;
			}
		}
		
		return bestMove;
	}

}
