package fairy_agents;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import montecarlo.AbstractCombiner;
import feature_extractor.FeatureConstants;
import ai_util.Util;
import arimaa3.GameState;


/**
 * This class translated botFairy's evaluation function from C to Java.
 * Additionally, this provides basic integration with Jeff Bacher's code.
 * Sorry, it's pretty long... and hard to make pretty :P We tried.
 * @author Neema, Vivek
 */
public class FairyEvaluation {
		
/*
	// unused constants -- I recommend folding them with Eclipse
 	// constants defining the various squares of the board
	
	private static final int A1 = 81;
	private static final int A2 = 71;
	private static final int A3 = 61;
	private static final int A4 = 51;
	private static final int A5 = 41;
	private static final int A6 = 31;
	private static final int A7 = 21;
	private static final int A8 = 11;
	private static final int B1 = 82;
	private static final int B2 = 72;
	private static final int B3 = 62;
	private static final int B4 = 52;
	private static final int B5 = 42;
	private static final int B6 = 32;
	private static final int B7 = 22;
	private static final int B8 = 12;
	private static final int C1 = 83;
	private static final int C2 = 73;
	private static final int C3 = 63;
	private static final int C4 = 53;
	private static final int C5 = 43;
	private static final int C6 = 33;
	private static final int C7 = 23;
	private static final int C8 = 13;
	private static final int D1 = 84;
	private static final int D2 = 74;
	private static final int D3 = 64;
	private static final int D4 = 54;
	private static final int D5 = 44;
	private static final int D6 = 34;
	private static final int D7 = 24;
	private static final int D8 = 14;
	private static final int E1 = 85;
	private static final int E2 = 75;
	private static final int E3 = 65;
	private static final int E4 = 55;
	private static final int E5 = 45;
	private static final int E6 = 35;
	private static final int E7 = 25;
	private static final int E8 = 15;
	private static final int F1 = 86;
	private static final int F2 = 76;
	private static final int F3 = 66;
	private static final int F4 = 56;
	private static final int F5 = 46;
	private static final int F6 = 36;
	private static final int F7 = 26;
	private static final int F8 = 16;
	private static final int G1 = 87;
	private static final int G2 = 77;
	private static final int G3 = 67;
	private static final int G4 = 57;
	private static final int G5 = 47;
	private static final int G6 = 37;
	private static final int G7 = 27;
	private static final int G8 = 17;
	private static final int H1 = 88;
	private static final int H2 = 78;
	private static final int H3 = 68;
	private static final int H4 = 58;
	private static final int H5 = 48;
	private static final int H6 = 38;
	private static final int H7 = 28;
	private static final int H8 = 18;
	
	private static final int MAX_NUMBER_MOVES = 100;
	
	private static final int OFF_BOARD = 0x18;
	
	private static final int FLIP_SIDE = GOLD^SILVER;
	private static final int TRUE = 1;
	private static final int FALSE = 0;
 */

	// constants used for the board
	
	private static final int EMPTY_SQUARE = 0x0;
	private static final int EMPTY = 0x0;
	private static final int OFF_BOARD_SQUARE = 0x9F; 
	
	private static final int GOLD = 0x10;
	private static final int SILVER = 0x8;
	private static final int OFF_BOARD_PIECE = 0x7;
	private static final int ELEPHANT_PIECE = 0x6;
	private static final int CAMEL_PIECE = 0x5;
	private static final int HORSE_PIECE = 0x4;
	private static final int DOG_PIECE = 0x3;
	private static final int CAT_PIECE = 0x2;
	private static final int RABBIT_PIECE = 0x1;
	private static final int EMPTY_PIECE = 0x0;
	private static final int PIECE_MASK = 0x7;
	private static final int OWNER_MASK = 0x18;

	private static final int NORTH = -10;
	private static final int SOUTH = 10;
	private static final int EAST = 1;
	private static final int WEST = -1;

	private static final int ELEPHANT_VALUE = 20000;
	private static final int CAMEL_VALUE = 5000;
	private static final int HORSE_VALUE = 3000;
	private static final int DOG_VALUE = 1800;
	private static final int CAT_VALUE = 1500;
	private static final int RABBIT_VALUE = 1000;
	
	private static final int RABBIT_FREE_AHEAD = 1000;
	private static final int RABBIT_FRIENDLY_AHEAD = 500;
	private static final int RABBIT_FREE_SIDE = 300;
	private static final int RABBIT_FRIENDLY_SIDE = 200;

	private static final int[] trap_square = {33,36,63,66};
	private static final int[] direction = {NORTH,EAST,SOUTH,WEST};
	
	private static final int[] adjacent_trap = {0,0,0,0,0,0,0,0,0,0,0,
	
	                                      0, 0, 0, 0, 0, 0, 0, 0,     0,0,                               
	                                      0, 0,33, 0, 0,36, 0, 0,     0,0,                               
	                                      0,33, 0,33,36, 0,36, 0,     0,0,                               
	                                      0, 0,33, 0, 0,36, 0, 0,     0,0,                               
	                                      0, 0,63, 0, 0,66, 0, 0,     0,0,                               
	                                      0,63, 0,63,66, 0,66, 0,     0,0,                               
	                                      0, 0,63, 0, 0,66, 0, 0,     0,0,                               
	                                      0, 0, 0, 0, 0, 0, 0, 0,     0,0,                               
	
	                                     0,0,0,0,0,0,0,0,0};
	
	private static final int[] adjacent2_trap = {0,0,0,0,0,0,0,0,0,0,0,
	
	                                       0, 0,33, 0, 0,36, 0, 0,     0,0,                               
	                                       0,33, 0,33,36, 0,36, 0,     0,0,                               
	                                      33, 0, 0,36,33, 0, 0,36,     0,0,                               
	                                       0,33,63,33,36,66,36, 0,     0,0,                               
	                                       0,63,33,63,66,36,66, 0,     0,0,                               
	                                      63, 0, 0,66,63, 0, 0,66,     0,0,                               
	                                       0,63, 0,63,66, 0,66, 0,     0,0,                               
	                                       0, 0,63, 0, 0,66, 0, 0,     0,0,                               
	
	                                      0,0,0,0,0,0,0,0,0};        

	
	/** 
	 * This subclass mimics botFairy's C-struct representation of the board.
	 * In addition, it has some functionality absent in C to support testing.
	 * See the bottom of this page for an exact copy of the C-struct (and comments).
	 */
	public class FairyBoard implements FeatureConstants {
		
		private char[] board;
		char at_move; // Who is at move? // character mask for GOLD or SILVER?
		char steps; // How many steps have the side at move done so far?
		int move; // How many moves have been done in the game so far?  0 at start, 2 after setup... even means gold is at move, odd means silver is at move.  Divide by 2 and add 1 to get official move number.
		long hashkey; // 64-bit hashkey, used for index into hash table, and for collision / repetition detection
		
