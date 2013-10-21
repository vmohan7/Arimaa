package ai_util;

import java.util.*;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RepetitionHashTable {
  public RepetitionHashTable(int size_in_bits) {
  }


  /**
   * Call this to setup a new repetition set in the table
   */
  public void increaseAge() {
    hash_current_age = (hash_current_age + 1) % hash_age_table.length;
    if ( hash_current_age == 0 ) {
      Arrays.fill(hash_table,0);
      hash_current_age++;
    }
  }

  // Hash table for storing repeated positions
  private static long hash_mask = 0xFFFFF;
  private static long hash_table[] = new long[65536 * 16];
  private static long hash_age_table[] = new long[256];
  private static int hash_current_age = 0;
  static {
    Random random = new Random(123456);
    for (int i = 0; i < hash_age_table.length; i++) {
      hash_age_table[i] = random.nextLong();
    }
  }



  /**
   * Tests if given hash_code is already in the hash table
   * Stores given hash code in the hash table
   * @param hash_code long
   * @return boolean
   */

  public boolean isRepetition(long hash_code) {
    // Need to use all 64 bits of hash to spread the indicies
    int index = (int) (hash_code % (hash_mask - 7));
    if (index < 0) {
      index += hash_mask;
    }
    long age_mask = 0xFF;
    long target = (hash_code & ~age_mask) | hash_current_age;
    boolean repeated = false;
    while (hash_current_age == (hash_table[index] & age_mask)) {
      if (target == hash_table[index]) {
        return true;
      }
      index = (index + 1) % (int) hash_mask;
    }

    hash_table[index] = target;
    return false;
  }



}
