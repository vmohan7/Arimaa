package montecarlo;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import montecarlo.AbstractNAVVSearch.MoveOrder;

import org.junit.Test;

public class MonteCarloTest {

	@Test
	public void test() {
		NaiveReflexAgent temp = new NaiveReflexAgent(null, false, null);
		PriorityQueue<MoveOrder> minPQ = new PriorityQueue<MoveOrder>( 3, temp.new MoveOrder(true) );
		minPQ.add(temp.new MoveOrder(null, 0.5) );
		minPQ.add(temp.new MoveOrder(null, 0.6) );
		
		assertEquals(minPQ.remove(), 0.5);
		assertEquals(minPQ.remove(), 0.6);

	}

}
