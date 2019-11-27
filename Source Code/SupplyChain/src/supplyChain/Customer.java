package supplyChain;

import jade.core.Agent;

public class Customer extends Agent {
	protected void setup() {
		
		//Create order with all details to send to manufacturer.
		Order order = generateOrder(); 
		
		//!! Test to check if order works !!
		///
		///
		
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

		//
		//
		// !! End of Test !!
		
		
		//SEND ORDER TO MANUFACTURER.
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
}