		/**
		 * Converts a GameState to the Fairy board representation.
		 * @param state to be converted
		 */
		public FairyBoard(GameState state) {
			board = new char[100];
			convertBoard(state);
			
			at_move = (char) ((state.getSideToMove() == PL_WHITE) ? GOLD : SILVER);
			steps = (char) (NUM_STEPS_IN_MOVE - state.getStepsRemaining()); // assumes 4 steps per move
			
			if (state.getTurnNumber() == 0) 
				move = 0;
			else //Bacher has 1-based counting system, which increments half as fast, so we subtract 2 to begin with...
				move = -2 + state.getTurnNumber() * 2 + (at_move == SILVER ? 1 : 0); // assumes 4 steps per move (sadly...)
			
			hashkey = state.getPositionHash();
		}
		
		
		/**
		 * A constructor to create a FairyBoard from a textfile.
		 * This is mainly for testing our translated evaluation against botFairy's C evaluation.
		 * @param filename The path to the textfile containing an ASCII board layout. 
		 * 				   (This file should be formatted as for botFairy.)
		 * 					e.g. (<b><i>You should look at the actual comment for formatting...
		 * 								JavaDocs butchers it</b></i>) <br>
									86w
									 +-----------------+
									8| r r r     r r r |
									7| R   r     d     |
									6|       e         |
									5|       E         |
									4|             C d |
									3|   H           R |
									2|     M     C     |
									1| R R R       R R |
									 +-----------------+
									   a b c d e f g h
		 * @throws IOException thrown if the file at <b>filename</b> is not found or if readLine() fails
		 */
		public FairyBoard(String filename) throws IOException {
			board = new char[100];
			initializeEmptyBoard(); // in C, this is done to the struct before being passed in for reading from file
			this.steps = 0; // this is how the board is constructed when reading from file in the C code
			
			// -------------------- now for the transcribed stuff...
			
			BufferedReader rd;
			//char line[100];
		    //int error_code=0; //unused -- exception thrown instead
		    int move=0;
		    //char side; //unused
		    int i, j;
			
			try {
				rd = new BufferedReader(new FileReader(filename));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw e;
			}
			
			try {
				String line = rd.readLine(); // line with move number and side to move
				for (i=0; line.charAt(i)>='0' && line.charAt(i)<='9'; i++)
		        {
		            move=move*10+line.charAt(i)-'0';
		        }
		        
				this.move = 2*move-2;
		        if (line.charAt(i)=='w')
		        {
		            this.at_move=GOLD;
		        } else 
		        {
		            this.at_move=SILVER;
		            this.move++;
		        }
		        
		        line = rd.readLine(); // line with top border of board
		        for (i=1; i<9; i++) // do this for each of the 8 lines of the board
		        {
		            line = rd.readLine();
		            for (j=1; j<9; j++)
		            {
		                switch(line.charAt(2*j+1))
		                {
		                    case 'E' :
		                        board[i*10+j]=(GOLD | ELEPHANT_PIECE);
		                        break;
		                    case 'M' :
		                        board[i*10+j]=(GOLD | CAMEL_PIECE);
		                        break;
		                    case 'H' :
		                        board[i*10+j]=(GOLD | HORSE_PIECE);
		                        break;
		                    case 'D' :
		                        board[i*10+j]=(GOLD | DOG_PIECE);
		                        break;
		                    case 'C' :
		                        board[i*10+j]=(GOLD | CAT_PIECE);
		                        break;
		                    case 'R' :
		                        board[i*10+j]=(GOLD | RABBIT_PIECE);
		                        break;
		                    case 'e' :
		                        board[i*10+j]=(SILVER | ELEPHANT_PIECE);
		                        break;
		                    case 'm' :
		                        board[i*10+j]=(SILVER | CAMEL_PIECE);
		                        break;
		                    case 'h' :
		                        board[i*10+j]=(SILVER | HORSE_PIECE);
		                        break;
		                    case 'd' :
		                        board[i*10+j]=(SILVER | DOG_PIECE);
		                        break;
		                    case 'c' :
		                        board[i*10+j]=(SILVER | CAT_PIECE);
		                        break;
		                    case 'r' :
		                        board[i*10+j]=(SILVER | RABBIT_PIECE);
		                        break;
		                    case ' ' : case 'X' :
		                        board[i*10+j]=EMPTY_SQUARE;
		                        break;
		                    default :
		                    	System.err.println("Unknown character encountered while reading board.");
		                        break;
		                }
		            }
		        }
		        rd.close();
		        this.hashkey = 0L;  
		        
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} 		
					
		}

		
		/** Helper method to populate the board char array with GameState's board information */
		private void convertBoard(GameState state){
			initializeEmptyBoard();
			for (int pieceType = PT_WHITE_RABBIT; pieceType <= PT_BLACK_ELEPHANT; pieceType++) {
				long bitboard = state.piece_bb[pieceType];
				while (bitboard != 0L){
					int index64 = Util.FirstOne(bitboard);
					updateBoardWithPiece(index64, pieceType);
					bitboard ^= (1L << index64);
				}
			}
		}
		
		/** Sets up OFF_BOARD_SQUAREs and EMPTY_SQUAREs for the playable part of the board */
		private void initializeEmptyBoard(){
			// Initialize all squares to be off the board at first
			for (int i = 0; i < 100; i++){
		        board[i] = OFF_BOARD_SQUARE;
		    }
			
			// All squares actually on the board are empty
		    for (int i = 1; i <= 8; i++) {
		        for (int j = 1; j <= 8; j++) {
		            board[10 * i + j] = EMPTY_SQUARE;
		        }
		    }			
		}
		
		/**
		 * Puts a piece on board at a particular square. (This method handles conversions from Bacher's conventions.)
		 * @param index64 the index into the long piece bitboard (0-63). [<i>8 * row + col, where (row = 0, col = 0) is a1</i>]
		 * @param bacherPieceType the values specified in arimaa3.Constants.java (0-11)
		 */
		private void updateBoardWithPiece(int index64, int bacherPieceType) {
			// convert index64 to board's index
			int row64 = index64 >>> 3; //index64 / 8;
			int col64 = index64 & 0x7; //index64 % 8;
			
			int flippedRow64 = (8 - 1) - row64; // 0th row becomes top row, rather than bottom
			
			int index = (flippedRow64 + 1) * 10 + (col64 + 1);
						
			// convert bacherPieceType to owner and piece type
			boolean isGold = bacherPieceType % 2 == PL_WHITE;
			int ownerBits = isGold ? GOLD : SILVER;
			int pieceType = (bacherPieceType / 2) + 1;
			
			// update board
			board[index] &= ~0x1F; //clear garbage from lower 5 bits
			board[index] |= ownerBits; 
			board[index] |= pieceType; // update with pieceType bits
		}

		/**
		 * @return the board
		 */
		public char[] getBoard() {
			return Arrays.copyOf(board, board.length);
		}

		/**
		 * @return the at_move
		 */
		public char getAtMove() {
			return at_move;
		}

		/**
		 * @return the steps
		 */
		public char getSteps() {
			return steps;
		}

		/**
		 * @return the move
		 */
		public int getMove() {
			return move;
		}

		/**
		 * @return the hashkey
		 */
		public long getHashkey() {
			return hashkey;
		}

		/**
		 * @param bpIndex The index on the board (using Fairy's board index)
		 * @return Character representing the piece (e.g. 'D' for gold dog; 'm' for silver camel)
		 */
		private char getPieceText(int bpIndex) {
			boolean isGold = OWNER(bpIndex) == GOLD;
			
			switch (PIECE(bpIndex)) {
				case EMPTY_PIECE: return ' ';
				case OFF_BOARD_PIECE: return 'x';
				case RABBIT_PIECE: return isGold ? 'R' : 'r';
				case CAT_PIECE: return isGold ? 'C' : 'c';
				case DOG_PIECE: return isGold ? 'D' : 'd';
				case HORSE_PIECE: return isGold ? 'H' : 'h';
				case CAMEL_PIECE: return isGold ? 'M' : 'm';
				case ELEPHANT_PIECE: return isGold ? 'E' : 'e';
				default: return 'x';
			}
		}

		/**  Who owns a (piece on a) bpIndex? */
		public int OWNER(int bpIndex) { 
			return (board[bpIndex] & OWNER_MASK); 
		}
		
		/** What piece is on a bpIndex? */
		public int PIECE(int bpIndex) { 
			return (board[bpIndex] & PIECE_MASK); 
		} 
		
		/** What is on a bpIndex?  Returns the owner | piece combination. */
		public int BOARD(int bpIndex) { 
			return (board[bpIndex]); 
		} 
		
		/** What row is a bpIndex in?  1 = bottom, 8 = top */
		public int ROW(int bpIndex) { 
			return (9-bpIndex/10); 
		} 
		
		/** What column is a bpIndex in?  1 = left (a), 8 = right (h) */
		public int COL(int bpIndex) { 
			return (bpIndex%10); 
		} 
		

