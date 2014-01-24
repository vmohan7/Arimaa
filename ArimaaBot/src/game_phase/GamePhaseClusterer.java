package game_phase;

import java.util.ArrayList;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

public class GamePhaseClusterer {

	public static void main(String args[]){
		GameData myGameData = new GameData(1000, 1.0);
		Utilities.printInfo("Finished fetching game data");
		ArrayList<double[]> trainMatrix = new ArrayList<double[]>();
		
		int count = 0;
		while (myGameData.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			Utilities.printInfoInline("Training on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = myGameData.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState()){
				trainMatrix.add( FeatureExtractor.extractFeatures(myParser.getNextGameState().getCurr()) );
			}
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("training took " + Utilities.msToString(endTime - startTime));
		}
		
		double[][] matrix = trainMatrix.toArray(new double[1][1]);
		KMeansWrapper kmeans = new KMeansWrapper(3, 1000, matrix);
		kmeans.cluster();
		double[][] centers = kmeans.centroids();
		
		for(int i = 0; i < centers.length; i++){
			for(int j = 0; j < centers[i].length; j++){
				//Print out centroids
			}
		}
	}

}
