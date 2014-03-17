package fairy_agents;

import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.Utilities;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.MoveList;
import arimaa3.ThreeFoldRepetition;

public class FairyDeepMoveOrderingAgent extends FairyMoveOrderingClusteringAgent {

	//These many moves will be used as a sample
	public static final int MAX_MOVES = 20;
	
	public FairyDeepMoveOrderingAgent(int depth) {
		super(depth);
	}
	
	/** Prints human-readable formatted output about the settings of the agent. */
	@Override
	public void printSettingsStats() {
		Utilities.printInfo(String.format("-----%nRunning stats for FairyDeepMoveOrderingAgent only:"));
		Utilities.printInfo("Moves considered: " + MAX_MOVES);
		Utilities.printInfo("Depth: " + searchDepth);
		Utilities.printInfo("XMeans trained on: " + getNumXMeansGames() + " games");
		Utilities.printInfo("MultiNB trained on: " + getNumTrainedGames() + " games");
		Utilities.printInfo(String.format("-----%n"));
	}
	
	@Override
	protected int getPrunedSize(int numMoves){
		return Math.min(numMoves, MAX_MOVES);
	}
	
	@Override
	public ArimaaMove selectMove(final ArimaaState arimaaState, String move_history){
		//we want to examine all moves at the root node
		MoveList moves = genRootMovesArrayList(arimaaState.getCurr());
		ArimaaMove bestMove = null;
		
		double maxAlpha = Double.NEGATIVE_INFINITY;
		long[] dup_positions = ThreeFoldRepetition.setup(move_history);
		
		for(ArimaaMove move : moves){
			GameState gs = new GameState();
			gs.playFullClear(move, arimaaState.getCurr());
			
			if ( isRepetitionBanned(gs.getPositionHash(), dup_positions) )
				continue;
			
			//will call get moves that will do pruning in alhabeta
			double a = AlphaBeta(new ArimaaState(arimaaState.getPrevPrev(), arimaaState.getPrev(), arimaaState.getCurr(), 
							arimaaState.getPrevPrevMove(), arimaaState.getPrevMove(), move) , maxDepth - 1, maxAlpha, Double.POSITIVE_INFINITY, false);
			if (maxAlpha < a){
				maxAlpha = a;
				bestMove = move;
			}
		}
		
		return bestMove;
	}

}
