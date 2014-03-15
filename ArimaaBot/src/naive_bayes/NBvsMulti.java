package naive_bayes;

import java.util.BitSet;
import java.util.PriorityQueue;

import montecarlo.MoveOrderingPruning.MoveOrder;

import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.GenTurn;
import feature_extractor.FeatureExtractor;
import utilities.GameData;
import utilities.GameParser;
import utilities.MoveArrayList;
import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

/**
 * Class to compare move orderings of some real game states.
 * @author arzavj
 *
 */
public class NBvsMulti {
	
	/* Fractions of GameData games used for training the model. */
	private static final double TRAIN_FRACTION = 0.5;
	
	/** Rating of the games in the GameData. */
	private static final int RATING = 2100;

	private static final int NUM_GAMES = 2;
	private static final int NUM_VISIBLE_MOVE_ORDERINGS = 1;
	private static final int SKIP_INTERVAL = 25;
	
	public static void main(String[] args) {
		
		GameData.setRatingThreshold(RATING);
		GameData myGameData = new GameData(NUM_GAMES, TRAIN_FRACTION);
		Utilities.printInfo("Finished fetching game data");
		
		NBTrain trainingModel = new NBTrain();
		Utilities.printInfo("Created the NB model");
		
		NBHypothesis nbHypothesis = trainNB(trainingModel, myGameData);
		
		myGameData.setMode(GameData.Mode.TRAIN);
		
		MultiNBTrain multiNBTrainingModel = new MultiNBTrain();
		Utilities.printInfo("Created the MultiNB model");
		MultiNBHypothesis multiNBHypothesis = trainMultiNB(multiNBTrainingModel, myGameData);
		
		test(multiNBHypothesis, nbHypothesis, myGameData);
		
		myGameData.close();
	}
	
	private static void test(MultiNBHypothesis multiNBHypothesis, NBHypothesis nbHypothesis, GameData myGameData) {
		
		GenTurn gen_turn = new GenTurn();
		 
		for (int count = 0; count < NUM_VISIBLE_MOVE_ORDERINGS && myGameData.hasNextGame(); count++) {
			GameInfo gi = myGameData.getNextGame();
			GameParser gp = new GameParser(gi);
			
			while (gp.hasNextGameState()) {
				ArimaaState state = null;
				
				// skip games
				for (int i = 0; i < SKIP_INTERVAL; i++) {
					if (!gp.hasNextGameState()) break; 
					state = gp.getNextGameState();
				}
				
				if (state == null) break;
				
				GameState curr = state.getCurr();
				System.out.println(curr.toBoardString());
				FeatureExtractor fe = new feature_extractor.FeatureExtractor( curr, state.getPrev(), state.getPrevPrev(),
						state.getPrevMove(), state.getPrevPrevMove() );
			    
				MoveArrayList moves = new MoveArrayList(10000);
				gen_turn.genAllTurns(curr, moves);
				
				PriorityQueue<MoveOrder> nbQueue = new PriorityQueue<MoveOrder>( moves.size(), new MoveOrder(false) );
				PriorityQueue<MoveOrder> multiNBQueue = new PriorityQueue<MoveOrder>( moves.size(), new MoveOrder(false) );
				
				for (ArimaaMove m : moves) {
					BitSet features = fe.extractFeatures(m);
					nbQueue.add(new MoveOrder(m, nbHypothesis.evaluate(features, curr)));
					multiNBQueue.add(new MoveOrder(m, multiNBHypothesis.evaluate(features, curr)));
				}
				
				System.out.println("NB Ordering...");
				printAllMovesUpToExpert(nbQueue, state.getNextMove());
				
				System.out.println("MultiNB Ordering...");
				printAllMovesUpToExpert(multiNBQueue, state.getNextMove());
				
				System.out.println(String.format("%n%n%n%n ******* NEXT ORDERING ******* %n%n%n"));
			}
		}

	}

	private static void printAllMovesUpToExpert(PriorityQueue<MoveOrder> queue, ArimaaMove expertMove) {
		boolean flag = true;
		System.out.println(String.format("--------------------------------------%n"));
		
		for (int pos = 1; flag; pos++) {
			MoveOrder move = queue.poll();
			assert(move != null);
			if (move.move == expertMove) flag = false;
			
			System.out.println(String.format("Move %5d: %s", pos, move.move.toString()));
		}
		
		System.out.println(String.format("--------------------------------------%n%n"));
	}

	private static MultiNBHypothesis trainMultiNB(MultiNBTrain multiNBTrainingModel, GameData myGameData) {
		Utilities.printInfo("Train fraction: " + TRAIN_FRACTION);
		Utilities.printInfo("Game data rating threshold <" +
							(GameData.USING_EXPERT ? "using" : "not using") +
							">: " + RATING);
		Utilities.printInfo("Training on " + (int)(TRAIN_FRACTION * NUM_GAMES) + " games...");
		
		NBHypothesis[] nbParameters = multiNBTrainingModel.train(myGameData);
		Utilities.printInfo("Just finished training!");
		
		Utilities.printInfo("Creating a hypothesis...");
		MultiNBHypothesis myHypothesis = new MultiNBHypothesis(nbParameters, (int)(NUM_GAMES * TRAIN_FRACTION)); 

		return myHypothesis;
	}

	private static NBHypothesis trainNB(NBTrain trainingModel, GameData myGameData) {
		Utilities.printInfo("Train fraction: " + TRAIN_FRACTION);
		Utilities.printInfo("Game data rating threshold <" +
							(GameData.USING_EXPERT ? "using" : "not using") +
							">: " + RATING);
		Utilities.printInfo("Training on " + (int)(TRAIN_FRACTION * NUM_GAMES) + " games...");		

		long[][] frequencyTable = trainingModel.train(myGameData);
		Utilities.printInfo("Just finished training!");
		
		Utilities.printInfo("Creating a hypothesis...");
		NBHypothesis myHypothesis = new NBHypothesis( frequencyTable, 
				trainingModel.getNumNonExpertMoves(), trainingModel.getNumExpertMoves() );
		
		return myHypothesis;
	}
}
