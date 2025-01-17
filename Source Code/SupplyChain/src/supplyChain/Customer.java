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
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import supplyChain_ontology.SupplyChainOntology;
import supplyChain_ontology.elements.*;


public class Customer extends Agent {
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	private Order order;
	private AID systemTicker;
	private AID manufacturer;
	private boolean response = false;
	
	@Override
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("customer");
		sd.setName(getLocalName() + "-customer-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException e) {
			e.printStackTrace();
		}
		
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
	
	private PhoneSpecification generatePhone() {
		PhoneSpecification phone = new PhoneSpecification();
		
		// Generate a random number.
		double randomNumber = Math.random();  

		if (randomNumber < 0.5)
		{
			phone.setScreen(0);
			phone.setBattery(0);
		}
		else
		{
			phone.setScreen(1);
			phone.setBattery(1);
		}
		
		//	Generate a new random number.
		randomNumber = Math.random();
		
		if (randomNumber < 0.5)
		{
			phone.setRAM(0);
		}
		else
		{
			phone.setRAM(1);
		}
		
		//	Generate another new random number.
		randomNumber = Math.random();
		
		if (randomNumber < 0.5)
		{
			phone.setStorage(0);
		}
		else
		{
			phone.setStorage(1);
		}
		
		//Return randomly generated specification.
		return phone;
	}
	
	private Order generateOrder() {
		//Create instance of Order class. 
		Order thisOrder = new Order();
				
		//Name of Customer Ordering
		thisOrder.setCustomer(getAID());
		
		//Specification of phones being ordered
		thisOrder.setPhone(generatePhone());
				
		//Quantity of specified phone being requested.
		thisOrder.setQuantity((int) Math.floor(1 + (50*Math.random()))); 
				
		//Price of each phone specified.
		thisOrder.setPrice((int) Math.floor(100 + (500*Math.random()))); 
				
		//Number of days till order is due.
		thisOrder.setDays((int) Math.floor(1 + (10*Math.random())));
				
		//Penalty for late order delivery.
		thisOrder.setPenalty(thisOrder.getQuantity() * ((int) Math.floor(1 + (50*Math.random())))); 
		
		//Return Order.
		return thisOrder;
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
					
					dailyActivity.addSubBehaviour(new PrepareOrder(myAgent));
					dailyActivity.addSubBehaviour(new FindManufacturer(myAgent));
					dailyActivity.addSubBehaviour(new SendOrder(myAgent));
					dailyActivity.addSubBehaviour(new AwaitResponse(myAgent));
					dailyActivity.addSubBehaviour(new DeliveryReceived(myAgent));
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
	
	public class PrepareOrder extends OneShotBehaviour {
		
		public PrepareOrder(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			//Create order with all details to send to manufacturer.
			order = generateOrder(); 
		}
	}
	
	public class FindManufacturer extends OneShotBehaviour {
		
		public FindManufacturer(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			DFAgentDescription manufacturerTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("manufacturer");
			manufacturerTemplate.addServices(sd);
			
			//Since there SHOULD only be one Manufacturer in the system, only the first found active
			//manufacturer will be recorded.
			try {
				DFAgentDescription[] manufacturers = DFService.search(myAgent, manufacturerTemplate);
				manufacturer = manufacturers[0].getName();
			}
			catch (FIPAException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class SendOrder extends OneShotBehaviour {
		
		public SendOrder(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {

			ACLMessage thisOrder = new ACLMessage(ACLMessage.REQUEST);
			thisOrder.addReceiver(manufacturer);
			thisOrder.setLanguage(codec.getName());
			thisOrder.setOntology(ontology.getName());
			
			try {
				getContentManager().fillContent(thisOrder, order);
				send(thisOrder);
			}
			catch (CodecException ce) {
				ce.printStackTrace();
			}
			catch (OntologyException oe) {
				oe.printStackTrace();
			} 	
		}
	}
	
	public class AwaitResponse extends OneShotBehaviour {
		
		public AwaitResponse(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			while(!response){
				MessageTemplate mt;
				ACLMessage msg; 
				
				mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
				msg = receive(mt);
				if (msg != null) {
					if (msg.getContent().equals("order confirm")) {
						System.out.println(getAID().getLocalName()+": My Order was Accepted!");
						response = true;
					}
				}
				
				mt = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
				msg = receive(mt);
				if (msg != null) {
					if (msg.getContent().equals("order refuse")) {
						System.out.println(getAID().getLocalName()+": My Order was Rejected...");
						response = true;
					}
				}
			}
		}
	}
	
	public class DeliveryReceived extends OneShotBehaviour {
		
		public DeliveryReceived(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				if (msg.getContent().equals("order completed")) {
					System.out.println(getAID().getLocalName()+": One of my orders has been delivered!");
				}
			}
		}
	}
	
	public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			response = false;
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(systemTicker);
			msg.setContent("done");
			myAgent.send(msg);
		}	
	}	
}
