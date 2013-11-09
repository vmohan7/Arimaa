package naive_bayes;
//import ai_util.LogFile;
import arimaa3.*;

public class FeatureExtractor{
	
	/* Game state resulting from a move that we are extracting feature vectors for */
	private GameState curr;
	
	/* Original game state that is one move behind curr (i.e. before trying all possible moves from prev)*/
	private GameState prev;
	
	private GameState prev_prev;
	
	/* "Expert" move actually played by person/bot in training data from prev game state */
	private ArimaaMove expert_move;
	
	/* Piece category --> type mapping in curr GameState. E.g. if piece_types[3] = 4, this means that "black cat" 
	 * is of piece type 4 for the current GameState. */
	private byte piece_types[];
	
	public FeatureExtractor(GameState prev, ArimaaMove expert_move) {
		this.expert_move = expert_move;
		this.prev = prev;
		prev_prev = null;
		curr = null;
		piece_types = new byte[12];
	}
	
	public void extractFeatures(GameState current_board){
		curr = current_board;
		calculatePieceTypes();
		// feature extraction subroutine calls here
		
	}
	

	public void incrementMove(GameState new_board, ArimaaMove expert_move){
		this.expert_move = expert_move;
		prev_prev = prev;
		prev = new_board;
	}
	
	// Calculates the piece type (e.g. 3) for each piece category (e.g. black dog).
	private void calculatePieceTypes(){
		// compute_secondary_bitboards has to have been called, so
		// that stronger_enemy_bb is up-to-date.
		
		for (int i = 0; i < 2; i++){ // calculate for rabbits 
			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
			if (numStronger < 5)
				piece_types[i] = 7;
			else if (numStronger < 7)
				piece_types[i] = 6;
			else
				piece_types[i] = 5;
		}
		
		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
			switch (numStronger) {
				case 0: piece_types[i] = 0; break;
				case 1: piece_types[i] = 1; break;
				case 2: piece_types[i] = 2; break;
				case 3: case 4: piece_types[i] = 3; break;
				default: piece_types[i] = 4; break;
			}
		}
	}
	
	private static byte countOneBits(long n) {
		// Algorithm adapted from http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetKernighan
		byte c; // c accumulates the total bits set in v
		for (c = 0; n != 0; c++)
			n &= n - 1; // clear the least significant bit set
		return c;
	}
	
	
	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
	};


	// This has been tested and works as expected. 
	private static void testPieceTypes(){
		
		FeatureExtractor fe = new FeatureExtractor(null, null);
//	    String white ="1w Ee2 Me1 Hg2 Hb2 Df2 Dd1 Cc2 Cd2 Ra2 Rh2 Ra1 Rb1 Rc1 Rf1 Rg1 Rh1";
//	    String black = "1b ha7 db7 cc7 md7 ee7 cf7 dg7 hh7 ra8 rb8 rc8 rd8 re8 rf8 rg8 rh8";
//
//	    GameState gs = new GameState(white,black);
//	    System.out.println(gs.toBoardString());
//	    fe.extractFeatures(gs);	
	    
	    for (String text : tests) {
		      GameState position = new GameState(text);
		      System.out.println(position.toBoardString());
		      fe.extractFeatures(position);
	    }
	}
	
	public static void main(String[] args){
		// testPieceTypes();

	}
	
}