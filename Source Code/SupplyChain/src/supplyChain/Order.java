package supplyChain;

public class Order {
	//	Local versions of variables used in get-set.
	private PhoneSpecification _phone;
	private int _quantity;
	private int _days;
	private int _price;
	private int _penalty;
	
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
}
