package naive_bayes;
import arimaa3.*;

public class FeatureExtractor{
	
	private GameState prevprev;
	private GameState prev;
	
	public void extract(GameState current_board, ArimaaMove expert_move){
		// feature extraction subroutine calls here
		
		
		prevprev = prev;
		prev = current_board;
	}
	
	// Calculates the piece types (e.g. 3) for each piece category (e.g. black dog).
	private void calculatePieceTypes(GameState current_board){
		// compute_secondary_bitboards has to have been called, so
		// that stronger_enemy_bb is up-to-date.
		
		short piece_types[] = new short[12];
		for (int i = 0; i < 2; i++){ // calculate for rabbits 
			short numStronger = count_bits(current_board.stronger_enemy_bb[i]);
			if (numStronger < 5)
				piece_types[i] = 7;
			else if (numStronger < 7)
				piece_types[i] = 6;
			else
				piece_types[i] = 5;
		}
		
		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
			short numStronger = count_bits(current_board.stronger_enemy_bb[i]);
			switch (numStronger) {
				case 0: piece_types[i] = 0; break;
				case 1: piece_types[i] = 1; break;
				case 2: piece_types[i] = 2; break;
				case 3: case 4: piece_types[i] = 3; break;
				default: piece_types[i] = 4; break;
			}
		}
		
		// TODO: return piece_types, or make it a class variable. Depends on use cases...
	}
	
	private static short count_bits(long n) {
		// Algorithm adapted from http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetKernighan
		short c; // c accumulates the total bits set in v
		for (c = 0; n != 0; c++)
			n &= n - 1; // clear the least significant bit set
		return c;
	}
	
	public static void main(String[] args){
		// System.out.println(count_bits(9));
	}
	
}