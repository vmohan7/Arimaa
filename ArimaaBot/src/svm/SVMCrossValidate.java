package svm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import utilities.AbstractHypothesis;
import utilities.DisconnectedGameData;
import utilities.HypothesisTest;
import utilities.helper_classes.Utilities;

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
		System.err.println("Usage: <num_games> <game_ids_file> <data_file_1> <data_file_2> ...");
		System.err.println("If optional parameter included in testing, then will use libsvm instead of liblinear");
		System.exit(1);
	}
	
	protected static void printTestData(String[] modelFiles, int num_games, File gameIds) {
		
		AbstractHypothesis[] myHypotheses = new AbstractHypothesis[modelFiles.length];
		
		for(int i = 0; i < modelFiles.length; i++)
			myHypotheses[i] = evaluateLibLinear(new File(modelFiles[i]), num_games, gameIds );
		
		ArrayList<Integer> gIds = new ArrayList<Integer>();
		try {
			Scanner scan = new Scanner(gameIds);
			while(scan.hasNext()){ gIds.add(scan.nextInt()); }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		DisconnectedGameData myGameData = new DisconnectedGameData(num_games, gIds, false);
		Utilities.printInfo("\nTesting " + modelFiles.length + " hypotheses on " + num_games +" TEST games...");
		
		for(int i = 0; i < myHypotheses.length; i++) {
			Utilities.printInfo("Testing model from " + modelFiles[i]);
			HypothesisTest.test(myHypotheses[i], myGameData);
			if(!myGameData.reset())
				throw new RuntimeException("No rows in the testing set!");
		}
	}
}
