package supplyChain;

import jade.core.Agent;

public class Manufacturer extends Agent{

	protected void setup() {
		//int storageCost = 5; // "w"
		
		// 	For the order of which to tackle the list of orders, focus on "next day turnover"
		//using a queue and only stocking for the next day from the 1 day delivery supplier.
		
		
		System.out.println("Agent "+getAID().getName()+" is active."); //Test line
		
	}
}
