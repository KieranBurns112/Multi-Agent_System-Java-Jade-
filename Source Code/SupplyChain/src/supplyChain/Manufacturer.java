package supplyChain;

import jade.core.AID;
import jade.core.Agent;
import java.util.ArrayList;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
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
	//The variable that tracks all monetary interactions with manufacturer - both earnings and losses.
	private int currentFunds = 0;
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();	
	//Daily cost of each component stored overnight
	//	instead of being used.
	private int storageCost = 5; // "w"
	private AID systemTicker;
	private int todaysCustomers = 0;
	private ArrayList<Order> allOrders = new ArrayList<>();
	private ArrayList<Order> processingOrders = new ArrayList<>();
	private PartsList currentStock = new PartsList();
	private PartsList requiredStock = new PartsList();
	private ArrayList<PartsInvoice> pendingDelivery = new ArrayList<>();
	private Order order;
	private int step = 0;
	int totalCustomers = 0;
	boolean partsConfirmed = false;
	
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
		
		addBehaviour(new AwaitTicker(this));
		// Receive Parts Behaviour
		
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
	
	private PartsList combinePartsLists(PartsList partsListA, PartsList partsListB) {
		PartsList partsListOut = new PartsList();
		
		partsListOut.setScreen_5inch(partsListA.getScreen_5inch() + partsListB.getScreen_5inch());
		partsListOut.setScreen_7inch(partsListA.getScreen_7inch() + partsListB.getScreen_7inch());
		partsListOut.setBattery_2000mAh(partsListA.getBattery_2000mAh() + partsListB.getBattery_2000mAh());
		partsListOut.setBattery_3000mAh(partsListA.getBattery_3000mAh() + partsListB.getBattery_3000mAh());
		partsListOut.setRAM_4Gb(partsListA.getRAM_4Gb() + partsListB.getRAM_4Gb());
		partsListOut.setRAM_8Gb(partsListA.getRAM_8Gb() + partsListB.getRAM_8Gb());
		partsListOut.setStorage_64Gb(partsListA.getStorage_64Gb() + partsListB.getStorage_64Gb());
		partsListOut.setStorage_256Gb(partsListA.getStorage_256Gb() + partsListB.getStorage_256Gb());
		
		return partsListOut;
	}
	
	private int todaysWarehouseCost () {
		//Cost is all parts added together, multiplied by "storageCost" (w).
		int cost = 0;
		
		cost += currentStock.getScreen_5inch();
		cost += currentStock.getScreen_7inch();
		cost += currentStock.getBattery_2000mAh();
		cost += currentStock.getBattery_3000mAh();
		cost += currentStock.getRAM_4Gb();
		cost += currentStock.getRAM_8Gb();
		cost += currentStock.getStorage_64Gb();
		cost += currentStock.getStorage_256Gb();
		
		cost = cost*storageCost;
		
		return cost;
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
					
					dailyActivity.addSubBehaviour(new NewDay(myAgent));
					dailyActivity.addSubBehaviour(new AwaitOrders(myAgent));
					dailyActivity.addSubBehaviour(new DecideOrders(myAgent));
					dailyActivity.addSubBehaviour(new OrderParts(myAgent));
					dailyActivity.addSubBehaviour(new AssembleAndShipPhones(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					
					myAgent.addBehaviour(dailyActivity);
				}
				else {
					System.out.println("Total earnings: �" + currentFunds);
					myAgent.doDelete();
				}
			}
			else {
				block();
			}
		}
	}
	
	public class NewDay extends OneShotBehaviour {
		
		public NewDay(Agent agent) {
			super(agent);
		}
		
		public void action() {
			if (allOrders.size() > 0) {
				for (int i = 0; i < allOrders.size(); i++) {
					allOrders.get(i).setDays(allOrders.get(i).getDays() - 1);
				}
			}
			
			if (pendingDelivery.size() > 0) {
				for (int i = 0; i < pendingDelivery.size(); i++) {
					pendingDelivery.get(i).setDueDays(pendingDelivery.get(i).getDueDays() - 1);
				}
				
				ArrayList<PartsInvoice> deliveryToday = new ArrayList<>();
				for (PartsInvoice invoice : pendingDelivery) {
					if (invoice.getDueDays() == 0) {
						deliveryToday.add(invoice);
						currentStock = combinePartsLists(currentStock, invoice.getParts());
					}
				}
				pendingDelivery.removeAll(deliveryToday);
			}	
		}
		
	}
	
	public class AwaitOrders extends OneShotBehaviour {
		
		public AwaitOrders(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			switch(step) {		
			case 0:
				
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
				step++;
				
			case 1:
				while (todaysCustomers != totalCustomers) {
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
					ACLMessage msg = receive(mt);
					if (msg != null) {
						try {	
							order = (Order) getContentManager().extractContent(msg);	
										
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
					else {
						block();
					}	
				}
				step++;
				break;
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
				boolean acceptOrder = false; 
				//^!!This is the answer!!^
				
				
				
				
				
				
				/*
				//!!Test to display the randomised orders!!			
				PartTypes partTypes = new PartTypes();
	
				String outputLine = "Agent "+ processingOrders.get(i).getCustomer().getName()+"'s order: ";
				PhoneSpecification orderPhone = processingOrders.get(i).getPhone();
	
				outputLine += partTypes.listScreens() [orderPhone.getScreen()] + ", ";					
				outputLine += partTypes.listBatteries() [orderPhone.getBattery()] + ", ";
				outputLine += partTypes.listRAM() [orderPhone.getRAM()] + ", ";
				outputLine += partTypes.listStorage() [orderPhone.getStorage()];
		
				outputLine += "  |  Quantity: " + processingOrders.get(i).getQuantity();
				outputLine += "  |  Due in " + processingOrders.get(i).getDays() + " days.";
				outputLine += "  |  �" + processingOrders.get(i).getPrice() + " per unit.";
				outputLine += "  |  �" + processingOrders.get(i).getPenalty() + " penalty per day past due date.";
		
				System.out.println(outputLine);
				//!End of test!!
				*/
				
				
				AID currentCustomer = processingOrders.get(i).getCustomer();
				
				ACLMessage reply;
				
				if (acceptOrder) {
					allOrders.add(processingOrders.get(i));
					
					
					
					
					
					
					
					//!!Add all required parts by the day's accepted orders to requiredStock!!
					
					
					
					
					
					
					
					reply = new ACLMessage(ACLMessage.AGREE);
					reply.setContent("order confirm");
				}
				else {
					reply = new ACLMessage(ACLMessage.REFUSE);
					reply.setContent("order refuse");
				}
				reply.addReceiver(currentCustomer);
				myAgent.send(reply);
			}
			processingOrders.clear();
			
		}
	}
	
	public class OrderParts extends OneShotBehaviour {
		
		public OrderParts(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			PartsList blankPartsList = new PartsList();
			
			if (!requiredStock.equals(blankPartsList)) {
				DFAgentDescription supplierTemplate = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("supplier");
				supplierTemplate.addServices(sd);
				
				//The manufacturer strategy of this manufacturer involves ordering from exclusively
				//"Supplier1"
				try {
					DFAgentDescription[] suppliers = DFService.search(myAgent, supplierTemplate);
					AID supplier1 = null;
					
					for (int i = 0; i < suppliers.length; i++) {
						AID currentSupplier = suppliers[i].getName();
						if (currentSupplier.getLocalName().equals("Supplier1")) {
							supplier1 = currentSupplier;
						}
					}
					
					if (supplier1 != null) {
						ACLMessage partsOrder = new ACLMessage(ACLMessage.REQUEST);
						partsOrder.addReceiver(supplier1);
						partsOrder.setLanguage(codec.getName());
						partsOrder.setOntology(ontology.getName());
						
						PartsRequest content = new PartsRequest();
						content.setManufacturer(getAID());
						content.setParts(requiredStock);
						
						try {
							getContentManager().fillContent(partsOrder, content);
							send(partsOrder);						
				
							MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
							ACLMessage msg = receive(mt);
							if (msg != null) {
								try {	
									PartsInvoice invoice = (PartsInvoice) getContentManager().extractContent(msg);
									pendingDelivery.add(invoice);
									currentFunds -= invoice.getCost();
									partsConfirmed = true;
								}
								catch (CodecException ce) {
									ce.printStackTrace();
								}
								catch (OntologyException oe) {
									oe.printStackTrace();
								}
							}
							else {
								block();
							}
						}
						catch (CodecException ce) {
							ce.printStackTrace();
						}
						catch (OntologyException oe) {
							oe.printStackTrace();
						} 	
					}
					else {
						System.out.println("Parts Request NOT Sent: Couldn't find Supplier1!");
					}
				}
				catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public class AssembleAndShipPhones extends OneShotBehaviour {
		
		public AssembleAndShipPhones(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			
			
			// !! Assemble the days orders with the stocked parts !!
			
			// !! Ship all completed orders for the day, removing them from the queue of orders !!
			
			// !! Calculate any penalties on orders with days < 0 !!
			
			
		}
	}
	
	public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {		
			//Apply overnight parts storage costs.
			currentFunds -= todaysWarehouseCost();
			
			//Set all daily variables back to default.
			todaysCustomers = 0;
			step = 0;
			partsConfirmed = false;
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(systemTicker);
			msg.setContent("done");
			myAgent.send(msg);
		}
	}	
}
