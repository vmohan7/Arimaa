package montecarlo;

import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.MoveList;
import arimaa3.ThreeFoldRepetition;

public abstract class AlphaBetaSearchAgent extends AbstractAgent{

	protected int maxDepth;

	public AlphaBetaSearchAgent(double[] weights, boolean training, int depth) {
		super(weights, training);
		maxDepth = depth;
	}


	/**
	 * Should be evaluated from the current player's (from the game state) perspective
	 * If the current player wins, then it returns a positive score
	 * Otherwise, it should return a negative score
	 * @return
	 */
	protected abstract double getGameOverScore(GameState gs);
	protected abstract double evaluation(ArimaaState state);

	/** Uses the ArrayList implementation of genRootMoves() to get all 
	 * moves from an ArimaaState.
	 */
	protected MoveList getMoves(ArimaaState state){
		return genRootMovesArrayList(state.getCurr()); 
	}
	
	//use ArimaaState such that the next move is filled in by this function
	//we assume that the next move is null
	@Override
	public ArimaaMove selectMove(final ArimaaState arimaaState, String move_history){
		MoveList moves = getMoves(arimaaState);
		ArimaaMove bestMove = trainRandomly( moves );
		if (bestMove != null)
			return bestMove;
		
		double maxAlpha = Double.NEGATIVE_INFINITY;
		long[] dup_positions = ThreeFoldRepetition.setup(move_history);
		
		for(ArimaaMove move : moves){
			GameState gs = new GameState();
			gs.playFullClear(move, arimaaState.getCurr());
			
			if ( isRepetitionBanned(gs.getPositionHash(), dup_positions) )
				continue;
			
			double a = AlphaBeta(new ArimaaState(arimaaState.getPrevPrev(), arimaaState.getPrev(), arimaaState.getCurr(), 
							arimaaState.getPrevPrevMove(), arimaaState.getPrevMove(), move) , maxDepth - 1, maxAlpha, Double.POSITIVE_INFINITY, false);
			if (maxAlpha < a){
				maxAlpha = a;
				bestMove = move;
			}
		}
		
		return bestMove;
	}
	
	//these variables are used strictly for the FasterAB branch and the time agent class
	private int numEvals = 0;
	private double timeToEval = 0.0;
	private int numNodes = 0;
	
	public void printAndResetLeafEvals(){
		System.out.println("The total number of nodes explored: " + numNodes);
		System.out.println("The number of leaf nodes explored: " + numEvals);
		System.out.println("The average time of the evaluation function: " + timeToEval/numEvals);
		numEvals = 0;
		timeToEval = 0.0;
		numNodes = 0;
	}

	
	private double AlphaBeta(final ArimaaState state, int depth, double alpha, double beta, boolean isMaxPlayer){
//		numNodes++;
		GameState next = new GameState();
		next.playFullClear(state.getNextMove(), state.getCurr());
		
		int sign = (maxDepth - depth) % 2 == 0 ? 1 : -1;
		
		ArimaaState nextState = new ArimaaState(state.getPrev(), state.getCurr(), next, 
				state.getPrevMove(), state.getNextMove(), null);
		
		if (next.isGameOver()){
			//flip the sign of next.gameResult to make it from the current player's perspective
			double score =  sign*(next.getGameResult() < 0 ? 1 : -1)*(this.getGameOverScore(next)); 
			return score;
		} else if (depth == 0){
//			numEvals++; //this line is strictly for testing
//			double start = (System.nanoTime() / 1E6);
			double score =  sign*evaluation(nextState);
//			timeToEval += (System.nanoTime() / 1E6) - start;
			return score; 
		}

		MoveList moves = getMoves(nextState);  //Changed getMoves for just a GameState and called it on next as oppsed to curr
		
		if (isMaxPlayer){
			for(ArimaaMove move: moves){
				alpha = Math.max(alpha, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
						state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, false) );
				if (beta <= alpha)
					break; // beta cut off
			}
			return alpha;
		} else {
			for(ArimaaMove move: moves){
				beta = Math.min(beta, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
						state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, true) );
				if (beta <= alpha)
					break; // alpha cut off
			}
			return beta;
		}
	}
	
	  private boolean isRepetitionBanned(long hash_code, long[] duplicate_positions) {

		    for (long banned_hash : duplicate_positions) {
		      if (banned_hash == hash_code) {
		        return true;
		      }
		    }

		    return false;
		  }
}
