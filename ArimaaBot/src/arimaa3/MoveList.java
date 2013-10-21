package arimaa3;

import java.util.*;

public class MoveList implements Iterable<ArimaaMove> {


  public MoveList(int max_size) {
    move_list = new ArimaaMove[max_size];
    for ( int i=0; i<max_size; i++ ) {
      move_list[i] = new ArimaaMove();
    }
    length = 0;
  }

  public void clear() {
    length = 0;
  }

  public int size() {
    return length;
  }

  public ArimaaMove getMove() {
    return move_list[length++];
  }


  /**
   * Returns the current move the internal iterator is on
   * @return ArimaaMove
   */
  public ArimaaMove getCurrentMove() {
    return move_list[my_iterator.cursor-1];
  }

  public ArimaaMove move_list[];
  private int length;  // exclusive end of the move list
  private Itr my_iterator = new Itr();

  public Iterator<ArimaaMove> iterator() {
    my_iterator.cursor = 0;
    return my_iterator;
  }

  private class Itr implements Iterator<ArimaaMove> {
    private int cursor = 0;

    public boolean hasNext() {
      return cursor != length;
    }

    public ArimaaMove next() {
      return move_list[cursor++];
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public String toString() {
    String result = "";
    for ( int i=0; i<length; i++ ) {
      result += " "+move_list[i]+"\n";
    }
    return result;
  }

  public void sort() {
    Arrays.sort(move_list,0,length);
  }


}
