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
		
		System.out.println( "First weight " + minPQ.remove().weight);
		System.out.println( "Second weight " + minPQ.remove().weight);
	}

}
