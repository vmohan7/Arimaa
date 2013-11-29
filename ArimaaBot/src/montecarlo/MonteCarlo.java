package montecarlo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import utilities.helper_classes.ArimaaState;

import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.FirstMove;
import arimaa3.GameState;
import arimaa3.MoveList;

public class MonteCarlo {

	/**
	 * @param args - The first string should be the location of the output. The second string is the number of trails
	 */
	public static void main(String[] args) {
		if (args.length != 2){
			System.out.println("Error in usage - 2 string: location of output followed by number of trails");
			System.exit(1);
		}
		int numGames = Integer.parseInt(args[1]);

		double[] weights = new double[Utilities.TOTAL_FEATURES];
		train(numGames, weights);

		outputWeights(weights, new File(args[0]) );
	}
	
	public static void outputWeights(double[] weights, File output) {
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(output.getAbsoluteFile()) );
			
			for(int i = 0; i < weights.length; i++){
				writer.write(weights[i] + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		MoveList possibleMoves = engine.genRootMoves(state.getCurr());
		
		//white needs to make the first move to initialize the state for TD
		ArimaaMove move = agents[0].selectMove(state, possibleMoves);
		state = new ArimaaState(gameBoard, move); 
		
		int moveCount = 1;
		gameBoard.playFullClear(state.getNextMove(), state.getCurr());
		while( !gameBoard.isGameOver() ) {
			System.out.println(state.getCurr());
			
			possibleMoves = engine.genRootMoves(gameBoard);
			if (possibleMoves.size() == 0) break; //Game Over does not seem to capture this
			
			ArimaaState nextState = new ArimaaState(state.getCurr(), gameBoard, null);
			move = agents[player].selectMove(nextState, possibleMoves);
			nextState = new ArimaaState(state.getCurr(), gameBoard, move);
			
			Utilities.TDUpdate(state, nextState, 0, ETA, weights);
			player = (player + 1)%agents.length;
			state = nextState;
			
			//Should be the same pointer, but let's be explicit about the update
			for (ReflexAgent agent: agents) 
				agent.setWeights(weights);

			moveCount++;
			
			gameBoard.playFullClear(state.getNextMove(), state.getCurr());
		}

		System.out.println(state.getCurr());
		Utilities.TDUpdate(state, null, moveCount % 2 == 0 ? 1 : 0 , ETA, weights);
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
	
}
