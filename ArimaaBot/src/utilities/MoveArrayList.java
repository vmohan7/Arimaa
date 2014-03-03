package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import arimaa3.ArimaaMove;
import arimaa3.MoveList;

/** Our version of Jeff Bacher's MoveList that uses an ArrayList to store moves. */
public class MoveArrayList extends MoveList {

	private ArrayList<ArimaaMove> move_list;
	private Itr my_iterator = new Itr();

	public MoveArrayList(int max_size) {
		super(0);
		move_list = new ArrayList<ArimaaMove>(max_size);
	}

	@Override
	public void clear() {
		move_list.clear();
	}

	@Override
	public int size() {
		return move_list.size();
	}

	@Override
	public ArimaaMove getMove() {
		ArimaaMove temp = new ArimaaMove();
		move_list.add(temp);
		return temp;
	}



	public Iterator<ArimaaMove> iterator() {
		my_iterator.cursor = 0;
		return my_iterator;
	}

	private class Itr implements Iterator<ArimaaMove> {
		private int cursor = 0;

		public boolean hasNext() {
			return cursor != move_list.size();
		}

		public ArimaaMove next() {
			return move_list.get(cursor++);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public String toString() {
		String result = "";
		for ( int i=0; i<move_list.size(); i++ ) {
			result += " "+ move_list.get(i) +"\n";
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void sort() {
		// Arrays.sort(move_list,0,length);
		Collections.sort(move_list);
	}

}
