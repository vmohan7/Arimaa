package montecarlo;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Random;

import utilities.helper_classes.ArimaaState;

import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.FirstMove;
import arimaa3.GameState;

public class MonteCarlo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numGames = 5;
		if (args.length == 2)
			numGames = Integer.parseInt(args[0]);

		double[] weights = new double[Utilities.TOTAL_FEATURES];
		train(numGames, weights);

	}
	
	public static void outputWeights(double[] weights, File output){
		BufferedWriter writer = new BufferedWriter(  );
	}
	
	public static final double VARIANCE = .01; 
	public static final double ETA = .1; 
	
	public static void train(int numGames, double[] weights){
		Random rand = new Random();
		for (int i = 0; i < weights.length; i++){
			weights[i] = VARIANCE*rand.nextGaussian(); //normal belief about the world
		}
		
		for (int i = 0; i < numGames; i++){
			System.out.println("Running game # " + (i+1) );
			runTrial(weights);
		}
	}

	private static void runTrial(double[] weights) {
		GameState gameBoard = getRandomStart();
		// 0 is white, 1 is black
		ReflexAgent []agents  = { new ReflexAgent(weights, true), new ReflexAgent(weights, true) };
		int player = 1; // we start with black in the loop
		ArimaaState state = new ArimaaState(gameBoard, null);
		ArimaaEngine engine = new ArimaaEngine();
		
		//white needs to make the first move to initialize the state for TD
		ArimaaMove move = agents[0].selectMove(state, engine);
		state = new ArimaaState(gameBoard, move); 
		
		int moveCount = 1;
		while( !gameBoard.isGameOver() ) {
			System.out.println(gameBoard);
			
			move = agents[player].selectMove(state, engine);
			gameBoard = new GameState();
			gameBoard.playFullClear(state.getNextMove(), state.getCurr());
			ArimaaState nextState = new ArimaaState(state.getCurr(), gameBoard, move);
			
			Utilities.TDUpdate(state, nextState, 0, ETA, weights);
			player = (player + 1)%agents.length;
			state = nextState;
			
			//Should be the same pointer, but let's be explicit about the update
			for (ReflexAgent agent: agents) 
				agent.setWeights(weights);

			moveCount++;
		}
	    
		//training for white only
		//this should work because the feature vector is symmetric built
		int reward = Math.max( 1, finalReward(moveCount/2) );
		Utilities.TDUpdate(state, null, moveCount % 2 == 0 ? reward : -reward , ETA, weights);
	}
	
	private static GameState getRandomStart(){
		GameState board = new GameState();
		FirstMove first_move = new FirstMove();

	    String w_text = first_move.getFirstMove(board, System.currentTimeMillis());
	    board = new GameState(w_text, "");
	    board.playPASS(board);
	    String b_text = first_move.getFirstMove(board, System.currentTimeMillis());
	    
	    return new GameState(w_text, b_text);
	}

	public static final int MAX_REWARD = 1000;
	public static final double LAMBDA = - 1.0/(10000*Math.E);
	/**
	 * A function that gives a reward based on how many moves were taken to get to the end, the fewer the better
	 * @param moveCount
	 * @return
	 */
	private static int finalReward(int moveCount){
		return (int) (MAX_REWARD*Math.exp(-LAMBDA*moveCount)); //At MoveCount = 100, reward = 1
	}
	
}
