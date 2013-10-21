package arimaa3;

import java.util.*;
import ai_util.*;

public class ThreeFoldRepetition {
  public ThreeFoldRepetition() {
  }

  /**
   * Returns a list of the hash codes of all repetition banned positions
   * @param moves_text String
   * @return ArrayList
   */
  public static long[] setup(String moves_text) {

    LogFile.message(moves_text);

    ArrayList<Long> banned_positions = new ArrayList<Long>();

    // Place each move in a separate token
    moves_text = moves_text.replaceAll("%13", "@");
    StringTokenizer tokenizer = new StringTokenizer(moves_text, "@");

    // Check if there are some moves
    if ( tokenizer.countTokens() < 2 ) {
      return new long[0];
    }

    GameState gs = new GameState(tokenizer.nextToken(), tokenizer.nextToken());
//    System.out.println(gs);

    // Add the initial position hash code to the list
    ArrayList<Long> hash_codes = new ArrayList<Long>();
    hash_codes.add(new Long(gs.getPositionHash()));

    // Remaining tokens are normal moves
    while (tokenizer.hasMoreTokens()) {
      String move_token = tokenizer.nextToken();
      StringTokenizer temp = new StringTokenizer(move_token);
      temp.nextToken(); // discard move number

      String move = "";
      int steps = 0;
      while (temp.hasMoreTokens()) {
        String step_token = temp.nextToken();
        move += step_token + " ";
        steps++;
      }

      // The last line is blank, so skip it!
      if (steps == 0) {
        continue;
      }

      // Need to explicitly add pass to complete step
      if (steps < 4) {
        move += "pass";
      }

      // Play the move and record the hash codes
      ArimaaMove temp_move = new ArimaaMove(move);
      gs.play(temp_move, gs);
//      System.out.println(gs);

      hash_codes.add(new Long(gs.getPositionHash()));
    }

    // Scan thru the list of codes and check for duplicates
    // Any duplicates mean the position has occurred twice, so if
    // we reach the position again, we lose
    while (!hash_codes.isEmpty()) {
      Long test = (Long) hash_codes.get(hash_codes.size() - 1);
      hash_codes.remove(hash_codes.size() - 1);
      if (hash_codes.contains(test)) {
        // Duplicate found, add move to repetition list
        banned_positions.add(test);
        LogFile.message("Duplicate hash value: " + test);
      }
    }


    // Turn result into array
   long result[] = new long[banned_positions.size()];
   for ( int i=0; i<banned_positions.size(); i++ ) {
     result[i] = banned_positions.get(i).longValue();
   }
   return result;

 }


