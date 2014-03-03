package fairy_agents;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import arimaa3.GameState;
import montecarlo.AbstractCombiner;

import org.junit.Test;

import fairy_agents.FairyEvaluation;
import fairy_agents.FairyEvaluation.FairyBoard;
import game_phase.GamePhase;

public class FairyTest {
	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   e             |%135| R               |%134|                 |%133| R               |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   E             |%135| r               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| r               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135| c               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| m E             |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| c               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
	};
	
	
	@Test
	public void testConvertBoard(){
		
		// Test this starting position
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState state = new GameState(white,black);
	    
	    //System.out.println(state.toBoardString());
	    
	    FairyEvaluation fe = new FairyEvaluation();
		FairyBoard fb = fe.new FairyBoard(state);
		
		//System.out.println(fb.toBoardString());
		
		assertTrue(fb.isEqualToGS(state));
		
		// Test all boards in tests[]
		for (int i = 0; i < tests.length; i++){
			state = new GameState(tests[i]);
			fb = fe.new FairyBoard(state);
			assertTrue(fb.isEqualToGS(state));
		}
		
	}
	
	
	/** Needed an implementation of AbstractCombiner... */
	private class DefaultTestCombiner extends AbstractCombiner {
		public DefaultTestCombiner(GamePhase whichPhase) { super(whichPhase); }
	}
	
	/** 
	 * Checks against a test file of output from C code. 
	 * The C code is provided for reference at the bottom of this file. 
	 */
	@Test
	public void testDefaultEvaluation() {
		
		final String TESTS_EXTENSION = "fairy-testing/";
		final String TESTS_FILE = "tests.txt"; // the location of the file with test boards
		final String C_CORRECT_OUTPUT = "test_results_navv_C.txt"; // formatted test output from C
		
		FairyEvaluation fe = new FairyEvaluation(); // for the fairy boards
		AbstractCombiner ac = new DefaultTestCombiner(null); // for the call to eval
		
		try {
			
			BufferedReader testsRd = new BufferedReader(new FileReader(TESTS_EXTENSION + TESTS_FILE));
			BufferedReader correctRd = new BufferedReader(new FileReader(TESTS_EXTENSION + C_CORRECT_OUTPUT));
			
			int nTestCases = Integer.parseInt(readLineIgnoreComments(testsRd));
			
			// Read each test file in order and get a score for each...
			for (int test = 0; test < nTestCases; test++) {
				// get the file name
				String filename = readLineIgnoreComments(testsRd);
				assertTrue(filename != null); // sanity-checks
				
				// evaluate the board
				FairyBoard fb = fe.new FairyBoard(TESTS_EXTENSION + filename);
				int score = FairyEvaluation.evaluate(fb, ac);	
	
			    // format the score into the C output's format and compare the two
				String testResult = String.format("File %s: %d", filename, score);
				String correctResult = correctRd.readLine();
				assertTrue(testResult.equals(correctResult));
				
				// skip the number following each file name
				String toSkip = readLineIgnoreComments(testsRd);
				assertTrue(toSkip != null); // sanity-checks
			}
			
			testsRd.close();
			correctRd.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Exiting...");
			assertTrue(false);
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

}


/** REFERENCE 1 -- The C code which runs the C evaluation function and outputs scores **
 *  			-- I have commented the code to the extent it made sense to make sense
 *  			   of botFairy's code
 *  			-- Note further that the C code had a bug in it that was fixed before I created
 *  			   the output file: (row-1)*(row-1) should have been (row-1)*(row-1)*(row-1)

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "board.h"
#include "search.h"
#include "hash.h"
#include "eval.h"


static const char *TESTS_DOCUMENT = "tests.txt";
static const char *OUTPUT_DOCUMENT = "test_results_navv_C.txt";

//copied over and modified runtest.c's main function
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