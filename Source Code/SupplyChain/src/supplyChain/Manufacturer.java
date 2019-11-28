package supplyChain;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Manufacturer extends Agent{
	//Daily cost of each component stored overnight
	//	instead of being used.
	int storageCost = 5; // "w"
	private AID systemTicker;
	
	@Override
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new AwaitTicker(this));
		
		//
		System.out.println("Agent "+getAID().getName()+" is active."); //Test line
		//
	}
	
	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class AwaitTicker extends CyclicBehaviour {
		
		public AwaitTicker(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"), MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) { 
				if (systemTicker == null) {
					systemTicker = msg.getSender();
				}
				
				if (msg.getContent().equals("new day")) {
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					
					dailyActivity.addSubBehaviour(new ReceiveParts(myAgent));
					dailyActivity.addSubBehaviour(new AwaitOrders(myAgent));
					dailyActivity.addSubBehaviour(new DecideOrders(myAgent));
					dailyActivity.addSubBehaviour(new ReplyAcceptOrDeny(myAgent));
					dailyActivity.addSubBehaviour(new OrderParts(myAgent));
					dailyActivity.addSubBehaviour(new AssemblePhones(myAgent));
					dailyActivity.addSubBehaviour(new ShipPhones(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					
					myAgent.addBehaviour(dailyActivity);
				}
				else {
					myAgent.doDelete();
				}
			}
			else {
				block();
			}
		}
	}
	
	public class ReceiveParts extends OneShotBehaviour {
		
		public ReceiveParts(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Receive ordered parts from Suppliers !!
			
			
		}
	}
	
	public class AwaitOrders extends OneShotBehaviour {
		
		public AwaitOrders(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Receive orders from all Customer agents !!
			
			
		}
	}
	
	public class DecideOrders extends OneShotBehaviour {
		
		public DecideOrders(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Decide which orders from the Customers to accept and reject !!
			
			
		}
	}
	
	public class ReplyAcceptOrDeny extends OneShotBehaviour {
		
		public ReplyAcceptOrDeny(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Reply to each of the customers with "accepted" or "rejected" for their orders !!
			
			
		}
	}
	
	public class OrderParts extends OneShotBehaviour {
		
		public OrderParts(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Order parts for each of the accepted phone orders !!
			
			
		}
	}
	
	public class AssemblePhones extends OneShotBehaviour {
		
		public AssemblePhones(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Assemble the days orders with the stocked parts !!
			
			
		}
	}
	
	public class ShipPhones extends OneShotBehaviour {
		
		public ShipPhones(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Ship all completed orders for the day, removing them from the queue of orders !!
			
			
		}
	}
	
	public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(systemTicker);
			msg.setContent("done");
			myAgent.send(msg);
		}
	}	
}
	
	
	
	
	// 	For the order of which to tackle the list of orders, focus on "next day turnover"
	//using a queue and only stocking for the next day from the 1 day delivery supplier.
