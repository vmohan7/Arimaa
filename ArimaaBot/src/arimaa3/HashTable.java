package arimaa3;

import java.util.*;

// Implements a transposition table for alpha/beta
// Does NOT STORE THE MOVE
// Stores score,depth,bound,age

// There are two tables, always replace and (depth or newer)

public class HashTable implements Constants {


  // Unknown hash value results
  public static final int NO_SCORE = -100024222;

  // All hash data stored here
  protected long hash_data[];
  protected ArimaaMove move_data[];
  protected int table_size;
  protected int global_age;

  private static final int entry_size = 4;

  public HashTable(int table_size) {
    hash_data = new long[table_size * entry_size];
    this.table_size = table_size;
    move_data = new ArimaaMove[table_size*2];
    for ( int i=0; i<move_data.length; i++ ) {
      move_data[i] = new ArimaaMove();
    }
    reset();
  }

  public int getSize() {
    return table_size;
  }

  public void reset() {
    // Places hash table in known state, so searches are repeatable
    for (int i = 0; i < hash_data.length; i++) {
      hash_data[i] = 0;
    }
    for ( int i=0; i<move_data.length; i++ ) {
      move_data[i].clear();
    }
    // Reset the current age
    global_age = 0;
  }

  // Possible values for bound field
  public static final int NO_BOUND = 0;
  public static final int UPPER_BOUND = 1;
  public static final int LOWER_BOUND = 2;
  public static final int EXACT_BOUND = 3;

  // Adds an entry to the hash table
  public void RecordHash(long hashKey, int depth, int value, int bound, ArimaaMove move, boolean is_defend_threat_search) {

    // The value is stored as a signed 16 bit number
    // Value is now a 20 bit number, but still keep the asserts
    // Anything outside that range will return an incorrect value
    assert (value >= -32768);
    assert (value <= 32767);

    int index = getHashIndex(hashKey);

    // First entry uses the "if deeper or newer search then replace"
    long current_entry = hash_data[index + 1];
    int current_depth = (int) ((current_entry >> 22) & 0xFF);
    int current_age = (int) ((current_entry >> 30) & 0x7);

    if ((current_age != global_age || depth >= current_depth)) {

      // Copy existing entry to always replace table
//      hash_data[index+2] = hash_data[index];
//      hash_data[index+3] = hash_data[index+1];


      // Add new entry
      long hashInfo = getHashInfo(depth, value, bound,move,is_defend_threat_search);
      hash_data[index] = hashKey;
      hash_data[index + 1] = hashInfo;
      if ( move != null ) {
        move_data[getMoveIndex(hashKey)+0].copy(move);
      }
    }

    // Second entry uses the always replace scheme
    // Only replace if first entry wasn't replaced
    else {
      long hashInfo = getHashInfo(depth, value, bound,move,is_defend_threat_search);
      hash_data[index + 2] = hashKey;
      hash_data[index + 3] = hashInfo;
      if ( move != null ) {
        move_data[getMoveIndex(hashKey)+1].copy(move);
      }
    }

  }

  // Returns the index into data array for key
  private int getHashIndex(long hashKey) {
    int index = (int) (hashKey % table_size);
    if (index < 0) {
      index += table_size;
    }
    index *= entry_size;
    return index;
  }

  // Returns the index into move array for key
  private int getMoveIndex(long hashKey) {
    int index = (int) (hashKey % table_size);
    if (index < 0) {
      index += table_size;
    }
    index *= 2;
    return index;
  }

  // Returns hash Info compressed into a long
  private long getHashInfo(int depth, int value, int bound, ArimaaMove move,boolean is_defend_threat_search) {
    long result = 0;

    if ( is_defend_threat_search ) {
      result += 1L << 34;
    }

    if ( move != null ) {
      result += 1L << 33;
    }

    result += (global_age & 0x7L) << 30;
    result += (depth & 0xFFL) << 22;
    result += (value & 0xFFFFFL) << 2;
    result += (bound & 0x3L);

    return result;
  }

  // returns true if hashKey is in the table
  public boolean containsKey(long hashKey) {
    boolean result = false;
    int index = getHashIndex(hashKey);
    if (hash_data[index] == hashKey || hash_data[index + 2] == hashKey) {
      result = true;
    }
    return result;
  }

