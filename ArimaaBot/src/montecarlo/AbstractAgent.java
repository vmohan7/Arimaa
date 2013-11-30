package montecarlo;

import java.util.Random;

import arimaa3.ArimaaMove;
import arimaa3.MoveList;
import utilities.helper_classes.ArimaaState;

public abstract class AbstractAgent {
	
	protected boolean training;
	protected double[] weights;
	
	public static final double RANDOM_CHOICE = 0.10;
	
	public AbstractAgent(double[] weights, boolean training){
		setWeights(weights);
		this.training = training; 
	}
	
	/**
	 * Returns the best move for the model. Should not be passed an empty set
	 * @param arimaaState - We assume that the nextMove field is null and use that to test out moves
	 * @return The best move that the computer can think of.
	 */
	public abstract ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves);
	
	/**
	 * 
	 * @param w - Sets the weights that are used for the evaluation functions
	 */
	public void setWeights(double[] w){
		weights = w;
	}
	
	protected ArimaaMove trainRandomly(MoveList moves){
		if (training){
			Random r = new Random();
			if( r.nextDouble() < RANDOM_CHOICE ){ 
				int choice = r.nextInt( moves.size() );
				System.out.println("PICKING RANDOM MOVE");
				return moves.move_list[ choice ];
			}
		}
		
		return null;
	}
}
