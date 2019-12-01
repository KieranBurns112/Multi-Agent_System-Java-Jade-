package supplyChain_ontology.elements;

import jade.content.Predicate;

public class PartsInvoice implements Predicate {
	private PartsList _parts;
	private int _cost;
	private int _dueDays;
	
	public PartsList getParts() {
		return _parts;
	}
	
	public void setParts(PartsList newParts) {
		this._parts = newParts;
	}
	
	public int getCost() {
		return _cost;
	}
	
	public void setCost(int newCost) {
		this._cost = newCost;
	}
	
	public int getDueDays() {
		return _dueDays;
	}
	
	public void setDueDays(int newDueDays) {
		this._dueDays = newDueDays;
	}
}
