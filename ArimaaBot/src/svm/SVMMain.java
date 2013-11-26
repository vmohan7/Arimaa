package svm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import de.bwaldvogel.liblinear.Model;

import utilities.DisconnectedGameData;
import utilities.HypothesisTest;


public class SVMMain {

	public static final int MODE = 0;
	public static final int DATA_FILE = 1;
	public static final int NUM_GAMES = 2;
	public static final int GAMEIDS = 3;
	
	public static void main(String[] args) {
		if (args.length != 4){
			printErrorMessage();
		} else{
			File dataFile = new File( args[DATA_FILE] );
			File gameFile = new File( args[GAMEIDS] );
			int num_games = Integer.parseInt(args[NUM_GAMES]); 
			
			if( args[MODE].equals("--train") ){
				getTrainData(dataFile, num_games, gameFile);
			} else if ( args[MODE].equals("--test")  ){
				printTestData(dataFile, num_games, gameFile);
			} else {
				printErrorMessage();
			}
		}
	}
	
	private static void printErrorMessage(){
		System.out.println("Usage: --train/test data_file num_games game_ids_file");
		System.exit(1);
	}
	
	private static void getTrainData(File dataFile, int num_games, File gameIds) {
		DisconnectedGameData myGameData = new DisconnectedGameData(num_games, null, false);
		
		SVMTrain trainingModel = new SVMTrain(dataFile);
		System.out.println("Created the SVM model");
		
		trainingModel.train(myGameData);
		System.out.println("Just finished creating the training file!");
		
		//Prints out game ids to file
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(gameIds.getAbsoluteFile()) );
			ArrayList<Integer> ids = myGameData.getCurrentGameId();
			for(Integer id : ids){
				writer.write(id + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void printTestData(File modelFile, int num_games, File gameIds) {

		Model model = null;
		try {
			model = Model.load(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished loading SVM (class#=" + model.getNrClass() + ")...");
		
		SVMHypothesis myHypothesis = new SVMHypothesis( model );
		ArrayList<Integer> gIds = new ArrayList<Integer>();
		try {
			Scanner scan = new Scanner(gameIds);
			while(scan.hasNext()){ gIds.add(scan.nextInt()); }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		DisconnectedGameData myGameData = new DisconnectedGameData(num_games, gIds, false);
		System.out.println("\nTesting hypothesis on " + num_games +" TEST games...");
		HypothesisTest.test(myHypothesis, myGameData);
		
		myGameData = new DisconnectedGameData(num_games, gIds, true);
		System.out.println("\nTesting hypothesis on TRAINING games...");
		HypothesisTest.test(myHypothesis, myGameData);
	}

}
