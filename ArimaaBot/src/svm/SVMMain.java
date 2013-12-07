package svm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import naive_bayes.NBMain;

import libsvm.svm;
import libsvm.svm_model;

import de.bwaldvogel.liblinear.Model;

import utilities.AbstractHypothesis;
import utilities.DisconnectedGameData;
import utilities.HypothesisTest;
import utilities.helper_classes.Utilities;


public class SVMMain {

	public static final int MODE = 0;
	public static final int DATA_FILE = 1;
	public static final int NUM_GAMES = 2;
	public static final int GAMEIDS = 3;
	public static final int LIBSVM = 4;
	
	public static void main(String[] args) {
		if (args.length < 4){
			printErrorMessage();
		} else{
			Utilities.PARSEABLE_OUTPUT = true; //Change to input later
			
			File gameFile = new File( args[GAMEIDS] );
			int num_games = Integer.parseInt(args[NUM_GAMES]); 
			
			if( args[MODE].equals("--train") ){
				getTrainData(new File( args[DATA_FILE] ), num_games, gameFile);
			} else if ( args[MODE].equals("--test")  ){
				boolean issvm = args.length >= 5 && Integer.parseInt(args[LIBSVM]) == 1; 
				printTestData(issvm, args[DATA_FILE], num_games, gameFile);
			} else {
				printErrorMessage();
			}
		}
	}
	
	private static void printErrorMessage(){
		System.out.println("Usage: --<train/test> <data_file> <num_games> <game_ids_file> <optional: 1>");
		System.out.println("If optional parameter included in testing, then will use libsvm instead of liblinear");
		System.exit(1);
	}
	
	private static void getTrainData(File dataFile, int num_games, File gameIds) {
		DisconnectedGameData myGameData = new DisconnectedGameData(num_games, null, false);
		
		SVMTrain trainingModel = new SVMTrain(dataFile);
		Utilities.printInfo("Created the SVM model");
		
		trainingModel.train(myGameData);
		Utilities.printInfo("Just finished creating the training file!");
		
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
		myGameData.close();
	}

	private static void printTestData(boolean isSvm, String modelFile, int num_games, File gameIds) {
		
		AbstractHypothesis myHypothesis;
		if(isSvm)
			myHypothesis = evaluateLibSvm(modelFile, num_games, gameIds);
		else
			myHypothesis = evaluateLibLinear(new File(modelFile), num_games, gameIds );
		
		
		ArrayList<Integer> gIds = new ArrayList<Integer>();
		try {
			Scanner scan = new Scanner(gameIds);
			while(scan.hasNext()){ gIds.add(scan.nextInt()); }
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		DisconnectedGameData myGameData = new DisconnectedGameData(num_games, gIds, false);
		Utilities.printInfo("\nTesting hypothesis on " + num_games +" TEST games...");
		HypothesisTest.test(myHypothesis, myGameData);
		
		myGameData = new DisconnectedGameData(num_games, gIds, true);
		Utilities.printInfo("\nTesting hypothesis on TRAINING games...");
		HypothesisTest.test(myHypothesis, myGameData);
		myGameData.close();
	}
	
	private static AbstractHypothesis evaluateLibSvm(String modelFile, int num_games, File gameIds){
		svm_model model = null;
		try {
			model = svm.svm_load_model(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Utilities.printInfo("Finished loading SVM model...");
		
		return new SVMHypothesis( model );
	}

	
	private static AbstractHypothesis evaluateLibLinear(File modelFile, int num_games, File gameIds){
		Model model = null;
		try {
			model = Model.load( modelFile );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Utilities.printInfo("Finished loading SVM (class#=" + model.getNrClass() + ")...");
		
		return new SVMLinearHypothesis( model );
	}

}