  // Probes hash table using provided hash key
  public int ProbeHash(long hashKey2, int depth, int alpha, int beta, boolean is_defend_threat_search) {

    int index = getHashIndex(hashKey2);
    int result = HashTable.NO_SCORE;

    // Check the depth/newer table
    if (hash_data[index] == hashKey2) {
      long hashInfo = hash_data[index + 1];
      result = test_for_cutoff(hashInfo, depth, alpha, beta,is_defend_threat_search);
      if (result != HashTable.NO_SCORE) {
        return result;
      }
    }

    // Check the always replace table
    if (hash_data[index + 2] == hashKey2) {
      long hashInfo = hash_data[index + 3];
      result = test_for_cutoff(hashInfo, depth, alpha, beta,is_defend_threat_search);
      if (result != HashTable.NO_SCORE) {
        return result;
      }
    }

    return result;
  }

  private int test_for_cutoff(long hashInfo, int depth, int alpha, int beta, boolean is_defend_threat_search) {
    int hash_depth = (int) ((hashInfo >> 22) & 0xFF);
    int hash_value = (short) ((hashInfo >> 2) & 0xFFFFF);
    int hash_bound = (int) ((hashInfo & 0x3));
    boolean is_stored_threat_search = (((hashInfo >> 34) & 0x01) == 1) ? true : false;

    // If we have a game theoretic score then depth doesn't matter since
    // a deeper search won't discover anything new. Just use the score!!!
    // This means the value is always good
    // IE: If its a mate score, set the draft to infinity

    // Arimaa Exception:
    // Defend threat search can have mate scores, that do not prove a loss/win
    // So don't increase depth if in this case

    // NOTE: hash_value >= 30000,  hashf = ALPHA is not infinite depth
    // NOTE: hash_value <= -30000, hashf = BETA  is not infinite depth
    if ( !is_stored_threat_search ) {
      if (hash_value >= SCORE_FORCED_WIN &&
          (hash_bound == LOWER_BOUND || hash_bound == EXACT_BOUND)) {
        hash_depth = Integer.MAX_VALUE;
      } else if (hash_value <= SCORE_FORCED_LOSS &&
        (hash_bound == UPPER_BOUND || hash_bound == EXACT_BOUND)) {
        hash_depth = Integer.MAX_VALUE;
      }
    }

    // A defend threat search can use a normal hash value
    // A normal search cant use a threat hash value because
    // the threat search mate scores don't mean the position is lost
    if ( !is_defend_threat_search && is_stored_threat_search ) {
      return HashTable.NO_SCORE;
    }

    // Check if we have enough depth
    if (hash_depth >= depth) {

      // Score is an exact value
      if (hash_bound == EXACT_BOUND) {
        return hash_value;
      }

      // Score is an upper bound
      if ((hash_bound == UPPER_BOUND) && (hash_value <= alpha)) {
        return hash_value;
      }

      // Score is a lower bound
      if ((hash_bound == LOWER_BOUND) && (hash_value >= beta)) {
        return hash_value;
      }
    }

    return HashTable.NO_SCORE;
  }


  // Increases the current age field used by the hash table
  public void IncreaseHashAge() {
    global_age++;
    if (global_age > 7) {
      global_age = 0;
    }
  }

  // Returns suggested move for a given position
  // If no move found then return no_move
  private int hash_move_depth;

  // This is only valid until the next ProbeHashMove call!!!!
  public int GetHashMoveDepth() {
    return hash_move_depth;
  }


  public ArimaaMove ProbeHashMove(long hashKey) {
    int index = getHashIndex(hashKey);
    hash_move_depth = -1;

    // Check the depth/newer table
    if (hash_data[index] == hashKey) {
      long hashInfo = hash_data[index + 1];
      if (((hashInfo>>33) & 0x01L) != 0) {
        hash_move_depth = (int) ( (hashInfo >> 22) & 0xFF);
        return move_data[getMoveIndex(hashKey)];
      }
    }

    // Check the always replace table
    if (hash_data[index + 2] == hashKey) {
      long hashInfo = hash_data[index + 3];
      if (((hashInfo>>33) & 0x01L) != 0) {
        hash_move_depth = (int) ( (hashInfo >> 22) & 0xFF);
        return move_data[getMoveIndex(hashKey)+1];
      }
    }

    return null;
  }


}
