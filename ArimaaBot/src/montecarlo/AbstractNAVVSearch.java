package montecarlo;

import java.util.Comparator;
import java.util.PriorityQueue;

import feature_extractor.FeatureExtractor;
import naive_bayes.NBHypothesis;
import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaMove;
import arimaa3.MoveList;

public abstract class AbstractNAVVSearch extends AbstractSearchAgent {

	private NBHypothesis hyp;
	public AbstractNAVVSearch(double[] weights, boolean training, int depth, NBHypothesis hyp) {
		super(weights, training, depth);
		this.hyp = hyp;
	}
	
	public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves){
		return super.selectMove( arimaaState, getMoves(arimaaState) ); //does the limiting for the first set as well
	}
	

	@Override
	protected MoveList getMoves(ArimaaState state) {
		FeatureExtractor fe = new FeatureExtractor(state.getCurr(), state.getPrev());
		MoveList moves = engine.genRootMoves(state.getCurr()); //TODO pass in as a parameter
		PriorityQueue<MoveOrder> minMoves = topMoves(fe, moves);
		MoveList bestMoves = new MoveList(minMoves.size());
		
		for(MoveOrder mo : minMoves){
			bestMoves.getMove().copy(mo.move);	
		}
			
		return bestMoves;
	}

	private PriorityQueue<MoveOrder> topMoves(FeatureExtractor fe, MoveList moves){
		//int topk =  (int) Math.ceil(moves.size() * 0.1);
		int topk =  (int) Math.ceil(moves.size() * 0.2);
		//int topk = 30;
		
		PriorityQueue<MoveOrder> minPQ = new PriorityQueue<MoveOrder>( topk + 1, new MoveOrder(true) );
		PriorityQueue<MoveOrder> maxPQ = new PriorityQueue<MoveOrder>( topk + 1, new MoveOrder(false) );
		
		int counter = 0;
		for (ArimaaMove move : moves){
			//in future if extracting features is time consuming we can merge with logLinear
			MoveOrder mo = new MoveOrder(move, hyp.evaluate(fe.extractFeatures(move)) ); 
			maxPQ.add( mo );
			minPQ.add( mo );
			counter++;
			
			if (counter >= topk){
				maxPQ.remove( minPQ.remove() ); //removes the lowest from the heap
			}
			
		}
		
		return maxPQ;
	}

	class MoveOrder implements Comparator<MoveOrder>{
		
		public ArimaaMove move;
		public double weight;
		
		private boolean isMin;
		
		public MoveOrder(boolean isMin){
			this.isMin = isMin;
		}
		
		public MoveOrder(ArimaaMove move, double w){
			this.move = move;
			weight = w;
		}
		

		@Override
		public int compare(MoveOrder move1, MoveOrder move2) {
			int w = 0;
			if (move1.weight < move2.weight)
				w = -1;
			else if (move1.weight > move2.weight)
				w = 1;
			
			
			if (isMin)
				return w;
			else 
				return -w; //reverses the ordering to get the max at the top
		}
		
	}
}
