package supplyChain_ontology.elements;

import jade.content.Predicate;
import jade.core.AID ;

public class Order implements Predicate {
	//	Local versions of variables used in get-set.
	private AID _customer;
	private PhoneSpecification _phone;
	private int _quantity;
	private int _days;
	private int _price;
	private int _penalty;
	
	//False by default, could be sent as true and no difference would be made.
	//
	//This is used by the Manufacturer as a way of tracking which orders have
	// been accepted and which have not.
	private boolean _accepted = false;
	
	//	Get and Set the customer variable, 
	//  used to find the customer who purchased the phone.
	public AID getCustomer() {
		return _customer;
	}
	
	public void setCustomer(AID newCustomer) {
		this._customer = newCustomer;
	}
	
	//	Get and Set Phone variable.
	public PhoneSpecification getPhone() {
		return _phone;
	}
	
	public void setPhone(PhoneSpecification newPhone) {
		this._phone = newPhone;
	}
	
	//	Get and Set Quantity variable.
	public int getQuantity() {
		return _quantity;
	}
	
	public void setQuantity(int newQuantity) {
		this._quantity = newQuantity;
	}
	
	//	Get and Set Days variable.
	public int getDays() {
		return _days;
	}
	
	public void setDays(int newDays) {
		this._days = newDays;
	}
	
	//	Get and Set Price variable.
	public int getPrice() {
		return _price;
	}
	
	public void setPrice(int newPrice) {
		this._price = newPrice;
	}
	
	//	Get and Set Penalty variable.
	public int getPenalty() {
		return _penalty;
	}
	
	public void setPenalty(int newPenalty) {
		this._penalty = newPenalty;
	}
	
	//	Get and Set Manufacturer acception variable.
	public boolean getAccepted() {
		return _accepted;
	}
	
	public void setAccepted(boolean newAccepted) {
		this._accepted = newAccepted;
	}
}
