package montecarlo;

import java.util.Arrays;

import feature_extractor.FeatureConstants;
import ai_util.Util;
import arimaa3.Constants;
import arimaa3.GameState;




public class FairyEvaluation {

	private static final int MAX_NUMBER_MOVES = 100;
	
	// constants used for the board
	
	private static final int EMPTY_SQUARE = 0x0;
	private static final int EMPTY = 0x0;
	private static final int OFF_BOARD_SQUARE = 0x9F; 
	private static final int OFF_BOARD = 0x18;
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
	private static final int FLIP_SIDE = GOLD^SILVER;
	private static final int TRUE = 1;
	private static final int FALSE = 0;
	private static final int NORTH = -10;
	private static final int SOUTH = 10;
	private static final int EAST = 1;
	private static final int WEST = -1;
	
	
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
	
	// macros for board manipulation
	
//	private int OWNER(int square) { return (bp->board[square] & OWNER_MASK); } // who owns a (piece on a) square?
//	private int PIECE(int square) { return (bp->board[square] & PIECE_MASK); } // what piece is on a square?
//	private int BOARD(int square) { return (bp->board[square]); } // What is on a square?  Returns the owner | piece combination.
//	private int ROW(int square) { return (9-square/10); } // what row is a square in?  1 = bottom, 8 = top
//	private int COL(int square) { return (square%10); } // what column is a square in?  1 = left (a), 8 = right (h)
//	private int PRINT_SQUARE(int square) { return sprintf(message,"%c%c",COL(square)-1+'a',ROW(square)-1+'1'); BOARD_Message(); } //
	
	public class FairyBoard implements FeatureConstants {
		
		//private state variables;
		private char[] bp;
		char at_move; // Who is at move? // character mask for GOLD or SILVER?
		char steps; // How many steps have the side at move done so far?
		int move; // How many moves have been done in the game so far?  0 at start, 2 after setup... even means gold is at move, odd means silver is at move.  Divide by 2 and add 1 to get official move number.
		long hashkey; // 64-bit hashkey, used for index into hash table, and for collision / repetition detection
		
		/**
		 * Converts a GameState to the Fairy board representation.
		 * @param state to be converted
		 */
		public FairyBoard(GameState state) {
			bp = new char[100];
			convertBoard(state);
			
			at_move = (char) ((state.getSideToMove() == PL_WHITE) ? GOLD : SILVER);
			steps = (char) (NUM_STEPS_IN_MOVE - state.getStepsRemaining()); // assumes 4 steps per move
			move = state.getTurnNumber() * 2; // assumes 4 steps per move (sadly...)
			hashkey = state.getPositionHash();
		}
		
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
		
		private void initializeEmptyBoard(){
			// Initialize all squares to be off the board at first
			for (int i = 0; i < 100; i++){
		        bp[i] = OFF_BOARD_SQUARE;
		    }
			
			// All squares actually on the board are empty
		    for (int i = 1; i <= 8; i++) {
		        for (int j = 1; j <= 8; j++) {
		            bp[10 * i + j] = EMPTY_SQUARE;
		        }
		    }			
		}
		
		/**
		 * Puts a piece on bp at a particular square. (This method handles conversions from Bacher's conventions.)
		 * @param index64 the index into the long piece bitboard (0-63). [<i>8 * row + col, where (row = 0, col = 0) is a1</i>]
		 * @param bacherPieceType the values specified in arimaa3.Constants.java (0-11)
		 */
		private void updateBoardWithPiece(int index64, int bacherPieceType) {
			// convert index64 to bp's index
			int row64 = index64 >>> 3; //index64 / 8;
			int col64 = index64 & 0x7; //index64 % 8;
			
			int flippedRow64 = (8 - 1) - row64; // 0th row becomes top row, rather than bottom
			
			int index = (flippedRow64 + 1) * 10 + (col64 + 1);
						
			// convert bacherPieceType to owner and piece type
			boolean isGold = bacherPieceType % 2 == PL_WHITE;
			int ownerBits = isGold ? GOLD : SILVER;
			int pieceType = (bacherPieceType / 2) + 1;
			
			// update bp
			bp[index] &= ~0x1F; //clear garbage from lower 5 bits
			bp[index] |= ownerBits; 
			bp[index] |= pieceType; // update with pieceType bits
		}

