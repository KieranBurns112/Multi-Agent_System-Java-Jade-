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
	private int todaysProfit = 0;
	private int totalProfit = 0;
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();	
	//Daily cost of each component stored overnight
	//	instead of being used.
	private int storageCost = 5; // "w"
	private AID systemTicker;
	private int todaysCustomers = 0;
	private Order todaysOrder = new Order();
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
					//Do Daily Behaviours 
					dailyActivity.addSubBehaviour(new NewDay(myAgent));
					dailyActivity.addSubBehaviour(new AwaitOrders(myAgent));
					dailyActivity.addSubBehaviour(new DecideOrders(myAgent));
					dailyActivity.addSubBehaviour(new OrderParts(myAgent));
					dailyActivity.addSubBehaviour(new AssembleAndShipPhones(myAgent));
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
	
	public class NewDay extends OneShotBehaviour {
		
		public NewDay(Agent agent) {
			super(agent);
		}
		
		public void action() {	
			if (allOrders.size() > 0) {		
				todaysOrder = allOrders.get(0);
				
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
			
			int currentBestOrder = -1;
			int currentBestProfit = 0;
			
			//loop through each order and find the one with the best profit that can be done tomorrow.	
			for (int i = 0; i < processingOrders.size(); i++) {
				processingOrders.get(i).setAccepted(false);
				
				if (processingOrders.get(i).getQuantity() <= 50) {
					//Earnings are calculated from unit Price per phone multiplied by quantity of phones.
					int thisPartsCost = 0;
					PhoneSpecification thisPartsList = processingOrders.get(i).getPhone();
					
					//All parts are purchased from Supplier1 using this manufacturer control strategy.
					Supplier1_info supplierPrices = new Supplier1_info();
					
					//This set of if...else statements calculates the prices of a single phone, based on the
					//	available info from Supplier1's price list, which in this version the the control strategy
					//	is the only supplier shopped from, due to the next day delivery service they provide.
					if (thisPartsList.getScreen() == 0) {
						thisPartsCost += supplierPrices.Prices()[0];
					}
					else {
						thisPartsCost += supplierPrices.Prices()[1];
					}
					
					if (thisPartsList.getBattery() == 0) {
						thisPartsCost += supplierPrices.Prices()[2];
					}
					else {
						thisPartsCost += supplierPrices.Prices()[3];
					}
					
					if (thisPartsList.getRAM() == 0) {
						thisPartsCost += supplierPrices.Prices()[4];
					}
					else {
						thisPartsCost += supplierPrices.Prices()[5];
					}
					
					if (thisPartsList.getStorage() == 0) {
						thisPartsCost += supplierPrices.Prices()[6];
					}
					else {
						thisPartsCost += supplierPrices.Prices()[7];
					}
					
					//The profit generated by a phone is calculated by (unit price*total units)-(parts cost of 1 unit*total units). 
					int thisProfit = (processingOrders.get(i).getPrice() * processingOrders.get(i).getQuantity()) -
							(thisPartsCost * processingOrders.get(i).getQuantity());
							
					if (thisProfit > currentBestProfit) {
						int oldBestOrder = currentBestOrder;
						currentBestOrder = i;
						
						currentBestProfit = thisProfit;
						
						if (oldBestOrder != -1){
							processingOrders.get(oldBestOrder).setAccepted(false);
						}
						processingOrders.get(currentBestOrder).setAccepted(true);
					}
				}
				
				//Display each of the phones that have been ordered	
				PartTypes partTypes = new PartTypes();

				String outputLine = "Agent "+ processingOrders.get(i).getCustomer().getLocalName()+"'s order: ";
				PhoneSpecification orderPhone = processingOrders.get(i).getPhone();

				outputLine += partTypes.ListScreens() [orderPhone.getScreen()] + ", ";					
				outputLine += partTypes.ListBatteries() [orderPhone.getBattery()] + ", ";
				outputLine += partTypes.ListRAM() [orderPhone.getRAM()] + ", ";
				outputLine += partTypes.ListStorage() [orderPhone.getStorage()];

				outputLine += "  |  Quantity: " + processingOrders.get(i).getQuantity();
				outputLine += "  |  Due in " + processingOrders.get(i).getDays() + " days.";
				outputLine += "  |  �" + processingOrders.get(i).getPrice() + " per unit.";
				outputLine += "  |  �" + processingOrders.get(i).getPenalty() + " penalty per day past due date.";

				System.out.println(outputLine);
			}
			
			//Return responses based on which orders are decided.
			for (int i = 0; i < processingOrders.size(); i++) {
				AID currentCustomer = processingOrders.get(i).getCustomer();
				
				ACLMessage reply;
				
				if (processingOrders.get(i).getAccepted()) {
					allOrders.add(processingOrders.get(i));
					
					//Order the required parts.
					PhoneSpecification thisPhone = processingOrders.get(i).getPhone();
					int phoneAmount = processingOrders.get(i).getQuantity();
					
					//if...else used because there are only 2 types of each part, can be easily modified by adding extra
					//else if clauses to accommodate extra part types.
					if (thisPhone.getScreen() == 0) {
						requiredStock.setScreen_5inch(requiredStock.getScreen_5inch() + phoneAmount);
					}
					else {
						requiredStock.setScreen_7inch(requiredStock.getScreen_7inch() + phoneAmount);
					}
					
					if (thisPhone.getBattery() == 0) {
						requiredStock.setBattery_2000mAh(requiredStock.getBattery_2000mAh() + phoneAmount);
					}
					else {
						requiredStock.setBattery_3000mAh(requiredStock.getBattery_3000mAh() + phoneAmount);
					}
					
					if (thisPhone.getRAM() == 0) {
						requiredStock.setRAM_4Gb(requiredStock.getRAM_4Gb() + phoneAmount);
					}
					else {
						requiredStock.setRAM_8Gb(requiredStock.getRAM_8Gb() + phoneAmount);
					}
					
					if (thisPhone.getStorage() == 0) {
						requiredStock.setStorage_64Gb(requiredStock.getStorage_64Gb() + phoneAmount);
					}
					else {
						requiredStock.setStorage_256Gb(requiredStock.getStorage_256Gb() + phoneAmount);
					}
					
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
									todaysProfit -= invoice.getCost();
									partsConfirmed = true;
									requiredStock = new PartsList();
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
			
			int canBuildToday = 50;
			int ordersRemainingPhones = todaysOrder.getQuantity();
			
			while (todaysOrder.getCustomer() != null && canBuildToday != 0){

				boolean allPartsAvailable = true;
				PhoneSpecification currentPhone = todaysOrder.getPhone();
				
				// Same case as in "DecideOrders", can be easily modified if there are 
				//	more than two types of any given part.
				if (currentPhone.getScreen() == 0) {
					if (currentStock.getScreen_5inch() != 0) {
						currentStock.setScreen_5inch(currentStock.getScreen_5inch() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				else {
					if (currentStock.getScreen_7inch() != 0) {
						currentStock.setScreen_7inch(currentStock.getScreen_7inch() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				
				if (currentPhone.getBattery() == 0) {
					if (currentStock.getBattery_2000mAh() != 0) {
						currentStock.setBattery_2000mAh(currentStock.getBattery_2000mAh() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				else {
					if (currentStock.getBattery_3000mAh() != 0) {
						currentStock.setBattery_3000mAh(currentStock.getBattery_3000mAh() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				
				if (currentPhone.getRAM() == 0) {
					if (currentStock.getRAM_4Gb() != 0) {
						currentStock.setRAM_4Gb(currentStock.getRAM_4Gb() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				else {
					if (currentStock.getRAM_8Gb() != 0) {
						currentStock.setRAM_8Gb(currentStock.getRAM_8Gb() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				
				if (currentPhone.getStorage() == 0) {
					if (currentStock.getStorage_64Gb() != 0) {
						currentStock.setStorage_64Gb(currentStock.getStorage_64Gb() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				else {
					if (currentStock.getStorage_256Gb() != 0) {
						currentStock.setStorage_256Gb(currentStock.getStorage_256Gb() -1);
					}
					else {
						allPartsAvailable = false;
					}
				}
				
				if (allPartsAvailable) {
					ordersRemainingPhones--;
				}
				
				//Iterates down since an attempt was made to get a phone built.
				canBuildToday--;
				
				if(ordersRemainingPhones == 0) {
					
					AID customer = todaysOrder.getCustomer();
					
					ACLMessage orderComplete = new ACLMessage(ACLMessage.INFORM);
					orderComplete.addReceiver(customer);
					orderComplete.setContent("order completed");
					myAgent.send(orderComplete);
					
					todaysProfit += (todaysOrder.getPrice() * todaysOrder.getQuantity());
					
					//Apply late fees to phones with less than 0 days remaining.
					if (todaysOrder.getDays() < 0) {
						todaysProfit -= (todaysOrder.getPenalty() *(todaysOrder.getDays()* -1));
					}
					
					//Remove order 0 (todaysOrder) as it has been handled.
					allOrders.remove(0);
	
					//Get rid of todaysOrder as it has been completed, allowing the loop
					//	to end early if it has not yet hit 50 phones built today.
					todaysOrder = new Order();
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
			//Apply overnight parts storage costs.
			todaysProfit -= todaysWarehouseCost();
			totalProfit += todaysProfit;
			
			//Display the earnings for this day.
			System.out.println("Todays earnings: �" + todaysProfit);
			
			//Display total earnings over the whole simulation.
			System.out.println("Total earnings: �" + totalProfit);	
			
			//Set all daily variables back to default.
			todaysCustomers = 0;
			todaysProfit = 0;
			step = 0;
			partsConfirmed = false;
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(systemTicker);
			msg.setContent("done");
			myAgent.send(msg);
		}
	}	
}
