package svm;

import java.io.File;
import java.io.IOException;

import de.bwaldvogel.liblinear.Model;

import utilities.GameData;
import utilities.HypothesisTest;


public class SVMMain {

	public static void main(String[] args) {
		if (args.length != 3){
			printErrorMessage();
		} else{
			File file = new File( args[1] );
			int num_games = Integer.parseInt(args[2]); 
			
			if( args[0].equals("--train") ){
				getTrainData(file, num_games);
			} else if ( args[0].equals("--test")  ){
				printTestData(file, num_games);
			} else {
				printErrorMessage();
			}
		}
	}
	
	private static void printErrorMessage(){
		System.out.println("Usage: --train/test file_name num_games");
		System.exit(1);
	}

	private static void printTestData(File modelFile, int num_games) {

		Model model = null;
		try {
			model = Model.load(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished loading SVM (class#=" + model.getNrClass() + ")...");
		
		SVMHypothesis myHypothesis = new SVMHypothesis( model );
		GameData myGameData = new GameData(num_games, 1.0); //testing does not worry about the training fraction
		myGameData.setMode(GameData.Mode.TEST);
		
		System.out.println("\nTesting hypothesis on " + num_games +" TEST games...");
		HypothesisTest.test(myHypothesis, myGameData);
		
	}

	private static void getTrainData(File file, int num_games) {
		GameData myGameData = new GameData(num_games+1, ( (double) num_games )/num_games+1); //+ 1 so that we get exactly num_games with train fraction
		
		SVMTrain trainingModel = new SVMTrain(file);
		System.out.println("Created the SVM model");
		
		trainingModel.train(myGameData);
		System.out.println("Just finished creating the training file!");
		
	}

}