  public static void main(String args[]) {
    String text[] = {
      "1w Ed2 Md1 Hg2 Hb2 De1 Df2 Cc2 Ce2 Ra2 Rh2 Ra1 Rb1 Rc1 Rf1 Rg1 Rh1%131b ee7 hb7 hg7 me8 dc7 dd7 cd8 cf7 ra7 rh7 ra8 rb8 rc8 rf8 rg8 rh8%132w Ce2n De1n Hg2n Ed2n%132b hb7s hg7s ee7s ee6w%133w Df2e De2w Hb2n Md1e%133b cf7e dd7e cd8s de7e%134w Ce3e Cf3s Cc2w Dd2w%134b rc8e dc7w rd8w cd7w%135w Cf2w Dg2w Me1w Ed3e%135b rc8e ed6e rd8w me8w%136w Df2n Ce2e Cf2e Df3s%136b rf8w ee6w re8e md8e%137w Df2n Cg2w Cf2w Df3s%137b rc8e ed6e rd8w me8w%138w Df2n Ce2e Cf2e Df3s%138b rf8w ee6w re8e md8e%139w Dc2e Cb2e Md1e Ee3w%139b df7w me8w ed6e%1310w Cc2w Dd2w Me1w Ed3e%1310b rf8w de7e re8e%1311w Df2n Cg2w Cf2w Df3s%1311b cc7e md8e ee6w%1312w Df2e Ce2e Md1e Ee3w%1312b rc8e cd7w rd8w%1313w Dc2n Cb2e Cc2e Dc3s%1313b df7w me8w ed6e%1314w Dc2w Cd2w Me1w Ed3e%1314b rf8w de7e re8e%1315w Cf2n Dg2w Df2w Cf3s%1315b cc7e md8e ee6w%1316w Cf2e De2e Md1e Ee3w%1316b rc8e cd7w rd8w%1317w Cc2n Db2e Dc2e Cc3s%1317b df7w me8w ed6e%1318w Cc2w Dd2w Me1w Ed3e%1318b rf8w de7e re8e%1319w Df2n Cg2w Cf2w Df3s%1319b cc7e md8e ee6w%1320w Df2e Ce2e Md1e Ee3w%1320b",
      "1w Ca2 Db2 Hc2 Md2 Ee2 Hf2 Dg2 Ch2 Ra1 Rb1 Rc1 Rd1 Re1 Rf1 Rg1 Rh1%131b ed7 hb7 hg7 md8 de7 dc7 ce8 cf7 ra7 rh7 ra8 rb8 rc8 rf8 rg8 rh8%132w Ee2n Md2n Db2n Dg2n%132b hg7s hb7s dc7w ed7s%133w Ee3n Ee4e Ef4n Ef5e%133b rh7w de7s ce8s hg6e%134w Eg5w Ef5w Ee5s de6s%134b hh6w ed6s de5n ed5n%135w Ee4n Ee5s de6s Ca2n%135b rg7e ed6s de5n ed5n%136w Ee4n Ee5s de6s Ch2n%136b md8s ed6s de5n ed5n%137w Ee4n Ee5s de6s Md3n%137b ed6s de5n ed5n Md4n%138w Ee4n Ee5s de6s Ee4w%138b ed6e Md5n Md6w Mc6x ee6w%139w Ed4n Hc2n Hc3n Ra1n%139b ce7s de5e ed6w ec6s%1310w Ed5e Rd1n Rd2n Rd3n%1310b ec5n Hc4n ec6e Hc5n Hc6x%1311w df5s Ee5e df4s df3x Ef5s%1311b hb6s hb5s Db3e Dc3x hb4s%1312w Ef4n Ef5e Hf2n Hf3n%1312b hb3n Ca3e Cb3e Cc3x hb4s%1313w Rd4w Rc4n Rc5w Rb5n%1313b Rb6e Rc6x db7s rh7s rh6s%1314w Eg5w hg6s Ef5w hg5w%1314b ra7s ra6s ra5s ce6e%1315w Ee5s hf5w Ra2n Rb1n%1315b hb3n Rb2n Rb3e Rc3x hb4s%1316w Ee4w he5s he4s Ed4e%1316b",
      "1w Ed2 Md1 Hg2 Hb2 De1 Df2 Cc2 Ce2 Ra2 Rh2 Ra1 Rb1 Rc1 Rf1 Rg1 Rh1%131b ee7 hb7 hg7 me8 dc7 dd7 cd8 cf7 ra7 rh7 ra8 rb8 rc8 rf8 rg8 rh8%132w Ce2n De1n Hg2n Ed2n%132b hb7s hg7s ee7s ee6w%133w Df2e De2w Hb2n Md1e%133b cf7e dd7e cd8s de7e%134w Ce3e Cf3s Cc2w Dd2w%134b rc8e dc7w rd8w cd7w%135w Cf2w Dg2w Me1w Ed3e%135b rc8e ed6e rd8w me8w%136w Df2n Ce2e Cf2e Df3s%136b rf8w ee6w re8e md8e%137w Df2n Cg2w Cf2w Df3s%137b",
    };

    for ( String position : text ) {
      ThreeFoldRepetition.setup(position);
    }
  }

}
