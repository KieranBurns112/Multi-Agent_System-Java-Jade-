package supplyChain;

import java.util.ArrayList;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SystemTicker extends Agent{
	
	//The number of simulated days the system will run for.
	public static final int simulationDays = 100; 
	
	@Override
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ticker-agent");
		sd.setName(getLocalName() + "-ticker-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException e) {
			e.printStackTrace();
		}
		
		doWait(5000);
		addBehaviour(new SynchroniseAgents(this));
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
	
	public class SynchroniseAgents extends Behaviour {
		
		private int step = 0;
		private ArrayList<AID> allAgents = new ArrayList<>();
		private int currentDay = 0;
		private int finishedMessages = 0;
		
		public SynchroniseAgents(Agent agent) {
			super(agent);
		}
		
		@Override
		public void action() {
			switch(step) {
			case 0:
					DFAgentDescription customerAD = new DFAgentDescription();
					ServiceDescription customerSD = new ServiceDescription();
					customerSD.setType("customer");
					customerAD.addServices(customerSD);
					
					DFAgentDescription manufacturerAD = new DFAgentDescription();
					ServiceDescription manufacturerSD = new ServiceDescription();
					manufacturerSD.setType("manufacturer");
					manufacturerAD.addServices(manufacturerSD);
					
					DFAgentDescription supplierAD = new DFAgentDescription();
					ServiceDescription supplierSD = new ServiceDescription();
					supplierSD.setType("supplier");
					supplierAD.addServices(supplierSD);
					
					try {
						DFAgentDescription[] allCustomers = DFService.search(myAgent, customerAD);				
						for (int i = 0; i < allCustomers.length; i++) {
							allAgents.add(allCustomers[i].getName());
						}
						
						DFAgentDescription[] allManufacturers = DFService.search(myAgent, manufacturerAD);					
						for (int i = 0; i < allManufacturers.length; i++) {
							allAgents.add(allManufacturers[i].getName());
						}
						
						DFAgentDescription[] allSuppliers = DFService.search(myAgent, supplierAD);
						for (int i = 0; i < allSuppliers.length; i++) {
							allAgents.add(allSuppliers[i].getName());
						}		
					}
					catch(FIPAException e) {
						e.printStackTrace();
					}
					
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					tick.setContent("new day");
					for (AID agent : allAgents) {
						tick.addReceiver(agent);
					}
					myAgent.send(tick);
					
					step++;
					currentDay++;
					break;
				
			case 1:
					MessageTemplate mt = MessageTemplate.MatchContent("done");
					ACLMessage msg = myAgent.receive(mt);
					if (msg != null) {
						finishedMessages++;
						if (finishedMessages >= allAgents.size()) {
							step++;
						}
					}
					else {
						block();
					}
			}
		}
		
		@Override
		public boolean done() {
			return step == 2;
		}
		
		@Override
		public void reset() {
			super.reset();
			step = 0;
			allAgents.clear();
			finishedMessages = 0;
		}
		
		@Override
		public int onEnd() {
			System.out.println("End of day " + currentDay);
			
			if (currentDay == simulationDays) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("terminate");
				
				for (AID agent : allAgents) {
					msg.addReceiver(agent);
				}
				
				myAgent.send(msg);
				myAgent.doDelete();
			}
			else {
				//Spacing for display cleanliness.
				System.out.println("");
				
				reset();
				myAgent.addBehaviour(this);
			}
			
			return 0;
		}
	}		
}
