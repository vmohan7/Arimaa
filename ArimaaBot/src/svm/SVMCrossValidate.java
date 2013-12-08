package svm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import utilities.AbstractHypothesis;
import utilities.DisconnectedGameData;
import utilities.HypothesisTest;

public class SVMCrossValidate extends SVMMain {

	public static final int NUM_GAMES = 0;
	public static final int GAMEIDS = 1;
	public static final int DATA_FILE_1 = 2;
	
	public static void main(String[] args) {
		if (args.length < 3){
			printErrorMessage();
		} else{
			File gameFile = new File( args[GAMEIDS] );
			int num_games = Integer.parseInt(args[NUM_GAMES]); 
			String[] modelFiles = new String[args.length - DATA_FILE_1];
			for(int i = DATA_FILE_1; i < args.length; i++)
				modelFiles[i - DATA_FILE_1] = args[i];
			printTestData(modelFiles, num_games, gameFile);
		}
	}
	
	private static void printErrorMessage(){
		System.out.println("Usage: <num_games> <game_ids_file> <data_file_1> <data_file_2> ...");
		System.out.println("If optional parameter included in testing, then will use libsvm instead of liblinear");
		System.exit(1);
	}
	
	protected static void printTestData(String[] modelFiles, int num_games, File gameIds) {
		
		AbstractHypothesis myHypothesis;
		if(isSvm)
			myHypothesis = evaluateLibSvm(modelFile, num_games, gameIds);
		else
			myHypothesis = evaluateLibLinear(new File(modelFile), num_games, gameIds );
		
		
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