		/**
		 * @return the bp
		 */
		public char[] getBp() {
			return Arrays.copyOf(bp, bp.length);
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
		
		private char getPieceText(int bpIndex) {
			// TODO: implement for reals
			return 'a';
		}

		public String toBoardString() {
			// TODO Fix move number and TS
			StringBuilder sb = new StringBuilder();

			sb.append(getMove());
			sb.append(getAtMove() == GOLD ? 'w' : 'b');
			
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

		    sb.append("TS: "); sb.append(getMove() * 8 /*total_steps*/); sb.append("\n");
			
			return sb.toString();
		}

	}

	public static double evaluate(GameState state){
		return 0.0;
	}
	
}

//#ifndef BOARDH_DEFINED // Don't include this twice
//
//#define BOARDH_DEFINED
//
//// private static final int TESTING = ;// define this to get a version suitable to run automatic tests with.
//

//
//typedef struct
//{
//    unsigned char board[100]; 
//        /*****
//        11 - 88 = actual board, edges around for easier move generation and evaluation
//        
//        11 12 ... 17 18    a8 b8 ... g8 h8
//        21 22 ... 27 28    a7 b7 ... g7 h7
//        ............... == ...............
//        71 72 ... 77 78    a2 b2 ... g2 h2
//        81 82 ... 87 88    a1 b1 ... g1 h1
//                
//        directions:
//            -10 : North (up, towards silver)
//            +10 : South (down, towards gold)
//            +1 : East (right from gold's view, left from silver's view)
//            -1 : West (left from gold's view, right from silver's view)
//            
//        highest bit - is square off the board?
//            (board[x]&0x80U)==0 : square is on board.
//            (board[x]&0x80U)==0x80U : square is off the board.
//        second, third bit - currently unused.
//        fourth, fifth bit - who owns the piece?
//            (board[x] & OWNER_MASK)==OFF_BOARD : square is off the board - both bits are set
//            (board[x] & OWNER_MASK)==GOLD : gold piece - first bit is set
//            (board[x] & OWNER_MASK)==SILVER : silver piece - second bit is set
//            (board[x] & OWNER_MASK)==EMPTY : empty square - none of the bits are set
//        remaining three bits - which kind of piece is it?
//            (board[x]&0x7U) gives which piece it is.
//                (board[x] & PIECE_MASK)==6 : Elephant
//                (board[x] & PIECE_MASK)==5 : Camel
//                (board[x] & PIECE_MASK)==4 : Horse
//                (board[x] & PIECE_MASK)==3 : Dog
//                (board[x] & PIECE_MASK)==2 : Cat
//                (board[x] & PIECE_MASK)==1 : Rabbit
//            Special cases:
//                (board[x] & PIECE_MASK)==0 : Empty
//                (board[x] & PIECE_MASK)==7 : Off the board
//        *****/
//    unsigned char at_move; // Who is at move?
//    unsigned char steps; // How many steps have the side at move done so far?
//    int move; // How many moves have been done in the game so far?  0 at start, 2 after setup... even means gold is at move, odd means silver is at move.  Divide by 2 and add 1 to get official move number.
//    unsigned long long int hashkey; // 64-bit hashkey, used for index into hash table, and for collision / repetition detection
//} board_t;
//
//
//extern const int direction[4];
//extern const int trap[4];
//extern char message[1000];
//
//void BOARD_Init(board_t *bp);
//void BOARD_Calculate_Hashkey(board_t *bp);
//void BOARD_Copy_Move(move_t *mpfrom, move_t *mpto);
//void BOARD_Do_Move(board_t *bp, move_t *mp);
//void BOARD_Undo_Move(board_t *bp, move_t *mp);
//int BOARD_Read_Position(board_t *bp, char *file_name);
//void BOARD_Print_Position(board_t *bp);
//int BOARD_Generate_Moves(board_t *bp, move_t ml[MAX_NUMBER_MOVES]);
//void BOARD_Print_Move(move_t *mp);
//void BOARD_Send_Move(move_t *mp);
//void BOARD_Message_Init(void);
//void BOARD_Message(void);
//void BOARD_Message_Exit(void);
//
//
//private static final int ELEPHANT_VALUE = 20000;
//private static final int CAMEL_VALUE = 5000;
//private static final int HORSE_VALUE = 3000;
//private static final int DOG_VALUE = 1800;
//private static final int CAT_VALUE = 1500;
//private static final int RABBIT_VALUE = 1000;
//
//private static final int RABBIT_FREE_AHEAD = 1000;
//private static final int RABBIT_FRIENDLY_AHEAD = 500;
//private static final int RABBIT_FREE_SIDE = 300;
//private static final int RABBIT_FRIENDLY_SIDE = 200;
//
//// board constants
//static const int trap_square[4]={33,36,63,66};
////static const int direction[4]={NORTH,EAST,SOUTH,WEST};
//
//static const int adjacent_trap[100]={0,0,0,0,0,0,0,0,0,0,0,
//
//                                      0, 0, 0, 0, 0, 0, 0, 0,     0,0,                               
//                                      0, 0,33, 0, 0,36, 0, 0,     0,0,                               
//                                      0,33, 0,33,36, 0,36, 0,     0,0,                               
//                                      0, 0,33, 0, 0,36, 0, 0,     0,0,                               
//                                      0, 0,63, 0, 0,66, 0, 0,     0,0,                               
//                                      0,63, 0,63,66, 0,66, 0,     0,0,                               
//                                      0, 0,63, 0, 0,66, 0, 0,     0,0,                               
//                                      0, 0, 0, 0, 0, 0, 0, 0,     0,0,                               
//
//                                     0,0,0,0,0,0,0,0,0};
//
//static const int adjacent2_trap[100]={0,0,0,0,0,0,0,0,0,0,0,
//
//                                       0, 0,33, 0, 0,36, 0, 0,     0,0,                               
//                                       0,33, 0,33,36, 0,36, 0,     0,0,                               
//                                      33, 0, 0,36,33, 0, 0,36,     0,0,                               
//                                       0,33,63,33,36,66,36, 0,     0,0,                               
//                                       0,63,33,63,66,36,66, 0,     0,0,                               
//                                      63, 0, 0,66,63, 0, 0,66,     0,0,                               
//                                       0,63, 0,63,66, 0,66, 0,     0,0,                               
//                                       0, 0,63, 0, 0,66, 0, 0,     0,0,                               
//
//                                      0,0,0,0,0,0,0,0,0};
//                                                                    
//int EVAL_Eval(board_t *bp, int verbose)
//
//// Evaluation is done from gold's perspective.  At the end of the evaluation, it's adjusted to be seen from current player's perspective.
//
//{
//    // evaluation constants
//    static const int piece_value[7]={0,RABBIT_VALUE,CAT_VALUE,DOG_VALUE,HORSE_VALUE,CAMEL_VALUE,ELEPHANT_VALUE};
//    // variables        
//    
//    // utility variables
//    int side_mask[OWNER_MASK];
//     
//    // loop variables
//    int square; 
//    int side;
//    int trap;
//    int dir;
//    int i;
//            
//    // value variables
//    int value=0;
//    int material_value[2]={0,0};
//    int trap_value[2]={0,0};
//    int rabbit_value[2]={0,0};
//            
//    // how many of the pieces do the players have, and where are they?
//    int elephants[2]={0,0};
//    int elephant_pos[2][1];
//    int camels[2]={0,0};
//    int camel_pos[2][1];
//    int horses[2]={0,0};
//    int horse_pos[2][2];
//    int dogs[2]={0,0};
//    int dog_pos[2][2];
//    int cats[2]={0,0};
//    int cat_pos[2][2];
//    int rabbits[2]={0,0};
//    int rabbit_pos[2][8];
//    
//    // trap evaluation variables
//    int trap_adjacent[2];
//    int trap_adjacent_strength[2];
//    int trap_adjacent_strongest[2];
//                                     
//    // material evaluation variables
//    int material[100]; // What is the piece on this square worth?
//    int piece_frozen;
//    int piece_adjacent_stronger_enemy;
//    int piece_adjacent_empty;
//    int piece_adjacent_strongest[2];
//    int piece_adjacent[2];
//    int piece_adjacent_trap;
//    
//    // rabbit evaluation variables
//    int row;
//    
//    // Initialize some evaluation stuff
//
//    side_mask[GOLD]=0;
//    side_mask[SILVER]=1;
//
//    // Determine extra information about the board state
//
//    for (square=11; square<=88; square++) // loop over board, initialize board state info and find where all the pieces are.
//    {
//        if (square%10==9) square+=2;
//        switch (PIECE(square))
//        {
//            case ELEPHANT_PIECE :
//                elephant_pos[side_mask[OWNER(square)]][elephants[side_mask[OWNER(square)]]]=square;
//                elephants[side_mask[OWNER(square)]]++;
//                break;
//            case CAMEL_PIECE :
//                camel_pos[side_mask[OWNER(square)]][camels[side_mask[OWNER(square)]]]=square;
//                camels[side_mask[OWNER(square)]]++;
//                break;
//            case HORSE_PIECE :
//                horse_pos[side_mask[OWNER(square)]][horses[side_mask[OWNER(square)]]]=square;
//                horses[side_mask[OWNER(square)]]++;
//                break;
//            case DOG_PIECE :
//                dog_pos[side_mask[OWNER(square)]][dogs[side_mask[OWNER(square)]]]=square;
//                dogs[side_mask[OWNER(square)]]++;
//                break;
//            case CAT_PIECE :
//                cat_pos[side_mask[OWNER(square)]][cats[side_mask[OWNER(square)]]]=square;
//                cats[side_mask[OWNER(square)]]++;
//                break;
//            case RABBIT_PIECE :
//                rabbit_pos[side_mask[OWNER(square)]][rabbits[side_mask[OWNER(square)]]]=square;
//                rabbits[side_mask[OWNER(square)]]++;
//                break;
//        }
//        if (OWNER(square)==GOLD || OWNER(square)==SILVER)
//        {
//            material[square]=piece_value[PIECE(square)];
//        } else
//        {
//            material[square]=0;
//        }
//    }
//    
//    // Evaluate trap squares, decide trap ownership.
//
//    if (verbose)
//    {
//        sprintf(message,"Evaluating traps:\n");
//        BOARD_Message();
//    }       
//    for (trap=0; trap<4; trap++)
//    {
//        for (side=0; side<2; side++)
//        {
//            trap_adjacent[side]=0;
//            trap_adjacent_strength[side]=0;
//            trap_adjacent_strongest[side]=0;
//        }
//        for (dir=0; dir<4; dir++)
//        {
//            switch (OWNER(trap_square[trap]+direction[dir]))
//            {
//                case GOLD :
//                    trap_adjacent[0]++;
//                    trap_adjacent_strength[0]+=PIECE(trap_square[trap]+direction[dir]);
//                    if (PIECE(trap_square[trap]+direction[dir])>trap_adjacent_strongest[0])
//                    {
//                        trap_adjacent_strongest[0]=PIECE(trap_square[trap]+direction[dir]);
//                    }
//                    break;
//                case SILVER :
//                    trap_adjacent[1]++;
//                    trap_adjacent_strength[1]+=PIECE(trap_square[trap]+direction[dir]);
//                    if (PIECE(trap_square[trap]+direction[dir])>trap_adjacent_strongest[1])
//                    {
//                        trap_adjacent_strongest[1]=PIECE(trap_square[trap]+direction[dir]);
//                    }
//                    break;
//            }
//        }
//        // Basically, 200 points are given out per trap.  50 to whoever has the strongest piece by the trap, 
//        // and 150 points split according to total strength of pieces, with two neutral strength added.
//        
//        // case 1 - only one side has pieces by the trap.
//        if (trap_adjacent[0]>0 && trap_adjacent[1]==0) 
//        {
//            trap_value[0]+=50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+1);
//            if (verbose)
//            {
//                PRINT_SQUARE(trap_square[trap]);
//                sprintf(message," is worth Gold (%d) - Silver (%d).\n",50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+1),0);
//                BOARD_Message();
//            }
//        }
//        if (trap_adjacent[1]>0 && trap_adjacent[0]==0)
//        {
//            trap_value[1]+=50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[1]+1);
//            if (verbose)
//            {
//                PRINT_SQUARE(trap_square[trap]);
//                sprintf(message," is worth Gold (%d) - Silver (%d).\n",0,50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[1]+1));
//                BOARD_Message();
//            }
//        }
//        // case 2 - both sides have pieces by the trap.
//        if (trap_adjacent[0]>0 && trap_adjacent[1]>0)
//        {
//            // subcase 1 - they are equally strong.  Split 100 points according to number of pieces.
//            if (trap_adjacent_strongest[0]==trap_adjacent_strongest[1])
//            {
//                trap_value[0]+=trap_adjacent_strength[0]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
//                trap_value[1]+=trap_adjacent_strength[1]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
//                if (verbose)
//                {
//                    PRINT_SQUARE(trap_square[trap]);
//                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",trap_adjacent_strength[0]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),trap_adjacent_strength[1]*200/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
//                    BOARD_Message();
//                }
//            }
//            // subcase 2 - gold is stronger.  Give 50 points to gold, and split 50 according to number of pieces.
//            if (trap_adjacent_strongest[0]>trap_adjacent_strongest[1])
//            {
//                trap_value[0]+=50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
//                trap_value[1]+=trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
//                if (verbose)
//                {
//                    PRINT_SQUARE(trap_square[trap]);
//                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",50+trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
//                    BOARD_Message();
//                }
//            }
//            // subcase 3 - silver is stronger.  Give 50 points to silver, and split 50 according to number of pieces.
//            if (trap_adjacent_strongest[1]>trap_adjacent_strongest[0])
//            {
//                trap_value[0]+=trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
//                trap_value[1]+=50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1);
//                if (verbose)
//                {
//                    PRINT_SQUARE(trap_square[trap]);
//                    sprintf(message," is worth Gold (%d) - Silver (%d).\n",trap_adjacent_strength[0]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1),50+trap_adjacent_strength[1]*150/(trap_adjacent_strength[0]+trap_adjacent_strength[1]+1));
//                    BOARD_Message();
//                }
//            }
//        }
//        // special case - give minus for (possible) frames
//        if (OWNER(trap_square[trap])==GOLD && trap_adjacent[1]>2)
//        {
//            material[trap_square[trap]]=material[trap_square[trap]]*4/5; // Trapped piece loses 20% of its value
//            if (verbose)
//            {
//                sprintf(message,"Piece at ");
//                BOARD_Message();
//                PRINT_SQUARE(trap_square[trap]);
//                sprintf(message," is worth %d due to being (possibly) framed.\n",material[trap_square[trap]]);
//                BOARD_Message();
//            }
//        }
//        if (OWNER(trap_square[trap])==SILVER && trap_adjacent[0]>2)
//        {
//            material[trap_square[trap]]=material[trap_square[trap]]*4/5; // Trapped piece loses 20% of its value
//            if (verbose)
//            {
//                sprintf(message,"Piece at ");
//                BOARD_Message();
//                PRINT_SQUARE(trap_square[trap]);
//                sprintf(message," is worth %d due to being (possibly) framed.\n",material[trap_square[trap]]);
//                BOARD_Message();
//            }
//        }
//    }
//    
//    // Evaluate material and individual pieces.
//
//    for (side=0; side<2; side++)
//    {
//        for (i=0; i<cats[side]; i++)
//        {
//            switch (side)
//            {
//                case 0 : 
//                    row=ROW(cat_pos[0][i]);
//                    break;
//                case 1 :
//                    row=9-ROW(cat_pos[1][i]);
//                    break;
//            }
//            if (row>3)
//            {
//                material[cat_pos[side][i]]=material[cat_pos[side][i]]*197/200; // Advanced cat lose 1.5 % of its value
//                if (verbose)
//                {
//                    PRINT_SQUARE(cat_pos[side][i]);
//                    sprintf(message," is worth %d due to being an advanced cat.\n",material[cat_pos[side][i]]);
//                    BOARD_Message();
//                }
//            } else if (row==3)
//            {
//                material[cat_pos[side][i]]=material[cat_pos[side][i]]*199/200; // Slightly advanced cat lose 0.5 % of its value
//                if (verbose)
//                {
//                    PRINT_SQUARE(cat_pos[side][i]);
//                    sprintf(message," is worth %d due to being a slightly advanced cat.\n",material[cat_pos[side][i]]);
//                    BOARD_Message();
//                }
//            }
//        }
//    
//        for (i=0; i<dogs[side]; i++)
//        {
//            switch (side)
//            {
//                case 0 : 
//                    row=ROW(dog_pos[0][i]);
//                    break;
//                case 1 :
//                    row=9-ROW(dog_pos[1][i]);
//                    break;
//            }
//            if (row>3)
//            {
//                material[dog_pos[side][i]]=material[dog_pos[side][i]]*197/200; // Advanced cat lose 1.5 % of its value
//                if (verbose)
//                {
//                    PRINT_SQUARE(dog_pos[side][i]);
//                    sprintf(message," is worth %d due to being an advanced dog.\n",material[dog_pos[side][i]]);
//                    BOARD_Message();
//                }
//            } else if (row==3)
//            {
//                material[dog_pos[side][i]]=material[dog_pos[side][i]]*199/200; // Slightly advanced cat lose 0.5 % of its value
//                if (verbose)
//                {
//                    PRINT_SQUARE(dog_pos[side][i]);
//                    sprintf(message," is worth %d due to being a slightly advanced dog.\n",material[dog_pos[side][i]]);
//                    BOARD_Message();
//                }
//            }
//        }
//    }
//
//    for (square=11; square<=88; square++)
//    {    
//        if (square%10==9) square+=2;
//        if (OWNER(square)==GOLD || OWNER(square)==SILVER)
//        {
//            // Check if it's frozen, number of adjacent empty, strongest adjacent, and all that
//            piece_adjacent[0]=0;
//            piece_adjacent[1]=0;
//            piece_adjacent_empty=0;
//            piece_adjacent_strongest[0]=0;
//            piece_adjacent_strongest[1]=0;
//            for (dir=0; dir<4; dir++)
//            {
//                switch (OWNER(square+direction[dir]))
//                {
//                    case GOLD :
//                        piece_adjacent[0]++;
//                        if (PIECE(square+direction[dir])>piece_adjacent_strongest[0])
//                        {
//                            piece_adjacent_strongest[0]=PIECE(square+direction[dir]);
//                        }
//                        break;
//                    case SILVER :
//                        piece_adjacent[1]++;
//                        if (PIECE(square+direction[dir])>piece_adjacent_strongest[1])
//                        {
//                            piece_adjacent_strongest[1]=PIECE(square+direction[dir]);
//                        }
//                        break;
//                    case EMPTY :
//                        piece_adjacent_empty++;
//                        break;
//                }
//            }
//            switch (OWNER(square))
//            {
//                case GOLD :
//                    piece_adjacent_stronger_enemy=piece_adjacent_strongest[1]>PIECE(square);
//                    piece_frozen=piece_adjacent_stronger_enemy && piece_adjacent[0]==0;
//                    break;
//                case SILVER :
//                    piece_adjacent_stronger_enemy=piece_adjacent_strongest[0]>PIECE(square);
//                    piece_frozen=piece_adjacent_stronger_enemy && piece_adjacent[1]==0;
//                    break;
//            }
//            if (piece_frozen)
//            {
//                material[square]=material[square]*9/10; // Frozen piece loses 10% of its value
//                if (verbose)
//                {
//                    PRINT_SQUARE(square);
//                    sprintf(message," is worth %d due to being frozen.\n",material[square]);
//                    BOARD_Message();
//                }
//            }
//            if (piece_adjacent_empty==0) 
//            {
//                material[square]=material[square]*199/200; // Immobile piece loses 0.5% of its value
//                if (verbose)
//                {
//                    PRINT_SQUARE(square);
//                    sprintf(message," is worth %d due to being immobile.\n",material[square]);
//                    BOARD_Message();
//                }
//            }
//            if ((piece_frozen || piece_adjacent_empty==0) && piece_adjacent_stronger_enemy) // Our piece has limited mobility, and there is a stronger enemy piece adjacent
//            {
//                // Check if it's held hostage or threatened by a capture
//                if (adjacent_trap[square]) // It's adjacent to a trap
//                {
//                    // If we have no other piece next to the trap, then consider this piece to be threatened, losing 30% of its value
//                    piece_adjacent_trap=0;
//                    for (dir=0; dir<4; dir++)
//                    {
//                        if (OWNER(adjacent_trap[square]+direction[dir])==OWNER(square))
//                        {
//                            piece_adjacent_trap++;
//                        }
//                    }
//                    if (piece_adjacent_trap==1)
//                    {
//                        material[square]=material[square]*7/10;
//                        if (verbose)
//                        {
//                            PRINT_SQUARE(square);
//                            sprintf(message," is worth %d due to being threatened (distance 1).\n",material[square]);
//                            BOARD_Message();
//                        }
//                    }
//                }
//                if (adjacent2_trap[square] && BOARD(adjacent2_trap[square])==EMPTY_SQUARE) 
//                // It's two steps away from an empty trap
//                {
//                    // If we have no piece next to the trap,
//                    // Really - should check so that there is a free path to trap.
//                    // then consider this piece to be threatened, losing 30% of its value
//                    piece_adjacent_trap=0;
//                    for (dir=0; dir<4; dir++)
//                    {
//                        if (OWNER(adjacent2_trap[square]+direction[dir])==OWNER(square))
//                        {
//                            piece_adjacent_trap++;
//                        }
//                    }
//                    if (piece_adjacent_trap==0)
//                    {
//                        material[square]=material[square]*7/10;
//                        if (verbose)
//                        {
//                            PRINT_SQUARE(square);
//                            sprintf(message," is worth %d due to being threatened (distance 2).\n",material[square]);
//                            BOARD_Message();
//                        }
//                    }
//                }
//            }
//            // Another case - if adjacent to a trap, and no other friendly piece adjacent, various possibilities for being threatened....
//            switch (OWNER(square))
//            {
//                case GOLD :
//                    material_value[0]+=material[square];
//                    break;
//                case SILVER :
//                    material_value[1]+=material[square];
//                    break;
//            }
//        }
//    }
//    
//    // Evaluate rabbits
//
//    for (i=0; i<rabbits[0]; i++)
//    {
//        row=ROW(rabbit_pos[0][i]);
//        rabbit_value[0]+=(row-1)*(row-1)*(row-1);
//        if (row==7)
//        {
//            switch (OWNER(rabbit_pos[0][i]+NORTH))
//            {
//                case EMPTY :
//                    rabbit_value[0]+=RABBIT_FREE_AHEAD;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[0][i]);
//                        sprintf(message," : gold rabbit value increased by %d due to free space ahead\n",RABBIT_FREE_AHEAD);
//                        BOARD_Message();
//                    }
//                    break;
//                case GOLD :
//                    rabbit_value[0]+=RABBIT_FRIENDLY_AHEAD;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[0][i]);
//                        sprintf(message," : gold rabbit value increased by %d due to friendly piece ahead\n",RABBIT_FRIENDLY_AHEAD);
//                        BOARD_Message();
//                    }
//                    break;
//            }
//            switch (OWNER(rabbit_pos[0][i]+EAST))
//            {
//                case EMPTY :
//                    rabbit_value[0]+=RABBIT_FREE_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[0][i]);
//                        sprintf(message," : gold rabbit value increased by %d due to free space to the east\n",RABBIT_FREE_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//                case GOLD :
//                    rabbit_value[0]+=RABBIT_FRIENDLY_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[0][i]);
//                        sprintf(message," : gold rabbit value increased by %d due to friendly piece to the east\n",RABBIT_FRIENDLY_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//            }
//            switch (OWNER(rabbit_pos[0][i]+WEST))
//            {
//                case EMPTY :
//                    rabbit_value[0]+=RABBIT_FREE_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[0][i]);
//                        sprintf(message," : gold rabbit value increased by %d due to free space to the west\n",RABBIT_FREE_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//                case GOLD :
//                    rabbit_value[0]+=RABBIT_FRIENDLY_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[0][i]);
//                        sprintf(message," : gold rabbit value increased by %d due to friendly piece to the west\n",RABBIT_FRIENDLY_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//            }
//        }
//    }
//    for (i=0; i<rabbits[1]; i++)
//    {
//        row=9-ROW(rabbit_pos[1][i]);
//        rabbit_value[1]+=(row-1)*(row-1);
//        if (row==7)
//        {
//            switch (OWNER(rabbit_pos[1][i]+SOUTH))
//            {
//                case EMPTY :
//                    rabbit_value[1]+=RABBIT_FREE_AHEAD;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[1][i]);
//                        sprintf(message," : silver rabbit value increased by %d due to free space ahead\n",RABBIT_FREE_AHEAD);
//                        BOARD_Message();
//                    }
//                    break;
//                case SILVER :
//                    rabbit_value[1]+=RABBIT_FRIENDLY_AHEAD;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[1][i]);
//                        sprintf(message," : silver rabbit value increased by %d due to friendly piece ahead\n",RABBIT_FRIENDLY_AHEAD);
//                        BOARD_Message();
//                    }
//                    break;
//            }
//            switch (OWNER(rabbit_pos[1][i]+EAST))
//            {
//                case EMPTY :
//                    rabbit_value[1]+=RABBIT_FREE_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[1][i]);
//                        sprintf(message," : silver rabbit value increased by %d due to free space to the east\n",RABBIT_FREE_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//                case SILVER :
//                    rabbit_value[1]+=RABBIT_FRIENDLY_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[1][i]);
//                        sprintf(message," : silver rabbit value increased by %d due to friendly piece to the east\n",RABBIT_FRIENDLY_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//            }
//            switch (OWNER(rabbit_pos[1][i]+WEST))
//            {
//                case EMPTY :
//                    rabbit_value[1]+=RABBIT_FREE_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[1][i]);
//                        sprintf(message," : silver rabbit value increased by %d due to free space to the west\n",RABBIT_FREE_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//                case SILVER :
//                    rabbit_value[1]+=RABBIT_FRIENDLY_SIDE;
//                    if (verbose)
//                    {
//                        PRINT_SQUARE(rabbit_pos[1][i]);
//                        sprintf(message," : silver rabbit value increased by %d due to friendly piece to the west\n",RABBIT_FRIENDLY_SIDE);
//                        BOARD_Message();
//                    }
//                    break;
//            }
//        }
//    }
//    
//    // Add up all the factors
//           
//    value+=material_value[0]-material_value[1];
//    value+=trap_value[0]-trap_value[1];
//    value+=rabbit_value[0]-rabbit_value[1];
//
//    if (verbose)
//    {
//        sprintf(message,"Material value Gold (%d) - Silver (%d)\n",material_value[0],material_value[1]);
//        BOARD_Message();
//        sprintf(message,"Trap value Gold (%d) - Silver (%d)\n",trap_value[0],trap_value[1]);
//        BOARD_Message();
//        sprintf(message,"Rabbit value Gold (%d) - Silver (%d)\n",rabbit_value[0],rabbit_value[1]);
//        BOARD_Message();
//    }
//        
//    // Adjust evaluation to be from the perspective of the present player.
//
//    if (bp->at_move==SILVER)
//    {
//        value=-value;
//    }
//    return value;
//}
