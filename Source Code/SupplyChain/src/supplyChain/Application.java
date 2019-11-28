package supplyChain;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Application {

	public static void main(String[] args) {
		
		//parameter used to define how many customers are used in the system (default = 3)
		int customers = 3; //"c"
		
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try {
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			AgentController manufacturer = myContainer.createNewAgent("manufacturer", Manufacturer.class.getCanonicalName(), null);
			manufacturer.start();
			
			AgentController customer;
			for (int i = 0; i < customers; i++)
			{
				customer = myContainer.createNewAgent("customer" + (i+1), Customer.class.getCanonicalName(), null);
				customer.start();
			}
			
			AgentController supplier;
			for (int i = 0; i < 2; i++)		
			{
				supplier = myContainer.createNewAgent("supplier" + (i+1), Supplier.class.getCanonicalName(), null);
				supplier.start();
			}
			
			AgentController systemTicker = myContainer.createNewAgent("ticker", SystemTicker.class.getCanonicalName(), null);
			systemTicker.start();
			
		}
		catch(Exception e) {
			System.out.println("Error initialising agents");
		}
	}	
}
