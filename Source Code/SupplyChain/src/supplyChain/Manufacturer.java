package supplyChain;

import java.util.ArrayList;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
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
import supplyChain_ontology.SupplyChainOntology;
import supplyChain_ontology.elements.*;

public class Manufacturer extends Agent{
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();	
	//Daily cost of each component stored overnight
	//	instead of being used.
	private int storageCost = 5; // "w"
	private AID systemTicker;
	private int todaysCustomers = 0;
	private ArrayList<Order> allOrders = new ArrayList<>();
	private ArrayList<Order> processingOrders = new ArrayList<>();
	
	@Override
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
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
		
		addBehaviour(new ReceiveOrders());
		addBehaviour(new AwaitTicker(this));
		
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
	
	public class ReceiveOrders extends CyclicBehaviour {
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				try {	
					Order order = (Order) getContentManager().extractContent(msg);	
								
					processingOrders.add(order);
					
					todaysCustomers++;
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				} 
			}
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
			
			int totalCustomers = 0;
			
			DFAgentDescription customerTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("customer");
			customerTemplate.addServices(sd);
			
			try {
				DFAgentDescription[] allCustomers = DFService.search(myAgent, customerTemplate);
				totalCustomers = allCustomers.length;
			}
			catch (FIPAException e) {
				e.printStackTrace();
			}
			
			if (todaysCustomers != totalCustomers) {
				block();
			}

		}
	}
	
	public class DecideOrders extends OneShotBehaviour {
		
		public DecideOrders(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			for (int i = 0; i < processingOrders.size(); i++) {
				
				// !! Decide which orders from the Customers to accept and reject !!
				// THIS IS THE MANUFACTURER AGENT CONTROL STRATEGY!!!!!
				// Possibly use an external class function to do this process, which
				//  has a single Order instance passed in, then returns a "true or false"
				//	answer whether to accept the order or not.
				boolean acceptOrder = true; 
				//^!!This is the answer!!^
				
				if (acceptOrder) {
					allOrders.add(processingOrders.get(i));
					//Set message to be returned to this customer to "Request Accepted"
				}
				else {
					//Set message to be returned to this customer to "Request Denied"
				}
			}
			processingOrders.clear();
			
			
			for (int i = 0; i < allOrders.size(); i++) {
				
				
				//!! Test to check if order works !!			
				PartTypes partTypes = new PartTypes();
	
				String outputLine = "Agent "+ allOrders.get(i).getCustomer().getName()+"'s order: ";
				PhoneSpecification orderPhone = allOrders.get(i).getPhone();
	
				outputLine += partTypes.listScreens() [orderPhone.getScreen()] + ", ";
				outputLine += partTypes.listBatteries() [orderPhone.getBattery()] + ", ";
				outputLine += partTypes.listRAM() [orderPhone.getRAM()] + ", ";
				outputLine += partTypes.listStorage() [orderPhone.getStorage()];
	
				outputLine += "  |  Quantity: " + allOrders.get(i).getQuantity();
				outputLine += "  |  Due in " + allOrders.get(i).getDays() + " days.";
				outputLine += "  |  �" + allOrders.get(i).getPrice() + " per unit.";
				outputLine += "  |  �" + allOrders.get(i).getPenalty() + " penalty per day past due date.";
	
				System.out.println(outputLine);
				
				//Cleanliness for testing
				allOrders.clear();	
				// !! End of Test !!
			}
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
			
			//Set all orders taken variable back to default value for next day.
			todaysCustomers = 0;
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(systemTicker);
			msg.setContent("done");
			myAgent.send(msg);
		}
	}	
}
	
	// 	For the order of which to tackle the list of orders, focus on "next day turnover"
	//using a queue and only stocking for the next day from the 1 day delivery supplier.
