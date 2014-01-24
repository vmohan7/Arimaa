package game_phase;

import ai_util.Util;
import arimaa3.*;

public class FeatureExtractor implements Constants {

	public static final int NUM_PIECES_FEATURES = 2;
	public static final int NUM_FEATURES = NUM_PIECES_FEATURES + 12*8;
	public static final int NUM_RANKS = 8;

	//PL_WHITE and PL_BLACK are constants that refer to the index into the features array
	
	/*
	 * Extracts features for the resulting board after playing a possible legal move.
	 * current_board is the resulting board after playing current_move on prev game state.
	 */
	public static double[] extractFeatures(GameState state) {
		double[] features = new double[NUM_FEATURES];
		extractNumPieces(state, features);
		extractRank(state, features);
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


	
}