		/** 
		 * The string representation of the fairy board, tailored to look almost 
		 * identical to GameState's boardString() 
		 * */
		public String toBoardString() {
			StringBuilder sb = new StringBuilder();

			sb.append(getMove());
			sb.append(getAtMove() == GOLD ? " (gold to play)" : " (silver to play)");
			
		    // display the board
		    sb.append(" \n +-----------------+\n");
		    
		    // loop over board, initialize board state info and find where all the pieces are.
		    for (int row = 1; row <= 8; row++) {
		    	sb.append((8+1) - row); sb.append("|");
		    			    	
		    	for (int col = 1; col <= 8; col++) {
		    		int index = row * 10 + col;
			        sb.append(" "); sb.append(getPieceText(index));

		    	}
		    	sb.append(" |\n");

		    }

		    sb.append(" +-----------------+\n");
		    sb.append("   a b c d e f g h\n");

		    sb.append("TS: "); sb.append(16 + (getMove() - 2) * 4 /*total_steps*/); sb.append("\n");
			
			return sb.toString();
		}
		
		/**
		 * Uses the string representations of the board to determine equality
		 * (after some String processing to eliminate cosmetic differences).
		 * @param gameState the candidate for equality
		 * @return true if the boards are equivalent, false otherwise
		 */
		public boolean isEqualToGS(GameState gameState) {
			String gsString = gameState.toBoardString();
			String gsBoardOnly = gsString.substring(gsString.indexOf('\n') + 1);
			String fairyString = toBoardString();
			String fairyBoardOnly = fairyString.substring(fairyString.indexOf('\n') + 1);
			
			return fairyBoardOnly.equals(gsBoardOnly);
		}
		

	}
	
	
/* ------------------------------------ FOR TESTING ONLY ------------------------------------ */ 
	private static final String TESTS_EXTENSION = "C:/Users/Neema/Desktop/Downloaded Bots/faerie/botFairy/Fairy_full/";
	private static final String TESTS_DOCUMENT = "tests.txt";
	private static final String OUTPUT = "C:/Users/Neema/Desktop/" /*TESTS_EXTENSION*/ + "test_results_navv_Java.txt";
	
	/** 
	 * Reads Fairy's test boards and outputs scores for each one. Ideally,
	 * these will match the tests from the evaluation function in C.
	 * I have written code in C that outputs in this exact format.
	 * Currently, that C code is only local. I have included it at the bottom of this file.
	 * See FairyBoard's constructor for "limitations" on the ASCII board.
	 * @author Neema
	 */
	public static void main(String args[]) {
		try {
			FairyEvaluation fe = new FairyEvaluation(); // for the eval method
			BufferedReader testsRd = new BufferedReader(new FileReader(TESTS_EXTENSION + TESTS_DOCUMENT));
			PrintWriter testsWr = new PrintWriter(OUTPUT);
			
			int nTestCases = Integer.parseInt(readLineIgnoreComments(testsRd));
			for (int test = 0; test < nTestCases; test++) {
				String filename = readLineIgnoreComments(testsRd);
				assert(filename != null);
				
				
				int[] scoreComponents = fe.mainTestEval(fe.new FairyBoard(TESTS_EXTENSION + filename));
				
				int score = 0;
				for (int i = 0; i < scoreComponents.length; i++) score += scoreComponents[i];
				
				testsWr.printf("File %s: Score = %5d   ||||   (material: %5d || trap: %5d || rabbit: %5d)%n",
						filename, score, scoreComponents[0], scoreComponents[1], scoreComponents[2]);
				
				String toSkip = readLineIgnoreComments(testsRd);
				assert(toSkip != null);
			}
			
			testsRd.close();
			testsWr.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Exiting...");
			return;
		}
		
	}
	
	private static String readLineIgnoreComments(BufferedReader rd) throws IOException {
		while (true) {
			String line = rd.readLine();
			if (line == null) return null;
			if (line.isEmpty()) return "";
			if (line.charAt(0) == '#') continue;
			return line;
		}
	}
/* ----------------------------------[END] FOR TESTING ONLY ---------------------------------- */
	
	
	/** Evaluates a GameState according to Fairy's eval.c! */
	public static double evaluate(GameState state, AbstractCombiner combiner) {
		FairyEvaluation fe = new FairyEvaluation();
		return fe.EVAL_Eval(state, combiner, 0);
	}
	
