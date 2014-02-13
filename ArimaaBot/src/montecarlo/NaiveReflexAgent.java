package montecarlo;

import naive_bayes.NBHypothesis;
import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaMove;
import arimaa3.MoveList;

public class NaiveReflexAgent extends AbstractNAVVSearch {
	public NaiveReflexAgent(double[] weights, boolean training, NBHypothesis hyp) {
		super(weights, training, 0, hyp); //ignore the depth
	}

	@Override
	public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves) {
		ArimaaMove bestMove = trainRandomly( moves );
		if (bestMove != null)
			return bestMove;
		

		MoveList minMoves = this.getMoves(arimaaState);
		
		double score = Double.NEGATIVE_INFINITY; //we want to maximize this value
		
		for (ArimaaMove move : minMoves){
			double evaluation = Utilities.logLinearEvaluation(arimaaState, move, weights);
			if (evaluation > score){
				score = evaluation;
				bestMove = move;
			}
		}
		
		return bestMove;
	}

	//Dummy overrides to make compiler happy
	
	@Override
	protected double getGameOverScore() { return 0; }

	@Override
	protected double evaluation(ArimaaState state) { return 0; }
	

	
}
