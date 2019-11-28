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

public class Customer extends Agent {
	Order order;
	private AID systemTicker;
	
	@Override
	protected void setup() {
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
					dailyActivity.addSubBehaviour(new SendOrder(myAgent));
					dailyActivity.addSubBehaviour(new GetReply(myAgent));
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
	
	public class SendOrder extends OneShotBehaviour {
		
		public SendOrder(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {

			
			
			
			// !! SEND ORDER TO MANUFACTURER AGENT !!
			
			
			
			
			//!! Test to check if order works !!			
			PartTypes partTypes = new PartTypes();
			
			String outputLine = "Agent "+getAID().getName()+"'s order: ";
			PhoneSpecification orderPhone = order.getPhone();
			
			outputLine += partTypes.listScreens() [orderPhone.getScreen()] + ", ";
			outputLine += partTypes.listBatteries() [orderPhone.getBattery()] + ", ";
			outputLine += partTypes.listRAM() [orderPhone.getRAM()] + ", ";
			outputLine += partTypes.listStorage() [orderPhone.getStorage()];
			
			outputLine += "  |  Quantity: " + order.getQuantity();
			outputLine += "  |  Due in " + order.getDays() + " days.";
			outputLine += "  |  �" + order.getPrice() + " per unit.";
			outputLine += "  |  �" + order.getPenalty() + " penalty per day past due date.";
			
			System.out.println(outputLine);
			// !! End of Test !!	
		}
	}
	
	public class GetReply extends OneShotBehaviour {
		
		public GetReply(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! AWAIT REPLY FROM MANUFACTURER !!
			
			// !! REQUIRES BOTH ACCEPT AND REJECT CLAUSES !!
			
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
