package supplyChain;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
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

public class Supplier extends Agent{
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	private AID systemTicker;
	private String[] stock;
	private int[] prices;
	private int deliveryDays;
	
	@Override
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("supplier");
		sd.setName(getLocalName() + "-supplier-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException e) {
			e.printStackTrace();
		}

		if (this.getLocalName().equals("Supplier1")) {
			Supplier1_info info = new Supplier1_info();
			stock = info.Items();
			prices = info.Prices();
			deliveryDays = info.DeliveryDays();
		}
		else if (this.getLocalName().equals("Supplier2")) {
			Supplier2_info info = new Supplier2_info();
			stock = info.Items();
			prices = info.Prices();
			deliveryDays = info.DeliveryDays();
		}
		else {
			System.out.println("Unable to load any supplier data for " + this.getLocalName());
			this.doDelete();
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
					
					dailyActivity.addSubBehaviour(new HandleRequests(myAgent));
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
	
	public class HandleRequests extends OneShotBehaviour {
		
		public HandleRequests(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				try {	
					PartsRequest request = (PartsRequest) getContentManager().extractContent(msg);
					
					AID manufacturer = request.getManufacturer();
					PartsList requestedParts = request.getParts();
					
					PartsList invoiceParts = new PartsList();
					
					int totalCost = 0;
					
					//If the items requested are stocked by the supplier, add them to the invoice.
					for (int i = 0; i < stock.length; i++) {
						if (stock[i].equals("5\" Screen")){
							invoiceParts.setScreen_5inch(requestedParts.getScreen_5inch());
							totalCost += prices[i] * requestedParts.getScreen_5inch();
						}
						else if (stock[i].equals("7\" Screen")) {
							invoiceParts.setScreen_7inch(requestedParts.getScreen_7inch());
							totalCost += prices[i] * requestedParts.getScreen_7inch();
						}
						else if (stock[i].equals("64Gb Storage")) {
							invoiceParts.setStorage_64Gb(requestedParts.getStorage_64Gb());
							totalCost += prices[i] * requestedParts.getStorage_64Gb();
						}
						else if (stock[i].equals("256Gb Storage")) {
							invoiceParts.setStorage_256Gb(requestedParts.getStorage_256Gb());
							totalCost += prices[i] * requestedParts.getStorage_256Gb();
						}
						else if (stock[i].equals("4Gb RAM")) {
							invoiceParts.setRAM_4Gb(requestedParts.getRAM_4Gb());
							totalCost += prices[i] * requestedParts.getRAM_4Gb();
						}
						else if (stock[i].equals("8Gb RAM")) {
							invoiceParts.setRAM_8Gb(requestedParts.getRAM_8Gb());
							totalCost += prices[i] * requestedParts.getRAM_8Gb();
						}
						else if (stock[i].equals("2000mAh Battery")) {
							invoiceParts.setBattery_2000mAh(requestedParts.getBattery_2000mAh());
							totalCost += prices[i] * requestedParts.getBattery_2000mAh();
						}
						else if (stock[i].equals("3000mAh Battery")) {
							invoiceParts.setBattery_3000mAh(requestedParts.getBattery_3000mAh());
							totalCost += prices[i] * requestedParts.getBattery_3000mAh();
						}
					}
					
					PartsInvoice invoice = new PartsInvoice();
					invoice.setDueDays(deliveryDays);
					invoice.setCost(totalCost);
					invoice.setParts(invoiceParts);
					
					ACLMessage confirmOrder = new ACLMessage(ACLMessage.AGREE);
					confirmOrder.addReceiver(manufacturer);
					confirmOrder.setLanguage(codec.getName());
					confirmOrder.setOntology(ontology.getName());
					
					try {
						getContentManager().fillContent(confirmOrder, invoice);
						send(confirmOrder);
					}
					catch (CodecException ce) {
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					} 	
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