	/** 
	 * The real evaluation is done here... 
	 * DISCLAIMER: This code has been transposed from C (from bot_Fairy) -- it is not ours.
	 * 
	 * Evaluation is done from gold's perspective.  At the end of the evaluation, 
	 * it's adjusted to be seen from current player's perspective.
	 */
	private double EVAL_Eval(GameState evalState, AbstractCombiner combiner, int verbose) {
		FairyBoard bp = new FairyBoard(evalState);
				
	    // evaluation constants
	    int[] piece_value = {0,RABBIT_VALUE,CAT_VALUE,DOG_VALUE,HORSE_VALUE,CAMEL_VALUE,ELEPHANT_VALUE};
	    // variables        
	    
	    // utility variables
	    int[] side_mask = new int[OWNER_MASK];
	     
	    // loop variables
	    int square; 
	    int side;
	    int trap;
	    int dir;
	    int i;
	            
	    // value variables
	    double value=0.0; // originally an int in eval.c
	    int[] material_value = new int[2];
	    int[] trap_value = new int[2];
	    int[] rabbit_value = new int[2];
	    
	    // how many of the pieces do the players have, and where are they?
	    int[] elephants = new int[2];
	    int[][] elephant_pos = new int[2][1]; 
	    int[] camels = new int[2];
	    int[][] camel_pos = new int[2][1];
	    int[] horses = new int[2];
	    int[][] horse_pos = new int[2][2];
	    int[] dogs = new int[2];
	    int[][] dog_pos = new int[2][2];
	    int[] cats = new int[2];
	    int[][] cat_pos = new int[2][2];
	    int[] rabbits = new int[2];
	    int[][] rabbit_pos = new int[2][8];
	    
	    // trap evaluation variables
	    int[] trap_adjacent = new int[2];
	    int[] trap_adjacent_strength = new int[2];
	    int[] trap_adjacent_strongest = new int[2];
	                                     
	    // material evaluation variables
	    int[] material = new int[100]; // What is the piece on this square worth?
	    int piece_frozen;
	    int piece_adjacent_stronger_enemy;
	    int piece_adjacent_empty;
	    int[] piece_adjacent_strongest = new int[2];
	    int[] piece_adjacent = new int[2];
	    int piece_adjacent_trap;
	    
	    // rabbit evaluation variables
	    int row;
	    
	    // Initialize some evaluation stuff
	
	    side_mask[GOLD]=0;
	    side_mask[SILVER]=1;
	
	    // Determine extra information about the board state
	
	    for (square=11; square<=88; square++) // loop over board, initialize board state info and find where all the pieces are.
	    {
	        if (square%10==9) square+=2;
	        switch (bp.PIECE(square))
	        {
	            case ELEPHANT_PIECE :
	                elephant_pos[side_mask[bp.OWNER(square)]][elephants[side_mask[bp.OWNER(square)]]]=square;
	                elephants[side_mask[bp.OWNER(square)]]++;
	                break;
	            case CAMEL_PIECE :
	                camel_pos[side_mask[bp.OWNER(square)]][camels[side_mask[bp.OWNER(square)]]]=square;
	                camels[side_mask[bp.OWNER(square)]]++;
	                break;
	            case HORSE_PIECE :
	                horse_pos[side_mask[bp.OWNER(square)]][horses[side_mask[bp.OWNER(square)]]]=square;
	                horses[side_mask[bp.OWNER(square)]]++;
	                break;
	            case DOG_PIECE :
	                dog_pos[side_mask[bp.OWNER(square)]][dogs[side_mask[bp.OWNER(square)]]]=square;
	                dogs[side_mask[bp.OWNER(square)]]++;
	                break;
	            case CAT_PIECE :
	                cat_pos[side_mask[bp.OWNER(square)]][cats[side_mask[bp.OWNER(square)]]]=square;
	                cats[side_mask[bp.OWNER(square)]]++;
	                break;
	            case RABBIT_PIECE :
	                rabbit_pos[side_mask[bp.OWNER(square)]][rabbits[side_mask[bp.OWNER(square)]]]=square;
	                rabbits[side_mask[bp.OWNER(square)]]++;
	                break;
	        }
	        if (bp.OWNER(square)==GOLD || bp.OWNER(square)==SILVER)
	        {
	            material[square]=piece_value[bp.PIECE(square)];
	        } else
	        {
	            material[square]=0;
	        }
	    }
	    
	    // Evaluate trap squares, decide trap ownership.
	
	    /* if (verbose)
	    {
	        sprintf(message,"Evaluating traps:\n");
	        BOARD_Message();
	    } */       
	    for (trap=0; trap<4; trap++)
	    {
	        for (side=0; side<2; side++)
	        {
	            trap_adjacent[side]=0;
	            trap_adjacent_strength[side]=0;
	            trap_adjacent_strongest[side]=0;
	        }
	        for (dir=0; dir<4; dir++)
	        {
	            switch (bp.OWNER(trap_square[trap]+direction[dir]))
	            {
	                case GOLD :
	                    trap_adjacent[0]++;
	                    trap_adjacent_strength[0]+=bp.PIECE(trap_square[trap]+direction[dir]);
	                    if (bp.PIECE(trap_square[trap]+direction[dir])>trap_adjacent_strongest[0])
	                    {
	                        trap_adjacent_strongest[0]=bp.PIECE(trap_square[trap]+direction[dir]);
	                    }
	                    break;
	                case SILVER :
	                    trap_adjacent[1]++;
	                    trap_adjacent_strength[1]+=bp.PIECE(trap_square[trap]+direction[dir]);
	                    if (bp.PIECE(trap_square[trap]+direction[dir])>trap_adjacent_strongest[1])
	                    {
	                        trap_adjacent_strongest[1]=bp.PIECE(trap_square[trap]+direction[dir]);
	                    }
	                    break;
	            }
	        }
	        // Basically, 200 points are given out per trap.  50 to whoever has the strongest piece by the trap, 
	        // and 150 points split according to total strength of pieces, with two neutral strength added.
	        
	        // case 1 - only one side has pieces by the trap.
	        if (trap_adjacent[0]>0 && trap_adjacent[1]==0) 
	        {
	            trap_value[0]+=50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+1);
	            /* if (verbose)
	            {
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth Gold (%d) - Silver (%d).\n",50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+1),0);
	                BOARD_Message();
	            } */
	        }
	        if (trap_adjacent[1]>0 && trap_adjacent[0]==0)
	        {
	            trap_value[1]+=50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[1]+1);
	            /* if (verbose)
	            {
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth Gold (%d) - Silver (%d).\n",0,50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[1]+1));
	                BOARD_Message();
	            } */
	        }
	        // case 2 - both sides have pieces by the trap.
	        if (trap_adjacent[0]>0 && trap_adjacent[1]>0)
	        {
	            // subcase 1 - they are equally strong.  Split 100 points according to number of pieces.
	            if (trap_adjacent_strongest[0]==trap_adjacent_strongest[1])
	            {
	                trap_value[0]+=trap_adjacent_strength[0]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                trap_value[1]+=trap_adjacent_strength[1]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(trap_square[trap]);
	                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",trap_adjacent_strength[0]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),trap_adjacent_strength[1]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
	                    BOARD_Message();
	                } */
	            }
	            // subcase 2 - gold is stronger.  Give 50 points to gold, and split 50 according to number of pieces.
	            if (trap_adjacent_strongest[0]>trap_adjacent_strongest[1])
	            {
	                trap_value[0]+=50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                trap_value[1]+=trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(trap_square[trap]);
	                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
	                    BOARD_Message();
	                } */
	            }
	            // subcase 3 - silver is stronger.  Give 50 points to silver, and split 50 according to number of pieces.
	            if (trap_adjacent_strongest[1]>trap_adjacent_strongest[0])
	            {
	                trap_value[0]+=trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                trap_value[1]+=50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(trap_square[trap]);
	                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
	                    BOARD_Message();
	                } */
	            }
	        }
	        // special case - give minus for (possible) frames
	        if (bp.OWNER(trap_square[trap])==GOLD && trap_adjacent[1]>2)
	        {
	            material[trap_square[trap]]=combiner.frameValue(material[trap_square[trap]]); //*4/5; // Trapped piece loses 20% of its value
	            /* if (verbose)
	            {
	                sprintf(message,"Piece at ");
	                BOARD_Message();
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth %d due to being (possibly) framed.\n",material[trap_square[trap]]);
	                BOARD_Message();
	            } */
	        }
	        if (bp.OWNER(trap_square[trap])==SILVER && trap_adjacent[0]>2)
	        {
	            material[trap_square[trap]]=combiner.frameValue(material[trap_square[trap]]); //*4/5; // Trapped piece loses 20% of its value
	            /* if (verbose)
	            {
	                sprintf(message,"Piece at ");
	                BOARD_Message();
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth %d due to being (possibly) framed.\n",material[trap_square[trap]]);
	                BOARD_Message();
	            } */
	        }
	    }
	    
	    // Evaluate material and individual pieces.
	
	    for (side=0; side<2; side++)
	    {
	        for (i=0; i<cats[side]; i++)
	        {
	            switch (side)
	            {
	                case 0 : 
	                    row=bp.ROW(cat_pos[0][i]);
	                    break;
	                case 1 :
	                default: // needed to include so Java knows row was initialized
	                    row=9-bp.ROW(cat_pos[1][i]);
	                    break;
	            }
	            if (row>3)
	            {
	                material[cat_pos[side][i]]=combiner.advancedValue(material[cat_pos[side][i]]); //*197/200; // Advanced cat lose 1.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(cat_pos[side][i]);
	                    sprintf(message," is worth %d due to being an advanced cat.\n",material[cat_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            } else if (row==3)
	            {
	                material[cat_pos[side][i]]=combiner.slightlyAdvancedValue(material[cat_pos[side][i]]); //*199/200; // Slightly advanced cat lose 0.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(cat_pos[side][i]);
	                    sprintf(message," is worth %d due to being a slightly advanced cat.\n",material[cat_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            }
	        }
	    
	        for (i=0; i<dogs[side]; i++)
	        {
	            switch (side)
	            {
	                case 0 : 
	                    row=bp.ROW(dog_pos[0][i]);
	                    break;
	                case 1 :
	                default: // needed to include so Java knows row was initialized
	                    row=9-bp.ROW(dog_pos[1][i]);
	                    break;
	            }
	            if (row>3)
	            {
	                material[dog_pos[side][i]]=combiner.advancedValue(material[dog_pos[side][i]]); //*197/200; // Advanced dog lose 1.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(dog_pos[side][i]);
	                    sprintf(message," is worth %d due to being an advanced dog.\n",material[dog_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            } else if (row==3)
	            {
	                material[dog_pos[side][i]]=combiner.slightlyAdvancedValue(material[dog_pos[side][i]]); //*199/200; // Slightly advanced dog lose 0.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(dog_pos[side][i]);
	                    sprintf(message," is worth %d due to being a slightly advanced dog.\n",material[dog_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            }
	        }
	    }
	
	    for (square=11; square<=88; square++)
	    {    
	        if (square%10==9) square+=2;
	        if (bp.OWNER(square)==GOLD || bp.OWNER(square)==SILVER)
	        {
	            // Check if it's frozen, number of adjacent empty, strongest adjacent, and all that
	            piece_adjacent[0]=0;
	            piece_adjacent[1]=0;
	            piece_adjacent_empty=0;
	            piece_adjacent_strongest[0]=0;
	            piece_adjacent_strongest[1]=0;
	            for (dir=0; dir<4; dir++)
	            {
	                switch (bp.OWNER(square+direction[dir]))
	                {
	                    case GOLD :
	                        piece_adjacent[0]++;
	                        if (bp.PIECE(square+direction[dir])>piece_adjacent_strongest[0])
	                        {
	                            piece_adjacent_strongest[0]=bp.PIECE(square+direction[dir]);
	                        }
	                        break;
	                    case SILVER :
	                        piece_adjacent[1]++;
	                        if (bp.PIECE(square+direction[dir])>piece_adjacent_strongest[1])
	                        {
	                            piece_adjacent_strongest[1]=bp.PIECE(square+direction[dir]);
	                        }
	                        break;
	                    case EMPTY :
	                        piece_adjacent_empty++;
	                        break;
	                }
	            }
	            switch (bp.OWNER(square))
	            {
	                case GOLD :
	                    piece_adjacent_stronger_enemy=piece_adjacent_strongest[1]>bp.PIECE(square) ? 1 : 0; // used ternary to mimic c evaluation of conditions
	                    piece_frozen=(piece_adjacent_stronger_enemy == 1 && piece_adjacent[0]==0) ? 1 : 0;
	                    break;
	                case SILVER :
	                default		: // more initialization hacks
	                    piece_adjacent_stronger_enemy=piece_adjacent_strongest[0]>bp.PIECE(square) ? 1 : 0;
	                    piece_frozen=(piece_adjacent_stronger_enemy == 1 && piece_adjacent[1]==0) ? 1 : 0;
	                    break;
	            }
	            if (piece_frozen == 1)
	            {
	                material[square]=material[square]*9/10; // Frozen piece loses 10% of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(square);
	                    sprintf(message," is worth %d due to being frozen.\n",material[square]);
	                    BOARD_Message();
	                } */
	            }
	            if (piece_adjacent_empty==0) 
	            {
	                material[square]=material[square]*199/200; // Immobile piece loses 0.5% of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(square);
	                    sprintf(message," is worth %d due to being immobile.\n",material[square]);
	                    BOARD_Message();
	                } */
	            }
	            if ((piece_frozen == 1 || piece_adjacent_empty==0) && piece_adjacent_stronger_enemy == 1) // Our piece has limited mobility, and there is a stronger enemy piece adjacent
	            {
	                // Check if it's held hostage or threatened by a capture
	                if (adjacent_trap[square] != 0) // It's adjacent to a trap
	                {
	                    // If we have no other piece next to the trap, then consider this piece to be threatened, losing 30% of its value
	                    piece_adjacent_trap=0;
	                    for (dir=0; dir<4; dir++)
	                    {
	                        if (bp.OWNER(adjacent_trap[square]+direction[dir])==bp.OWNER(square))
	                        {
	                            piece_adjacent_trap++;
	                        }
	                    }
	                    if (piece_adjacent_trap==1)
	                    {
	                        material[square]=material[square]*7/10;
	                        /* if (verbose)
	                        {
	                            PRINT_SQUARE(square);
	                            sprintf(message," is worth %d due to being threatened (distance 1).\n",material[square]);
	                            BOARD_Message();
	                        } */
	                    }
	                }
	                if (adjacent2_trap[square] != 0 && bp.BOARD(adjacent2_trap[square])==EMPTY_SQUARE) 
	                // It's two steps away from an empty trap
	                {
	                    // If we have no piece next to the trap,
	                    // Really - should check so that there is a free path to trap.
	                    // then consider this piece to be threatened, losing 30% of its value
	                    piece_adjacent_trap=0;
	                    for (dir=0; dir<4; dir++)
	                    {
	                        if (bp.OWNER(adjacent2_trap[square]+direction[dir])==bp.OWNER(square))
	                        {
	                            piece_adjacent_trap++;
	                        }
	                    }
	                    if (piece_adjacent_trap==0)
	                    {
	                        material[square]=material[square]*7/10;
	                        /* if (verbose)
	                        {
	                            PRINT_SQUARE(square);
	                            sprintf(message," is worth %d due to being threatened (distance 2).\n",material[square]);
	                            BOARD_Message();
	                        } */
	                    }
	                }
	            }
	            // Another case - if adjacent to a trap, and no other friendly piece adjacent, various possibilities for being threatened....
	            switch (bp.OWNER(square))
	            {
	                case GOLD :
	                    material_value[0]+=material[square];
	                    break;
	                case SILVER :
	                    material_value[1]+=material[square];
	                    break;
	            }
	        }
	    }
	    
	    // Evaluate rabbits
	
	    for (i=0; i<rabbits[0]; i++)
	    {
	        row=bp.ROW(rabbit_pos[0][i]);
	        rabbit_value[0]+=(row-1)*(row-1)*(row-1);
	        if (row==7)
	        {
	            switch (bp.OWNER(rabbit_pos[0][i]+NORTH))
	            {
	                case EMPTY :
	                    rabbit_value[0]+=RABBIT_FREE_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to free space ahead\n",RABBIT_FREE_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	                case GOLD :
	                    rabbit_value[0]+=RABBIT_FRIENDLY_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to friendly piece ahead\n",RABBIT_FRIENDLY_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[0][i]+EAST))
	            {
	                case EMPTY :
	                    rabbit_value[0]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to free space to the east\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case GOLD :
	                    rabbit_value[0]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to friendly piece to the east\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[0][i]+WEST))
	            {
	                case EMPTY :
	                    rabbit_value[0]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to free space to the west\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case GOLD :
	                    rabbit_value[0]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to friendly piece to the west\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	        }
	    }
	    for (i=0; i<rabbits[1]; i++)
	    {
	        row=9-bp.ROW(rabbit_pos[1][i]);
	        rabbit_value[1]+=(row-1)*(row-1)*(row-1); // Modified from original Fairy code to match gold rabbit value calculation
//	        rabbit_value[1]+=(row-1)*(row-1);		  // Original
	        if (row==7)
	        {
	            switch (bp.OWNER(rabbit_pos[1][i]+SOUTH))
	            {
	                case EMPTY :
	                    rabbit_value[1]+=RABBIT_FREE_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to free space ahead\n",RABBIT_FREE_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	                case SILVER :
	                    rabbit_value[1]+=RABBIT_FRIENDLY_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to friendly piece ahead\n",RABBIT_FRIENDLY_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[1][i]+EAST))
	            {
	                case EMPTY :
	                    rabbit_value[1]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to free space to the east\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case SILVER :
	                    rabbit_value[1]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to friendly piece to the east\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[1][i]+WEST))
	            {
	                case EMPTY :
	                    rabbit_value[1]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to free space to the west\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case SILVER :
	                    rabbit_value[1]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to friendly piece to the west\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	        }
	    }
	    
	    // Add up all the factors
	    int materialValue = material_value[0] - material_value[1];
	    int trapValue = trap_value[0] - trap_value[1];
	    int rabbitValue = rabbit_value[0] - rabbit_value[1];
	    value = combiner.combineScore(materialValue, trapValue, rabbitValue);
//	    value+=material_value[0]-material_value[1];
//	    value+=trap_value[0]-trap_value[1];
//	    value+=rabbit_value[0]-rabbit_value[1];
	
	    /* if (verbose)
	    {
	        sprintf(message,"Material value Gold (%d) - Silver (%d)\n",material_value[0],material_value[1]);
	        BOARD_Message();
	        sprintf(message,"Trap value Gold (%d) - Silver (%d)\n",trap_value[0],trap_value[1]);
	        BOARD_Message();
	        sprintf(message,"Rabbit value Gold (%d) - Silver (%d)\n",rabbit_value[0],rabbit_value[1]);
	        BOARD_Message();
	    } */
	        
	    // Adjust evaluation to be from the perspective of the present player.
	
	    if (bp.getAtMove()==SILVER)
	    {
	        value=-value;
	    }
	    return value;
	}




	/** 
	 * Copied and slightly modified version of evaluation for our test in main.
	 * Returns each component of the score calculation.
	 */
	private int[] mainTestEval(FairyBoard bp) {
		
	    // evaluation constants
	    int[] piece_value = {0,RABBIT_VALUE,CAT_VALUE,DOG_VALUE,HORSE_VALUE,CAMEL_VALUE,ELEPHANT_VALUE};
	    // variables        
	    
	    // utility variables
	    int[] side_mask = new int[OWNER_MASK];
	     
	    // loop variables
	    int square; 
	    int side;
	    int trap;
	    int dir;
	    int i;
	            
	    // value variables
	    // double value=0.0; // originally an int in eval.c
	    int[] material_value = new int[2];
	    int[] trap_value = new int[2];
	    int[] rabbit_value = new int[2];
	    
	    // how many of the pieces do the players have, and where are they?
	    int[] elephants = new int[2];
	    int[][] elephant_pos = new int[2][1]; 
	    int[] camels = new int[2];
	    int[][] camel_pos = new int[2][1];
	    int[] horses = new int[2];
	    int[][] horse_pos = new int[2][2];
	    int[] dogs = new int[2];
	    int[][] dog_pos = new int[2][2];
	    int[] cats = new int[2];
	    int[][] cat_pos = new int[2][2];
	    int[] rabbits = new int[2];
	    int[][] rabbit_pos = new int[2][8];
	    
	    // trap evaluation variables
	    int[] trap_adjacent = new int[2];
	    int[] trap_adjacent_strength = new int[2];
	    int[] trap_adjacent_strongest = new int[2];
	                                     
	    // material evaluation variables
	    int[] material = new int[100]; // What is the piece on this square worth?
	    int piece_frozen;
	    int piece_adjacent_stronger_enemy;
	    int piece_adjacent_empty;
	    int[] piece_adjacent_strongest = new int[2];
	    int[] piece_adjacent = new int[2];
	    int piece_adjacent_trap;
	    
	    // rabbit evaluation variables
	    int row;
	    
	    // Initialize some evaluation stuff
	
	    side_mask[GOLD]=0;
	    side_mask[SILVER]=1;
	
	    // Determine extra information about the board state
	
	    for (square=11; square<=88; square++) // loop over board, initialize board state info and find where all the pieces are.
	    {
	        if (square%10==9) square+=2;
	        switch (bp.PIECE(square))
	        {
	            case ELEPHANT_PIECE :
	                elephant_pos[side_mask[bp.OWNER(square)]][elephants[side_mask[bp.OWNER(square)]]]=square;
	                elephants[side_mask[bp.OWNER(square)]]++;
	                break;
	            case CAMEL_PIECE :
	                camel_pos[side_mask[bp.OWNER(square)]][camels[side_mask[bp.OWNER(square)]]]=square;
	                camels[side_mask[bp.OWNER(square)]]++;
	                break;
	            case HORSE_PIECE :
	                horse_pos[side_mask[bp.OWNER(square)]][horses[side_mask[bp.OWNER(square)]]]=square;
	                horses[side_mask[bp.OWNER(square)]]++;
	                break;
	            case DOG_PIECE :
	                dog_pos[side_mask[bp.OWNER(square)]][dogs[side_mask[bp.OWNER(square)]]]=square;
	                dogs[side_mask[bp.OWNER(square)]]++;
	                break;
	            case CAT_PIECE :
	                cat_pos[side_mask[bp.OWNER(square)]][cats[side_mask[bp.OWNER(square)]]]=square;
	                cats[side_mask[bp.OWNER(square)]]++;
	                break;
	            case RABBIT_PIECE :
	                rabbit_pos[side_mask[bp.OWNER(square)]][rabbits[side_mask[bp.OWNER(square)]]]=square;
	                rabbits[side_mask[bp.OWNER(square)]]++;
	                break;
	        }
	        if (bp.OWNER(square)==GOLD || bp.OWNER(square)==SILVER)
	        {
	            material[square]=piece_value[bp.PIECE(square)];
	        } else
	        {
	            material[square]=0;
	        }
	    }
	    
	    // Evaluate trap squares, decide trap ownership.
	
	    /* if (verbose)
	    {
	        sprintf(message,"Evaluating traps:\n");
	        BOARD_Message();
	    } */       
	    for (trap=0; trap<4; trap++)
	    {
	        for (side=0; side<2; side++)
	        {
	            trap_adjacent[side]=0;
	            trap_adjacent_strength[side]=0;
	            trap_adjacent_strongest[side]=0;
	        }
	        for (dir=0; dir<4; dir++)
	        {
	            switch (bp.OWNER(trap_square[trap]+direction[dir]))
	            {
	                case GOLD :
	                    trap_adjacent[0]++;
	                    trap_adjacent_strength[0]+=bp.PIECE(trap_square[trap]+direction[dir]);
	                    if (bp.PIECE(trap_square[trap]+direction[dir])>trap_adjacent_strongest[0])
	                    {
	                        trap_adjacent_strongest[0]=bp.PIECE(trap_square[trap]+direction[dir]);
	                    }
	                    break;
	                case SILVER :
	                    trap_adjacent[1]++;
	                    trap_adjacent_strength[1]+=bp.PIECE(trap_square[trap]+direction[dir]);
	                    if (bp.PIECE(trap_square[trap]+direction[dir])>trap_adjacent_strongest[1])
	                    {
	                        trap_adjacent_strongest[1]=bp.PIECE(trap_square[trap]+direction[dir]);
	                    }
	                    break;
	            }
	        }
	        // Basically, 200 points are given out per trap.  50 to whoever has the strongest piece by the trap, 
	        // and 150 points split according to total strength of pieces, with two neutral strength added.
	        
	        // case 1 - only one side has pieces by the trap.
	        if (trap_adjacent[0]>0 && trap_adjacent[1]==0) 
	        {
	            trap_value[0]+=50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+1);
	            /* if (verbose)
	            {
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth Gold (%d) - Silver (%d).\n",50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+1),0);
	                BOARD_Message();
	            } */
	        }
	        if (trap_adjacent[1]>0 && trap_adjacent[0]==0)
	        {
	            trap_value[1]+=50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[1]+1);
	            /* if (verbose)
	            {
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth Gold (%d) - Silver (%d).\n",0,50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[1]+1));
	                BOARD_Message();
	            } */
	        }
	        // case 2 - both sides have pieces by the trap.
	        if (trap_adjacent[0]>0 && trap_adjacent[1]>0)
	        {
	            // subcase 1 - they are equally strong.  Split 100 points according to number of pieces.
	            if (trap_adjacent_strongest[0]==trap_adjacent_strongest[1])
	            {
	                trap_value[0]+=trap_adjacent_strength[0]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                trap_value[1]+=trap_adjacent_strength[1]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(trap_square[trap]);
	                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",trap_adjacent_strength[0]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),trap_adjacent_strength[1]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
	                    BOARD_Message();
	                } */
	            }
	            // subcase 2 - gold is stronger.  Give 50 points to gold, and split 50 according to number of pieces.
	            if (trap_adjacent_strongest[0]>trap_adjacent_strongest[1])
	            {
	                trap_value[0]+=50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                trap_value[1]+=trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(trap_square[trap]);
	                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
	                    BOARD_Message();
	                } */
	            }
	            // subcase 3 - silver is stronger.  Give 50 points to silver, and split 50 according to number of pieces.
	            if (trap_adjacent_strongest[1]>trap_adjacent_strongest[0])
	            {
	                trap_value[0]+=trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                trap_value[1]+=50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(trap_square[trap]);
	                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
	                    BOARD_Message();
	                } */
	            }
	        }
	        // special case - give minus for (possible) frames
	        if (bp.OWNER(trap_square[trap])==GOLD && trap_adjacent[1]>2)
	        {
	            material[trap_square[trap]]=material[trap_square[trap]]*4/5; // Trapped piece loses 20% of its value
	            /* if (verbose)
	            {
	                sprintf(message,"Piece at ");
	                BOARD_Message();
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth %d due to being (possibly) framed.\n",material[trap_square[trap]]);
	                BOARD_Message();
	            } */
	        }
	        if (bp.OWNER(trap_square[trap])==SILVER && trap_adjacent[0]>2)
	        {
	            material[trap_square[trap]]=material[trap_square[trap]]*4/5; // Trapped piece loses 20% of its value
	            /* if (verbose)
	            {
	                sprintf(message,"Piece at ");
	                BOARD_Message();
	                PRINT_SQUARE(trap_square[trap]);
	                sprintf(message," is worth %d due to being (possibly) framed.\n",material[trap_square[trap]]);
	                BOARD_Message();
	            } */
	        }
	    }
	    
	    // Evaluate material and individual pieces.
	
	    for (side=0; side<2; side++)
	    {
	        for (i=0; i<cats[side]; i++)
	        {
	            switch (side)
	            {
	                case 0 : 
	                    row=bp.ROW(cat_pos[0][i]);
	                    break;
	                case 1 :
	                default: // needed to include so Java knows row was initialized
	                    row=9-bp.ROW(cat_pos[1][i]);
	                    break;
	            }
	            if (row>3)
	            {
	                material[cat_pos[side][i]]=material[cat_pos[side][i]]*197/200; // Advanced cat lose 1.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(cat_pos[side][i]);
	                    sprintf(message," is worth %d due to being an advanced cat.\n",material[cat_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            } else if (row==3)
	            {
	                material[cat_pos[side][i]]=material[cat_pos[side][i]]*199/200; // Slightly advanced cat lose 0.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(cat_pos[side][i]);
	                    sprintf(message," is worth %d due to being a slightly advanced cat.\n",material[cat_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            }
	        }
	    
	        for (i=0; i<dogs[side]; i++)
	        {
	            switch (side)
	            {
	                case 0 : 
	                    row=bp.ROW(dog_pos[0][i]);
	                    break;
	                case 1 :
	                default: // needed to include so Java knows row was initialized
	                    row=9-bp.ROW(dog_pos[1][i]);
	                    break;
	            }
	            if (row>3)
	            {
	                material[dog_pos[side][i]]=material[dog_pos[side][i]]*197/200; // Advanced cat lose 1.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(dog_pos[side][i]);
	                    sprintf(message," is worth %d due to being an advanced dog.\n",material[dog_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            } else if (row==3)
	            {
	                material[dog_pos[side][i]]=material[dog_pos[side][i]]*199/200; // Slightly advanced cat lose 0.5 % of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(dog_pos[side][i]);
	                    sprintf(message," is worth %d due to being a slightly advanced dog.\n",material[dog_pos[side][i]]);
	                    BOARD_Message();
	                } */
	            }
	        }
	    }
	
	    for (square=11; square<=88; square++)
	    {    
	        if (square%10==9) square+=2;
	        if (bp.OWNER(square)==GOLD || bp.OWNER(square)==SILVER)
	        {
	            // Check if it's frozen, number of adjacent empty, strongest adjacent, and all that
	            piece_adjacent[0]=0;
	            piece_adjacent[1]=0;
	            piece_adjacent_empty=0;
	            piece_adjacent_strongest[0]=0;
	            piece_adjacent_strongest[1]=0;
	            for (dir=0; dir<4; dir++)
	            {
	                switch (bp.OWNER(square+direction[dir]))
	                {
	                    case GOLD :
	                        piece_adjacent[0]++;
	                        if (bp.PIECE(square+direction[dir])>piece_adjacent_strongest[0])
	                        {
	                            piece_adjacent_strongest[0]=bp.PIECE(square+direction[dir]);
	                        }
	                        break;
	                    case SILVER :
	                        piece_adjacent[1]++;
	                        if (bp.PIECE(square+direction[dir])>piece_adjacent_strongest[1])
	                        {
	                            piece_adjacent_strongest[1]=bp.PIECE(square+direction[dir]);
	                        }
	                        break;
	                    case EMPTY :
	                        piece_adjacent_empty++;
	                        break;
	                }
	            }
	            switch (bp.OWNER(square))
	            {
	                case GOLD :
	                    piece_adjacent_stronger_enemy=piece_adjacent_strongest[1]>bp.PIECE(square) ? 1 : 0; // used ternary to mimic c evaluation of conditions
	                    piece_frozen=(piece_adjacent_stronger_enemy == 1 && piece_adjacent[0]==0) ? 1 : 0;
	                    break;
	                case SILVER :
	                default		: // more initialization hacks
	                    piece_adjacent_stronger_enemy=piece_adjacent_strongest[0]>bp.PIECE(square) ? 1 : 0;
	                    piece_frozen=(piece_adjacent_stronger_enemy == 1 && piece_adjacent[1]==0) ? 1 : 0;
	                    break;
	            }
	            if (piece_frozen == 1)
	            {
	                material[square]=material[square]*9/10; // Frozen piece loses 10% of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(square);
	                    sprintf(message," is worth %d due to being frozen.\n",material[square]);
	                    BOARD_Message();
	                } */
	            }
	            if (piece_adjacent_empty==0) 
	            {
	                material[square]=material[square]*199/200; // Immobile piece loses 0.5% of its value
	                /* if (verbose)
	                {
	                    PRINT_SQUARE(square);
	                    sprintf(message," is worth %d due to being immobile.\n",material[square]);
	                    BOARD_Message();
	                } */
	            }
	            if ((piece_frozen == 1 || piece_adjacent_empty==0) && piece_adjacent_stronger_enemy == 1) // Our piece has limited mobility, and there is a stronger enemy piece adjacent
	            {
	                // Check if it's held hostage or threatened by a capture
	                if (adjacent_trap[square] != 0) // It's adjacent to a trap
	                {
	                    // If we have no other piece next to the trap, then consider this piece to be threatened, losing 30% of its value
	                    piece_adjacent_trap=0;
	                    for (dir=0; dir<4; dir++)
	                    {
	                        if (bp.OWNER(adjacent_trap[square]+direction[dir])==bp.OWNER(square))
	                        {
	                            piece_adjacent_trap++;
	                        }
	                    }
	                    if (piece_adjacent_trap==1)
	                    {
	                        material[square]=material[square]*7/10;
	                        /* if (verbose)
	                        {
	                            PRINT_SQUARE(square);
	                            sprintf(message," is worth %d due to being threatened (distance 1).\n",material[square]);
	                            BOARD_Message();
	                        } */
	                    }
	                }
	                if (adjacent2_trap[square] != 0 && bp.BOARD(adjacent2_trap[square])==EMPTY_SQUARE) 
	                // It's two steps away from an empty trap
	                {
	                    // If we have no piece next to the trap,
	                    // Really - should check so that there is a free path to trap.
	                    // then consider this piece to be threatened, losing 30% of its value
	                    piece_adjacent_trap=0;
	                    for (dir=0; dir<4; dir++)
	                    {
	                        if (bp.OWNER(adjacent2_trap[square]+direction[dir])==bp.OWNER(square))
	                        {
	                            piece_adjacent_trap++;
	                        }
	                    }
	                    if (piece_adjacent_trap==0)
	                    {
	                        material[square]=material[square]*7/10;
	                        /* if (verbose)
	                        {
	                            PRINT_SQUARE(square);
	                            sprintf(message," is worth %d due to being threatened (distance 2).\n",material[square]);
	                            BOARD_Message();
	                        } */
	                    }
	                }
	            }
	            // Another case - if adjacent to a trap, and no other friendly piece adjacent, various possibilities for being threatened....
	            switch (bp.OWNER(square))
	            {
	                case GOLD :
	                    material_value[0]+=material[square];
	                    break;
	                case SILVER :
	                    material_value[1]+=material[square];
	                    break;
	            }
	        }
	    }
	    
	    // Evaluate rabbits
	
	    for (i=0; i<rabbits[0]; i++)
	    {
	        row=bp.ROW(rabbit_pos[0][i]);
	        rabbit_value[0]+=(row-1)*(row-1)*(row-1);
	        if (row==7)
	        {
	            switch (bp.OWNER(rabbit_pos[0][i]+NORTH))
	            {
	                case EMPTY :
	                    rabbit_value[0]+=RABBIT_FREE_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to free space ahead\n",RABBIT_FREE_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	                case GOLD :
	                    rabbit_value[0]+=RABBIT_FRIENDLY_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to friendly piece ahead\n",RABBIT_FRIENDLY_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[0][i]+EAST))
	            {
	                case EMPTY :
	                    rabbit_value[0]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to free space to the east\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case GOLD :
	                    rabbit_value[0]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to friendly piece to the east\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[0][i]+WEST))
	            {
	                case EMPTY :
	                    rabbit_value[0]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to free space to the west\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case GOLD :
	                    rabbit_value[0]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[0][i]);
	                        sprintf(message," : gold rabbit value increased by %d due to friendly piece to the west\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	        }
	    }
	    for (i=0; i<rabbits[1]; i++)
	    {
	        row=9-bp.ROW(rabbit_pos[1][i]);
	        rabbit_value[1]+=(row-1)*(row-1);
	        if (row==7)
	        {
	            switch (bp.OWNER(rabbit_pos[1][i]+SOUTH))
	            {
	                case EMPTY :
	                    rabbit_value[1]+=RABBIT_FREE_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to free space ahead\n",RABBIT_FREE_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	                case SILVER :
	                    rabbit_value[1]+=RABBIT_FRIENDLY_AHEAD;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to friendly piece ahead\n",RABBIT_FRIENDLY_AHEAD);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[1][i]+EAST))
	            {
	                case EMPTY :
	                    rabbit_value[1]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to free space to the east\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case SILVER :
	                    rabbit_value[1]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to friendly piece to the east\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	            switch (bp.OWNER(rabbit_pos[1][i]+WEST))
	            {
	                case EMPTY :
	                    rabbit_value[1]+=RABBIT_FREE_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to free space to the west\n",RABBIT_FREE_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	                case SILVER :
	                    rabbit_value[1]+=RABBIT_FRIENDLY_SIDE;
	                    /* if (verbose)
	                    {
	                        PRINT_SQUARE(rabbit_pos[1][i]);
	                        sprintf(message," : silver rabbit value increased by %d due to friendly piece to the west\n",RABBIT_FRIENDLY_SIDE);
	                        BOARD_Message();
	                    } */
	                    break;
	            }
	        }
	    }
	    
	    // Add up all the factors
	    int materialValue = material_value[0] - material_value[1];
	    int trapValue = trap_value[0] - trap_value[1];
	    int rabbitValue = rabbit_value[0] - rabbit_value[1];
	    
	    int[] toPopulate = new int[3];
	    toPopulate[0] = materialValue;
	    toPopulate[1] = trapValue;
	    toPopulate[2] = rabbitValue;
	    
	//    if (bp.getAtMove()==SILVER)
	//    {
	//        
	//    }
	    
	    return toPopulate;
	}
}





/** REFERENCE 1 -- FairyBoard is our Java equivalent of this struct **

typedef struct
{
    unsigned char board[100]; 
        *****
        11 - 88 = actual board, edges around for easier move generation and evaluation
        
        11 12 ... 17 18    a8 b8 ... g8 h8
        21 22 ... 27 28    a7 b7 ... g7 h7
        ............... == ...............
        71 72 ... 77 78    a2 b2 ... g2 h2
        81 82 ... 87 88    a1 b1 ... g1 h1
                
        directions:
            -10 : North (up, towards silver)
            +10 : South (down, towards gold)
            +1 : East (right from gold's view, left from silver's view)
            -1 : West (left from gold's view, right from silver's view)
            
        highest bit - is square off the board?
            (board[x]&0x80U)==0 : square is on board.
            (board[x]&0x80U)==0x80U : square is off the board.
        second, third bit - currently unused.
        fourth, fifth bit - who owns the piece?
            (board[x] & OWNER_MASK)==OFF_BOARD : square is off the board - both bits are set
            (board[x] & OWNER_MASK)==GOLD : gold piece - first bit is set
            (board[x] & OWNER_MASK)==SILVER : silver piece - second bit is set
            (board[x] & OWNER_MASK)==EMPTY : empty square - none of the bits are set
        remaining three bits - which kind of piece is it?
            (board[x]&0x7U) gives which piece it is.
                (board[x] & PIECE_MASK)==6 : Elephant
                (board[x] & PIECE_MASK)==5 : Camel
                (board[x] & PIECE_MASK)==4 : Horse
                (board[x] & PIECE_MASK)==3 : Dog
                (board[x] & PIECE_MASK)==2 : Cat
                (board[x] & PIECE_MASK)==1 : Rabbit
            Special cases:
                (board[x] & PIECE_MASK)==0 : Empty
                (board[x] & PIECE_MASK)==7 : Off the board
        *****
    unsigned char at_move; // Who is at move?
    unsigned char steps; // How many steps have the side at move done so far?
    int move; // How many moves have been done in the game so far?  0 at start, 2 after setup... even means gold is at move, odd means silver is at move.  Divide by 2 and add 1 to get official move number.
    unsigned long long int hashkey; // 64-bit hashkey, used for index into hash table, and for collision / repetition detection
} board_t;

*/


/** REFERENCE 2 -- The C code which runs the C evaluation function and outputs scores **
  *  			-- I have commented the code to the extent it made sense to make sense
  *  			   of botFairy's code

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "board.h"
#include "search.h"
#include "hash.h"
#include "eval.h"


static const char *TESTS_DOCUMENT = "tests.txt";
static const char *OUTPUT_DOCUMENT = "test_results_navv_C.txt";

// copied over and modified runtest.c's main function
int main() // returns 1 if an error occurs, 0 otherwise
{
	int error_code = 0;
	board_t position;
	move_t moves[4];
	int i, j;
	int steps = 0;
	FILE *fp, *outputFP;
	char line[100];
	int number_of_tests = 0;

	BOARD_Message_Init();
	{
		BOARD_Init(&position);
		
		fp = fopen(TESTS_DOCUMENT, "r");
		outputFP = fopen(OUTPUT_DOCUMENT, "w");
		if (fp == NULL || outputFP == NULL) {
			error_code = 1;
		}
		else {
			fgets(line, 100, fp);
			while (line[0] == '#') {
				fgets(line, 100, fp);
			}

			// at this point, the line buffer contains the number of tests to expect in the file (first line of file)
			// note that each test is its own text file--an ASCII board

			// hand-rolled reading of an integer... why not strtoul?
			for (i = 0; line[i] >= '0' && line[i] <= '9'; i++) {
				number_of_tests = number_of_tests * 10 + line[i] - '0';
			}

			// looping over each board/text file
			for (i = 0; !error_code && i<number_of_tests; i++)
			{
				fgets(line, 100, fp);
				while (line[0] == '#')
				{
					fgets(line, 100, fp);
				}

				// now, line contains the name of the file, terminated by a newline (\n)

				for (j = 0; line[j] != '\n'; j++)
				{
				}
				line[j] = '\0';
				sprintf(message, "Reading position \"%s\" from file.\n", line);
				BOARD_Message();

				// populate the board from the ASCII representation at the given filename...
				if (BOARD_Read_Position(&position, line))
				{
					sprintf(message, "Couldn't read position from file.\n");
					BOARD_Message();
					error_code = 1;
				}
				else
				{
					position.steps = 0;
					fprintf(outputFP, "File %s: %d\n", line, EVAL_Eval(&position, TRUE));

					// read a line of the tests.txt file we're not using (some limit for the search)
					// in addition to any comments. NOTE this is done after fprintf so filename is preserved
					fgets(line, 100, fp);
					while (line[0] == '#')
					{
						fgets(line, 100, fp);
					}
				}
			}
			fclose(fp);
			fclose(outputFP);
		}
	}
	BOARD_Message_Exit();
	return 0;
}
*/
