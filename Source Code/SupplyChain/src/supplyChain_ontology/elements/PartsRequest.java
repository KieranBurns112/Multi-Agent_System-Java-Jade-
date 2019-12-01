package supplyChain_ontology.elements;

import jade.content.Predicate;
import jade.core.AID ;

public class PartsRequest implements Predicate {
	private AID _manufacturer;
	private PartsList _parts;
	
	public AID getManufacturer() {
		return _manufacturer;
	}
	
	public void setManufacturer(AID newManufacturer) {
		this._manufacturer = newManufacturer;
	}
	
	public PartsList getParts() {
		return _parts;
	}
	
	public void setParts(PartsList newParts) {
		this._parts = newParts;
	}
}
