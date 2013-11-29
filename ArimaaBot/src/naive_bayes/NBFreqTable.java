package naive_bayes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import utilities.DisconnectedGameData;
import utilities.helper_classes.Utilities;

public class NBFreqTable {
	
	private static void train(int numGames, String fileLoc){
		System.out.println("-------------------------------");
		System.out.println("------START OF ROUND (" + (numGames) + ")------");
		System.out.println("-------------------------------\n");
		final long startTime = System.currentTimeMillis();
		
		DisconnectedGameData myGameData = new DisconnectedGameData(numGames, null, false);
		System.out.println("Finished fetching game data");
		
		NBTrain trainingModel = new NBTrain();
		System.out.println("Created the NB model");

		long[][] frequencyTable = trainingModel.train(myGameData);
		System.out.println("Just finished training!");
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Round execution time: " + Utilities.msToString(endTime - startTime));
		System.out.println("\n------------------------");
		System.out.println("------END OF ROUND------");
		System.out.println("------------------------\n\n");
		
		outputToFile(fileLoc, frequencyTable, trainingModel.getNumNonExpertMoves(), trainingModel.getNumExpertMoves());
	}
	
	private static void outputToFile(String file, long[][] ft, long numNeg, long numPos){
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(new File(file).getAbsoluteFile()) );
			
			writer.write(numNeg + "\n" + numPos + "\n"); //first two y values
			
			for (int i = 0; i < ft.length; i++){
				for (int j = 0; j < ft[i].length; j++){ 
					writer.write(ft[i][j] + " "); //elements in row
				}
				writer.write("\n"); //each row
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// Instructions to redirect console output to file:
	// right click NBMain.java -> Run as -> Run Configurations... -> select "Common" tab
	// 						   -> check "File" under "Standard Input and Output"
	//                         -> enter destination file name :D
	
	public static void main(String[] args) {
		final long totalStartTime = System.currentTimeMillis();
		
		if (args.length != 2){
			System.out.println("Error in NBFreqTable: expects 2 arguments...\n"
					+ "Arguments: <num-examples> <location-to-write-frequency-table> \n"
					);
			return;
		}
		
		int numExamples = Integer.parseInt(args[0]);
		String fileLocation = args[1];

		train(numExamples, fileLocation);
		
		final long totalEndTime = System.currentTimeMillis();
		System.out.println("\n\nTotal execution time (from process running to termination): " 
					+ Utilities.msToString(totalEndTime - totalStartTime));
		
	}


}
