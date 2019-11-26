package supplyChain;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Application {

	public static void main(String[] args) {
		
		//each of the parameters required when starting the application
		int days = 100;
		int customers = 3; //"c"
		
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try {
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			// start manufacturer agent
			
			// start customer agents (number from customers variables)
			
			// start both suppliers
		}
		catch(Exception e) {
			System.out.println("Error initialising agents");
		}
	}	
}
