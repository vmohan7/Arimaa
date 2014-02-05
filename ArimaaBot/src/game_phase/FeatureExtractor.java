package game_phase;

import utilities.helper_classes.Utilities;
import ai_util.Util;
import arimaa3.*;

public class FeatureExtractor implements Constants {

	public static final int NUM_PIECES_FEATURES = 2;
	public static final int NUM_RANKS = 8;
	public static final int NUM_PIECE_RANK = 12*NUM_RANKS;
	public static final int NUM_TRAPS = 4; 
	public static final int NUM_FEATURES = NUM_PIECES_FEATURES + NUM_PIECE_RANK + 2*NUM_TRAPS;


	//PL_WHITE and PL_BLACK are constants that refer to the index into the features array
	
	/*
	 * Extracts features for the resulting board after playing a possible legal move.
	 * current_board is the resulting board after playing current_move on prev game state.
	 */
	public static double[] extractFeatures(GameState state) {
		double[] features = new double[NUM_FEATURES];
		extractNumPieces(state, features);
		extractRank(state, features);
		extractTrapDominance(state, features);
		return features;
	}
	
	private static void extractNumPieces(GameState state, double[] features){
		features[PL_WHITE] = Util.PopCnt( state.colour_bb[PL_WHITE] );
		features[PL_BLACK] = Util.PopCnt( state.colour_bb[PL_BLACK] );
	}
	
	private static void extractRank(GameState state, double[] features){
		for(int piece_type = PT_WHITE_RABBIT; piece_type <= PT_BLACK_ELEPHANT; piece_type++){
			for(int rank = 0; rank < NUM_RANKS; rank++){
				long curr_rank = RANK_1 << (rank*NUM_RANKS); //the current rank
				features[NUM_PIECES_FEATURES + piece_type*NUM_RANKS +rank] = 
						Util.PopCnt( state.piece_bb[piece_type] & curr_rank);
			}
		}
	}
	
	private static int getDistance(int cell, int trapID){
		int rank = cell/8;
		int col = cell % 8;
		
		if (trapID == 0){
			return (rank - 2) + (col - 2);
		}
		else if (trapID == 1){
			return (rank - 5) + (col - 2);
		}
		else if (trapID == 2){
			return (rank - 2) + (col - 5);
		}
		else {
			return (rank - 5) + (col - 5);
		}
		
	}
	
	private static void extractTrapDominance(GameState state, double[] features){
		int[] weights = { 9, 9, 7, 7, 5, 5, 3, 3, 2, 2, 1, 1 };
		byte[] pieceRanks = Utilities.calculatePieceTypes(state);
		for(int i = 0; i < TRAP.length; i++){
			for(int j = 0; j < state.piece_bb.length; j++){
				double score = 0;

				for (int cell = 0; cell < 64; cell++){
					if ((state.piece_bb[j] & (1L << cell)) == 1){
						int distance = getDistance(cell, i);
						if (distance > 4){
							continue; //dont consider pieces that are greater than 4 away
						}
						
						score += (1.0/distance)*weights[pieceRanks[j]];
						
					}
				}
				
				int trapFeatureID= (j % 2)*TRAP.length + i; //assigns a unique index for each (player, trap square) tuple
				features[NUM_PIECES_FEATURES + NUM_PIECE_RANK + trapFeatureID] += score;
			}
			

			
		}
		

	}


	
}