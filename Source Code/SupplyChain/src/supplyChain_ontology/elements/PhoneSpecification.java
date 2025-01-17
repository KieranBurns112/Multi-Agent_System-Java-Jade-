package supplyChain_ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class PhoneSpecification implements Concept {
	//	Local versions of variables used in get-set.
	private int _screen;
	private int _battery;
	private int _ram;
	private int _storage;
	
	//	Get and Set Screen variable.
	@Slot (mandatory = true)
	public int getScreen() {
		return _screen;
	}
	
	public void setScreen(int newScreen) {
		this._screen = newScreen;
	}
	
	//	Get and Set Battery variable.
	@Slot (mandatory = true)
	public int getBattery() {
		return _battery;
	}
	
	public void setBattery(int newBattery) {
		this._battery = newBattery;
	}
	
	//	Get and Set RAM variable.
	@Slot (mandatory = true)
	public int getRAM() {
		return _ram;
	}
	
	public void setRAM(int newRAM) {
		this._ram = newRAM;
	}
	
	//	Get and Set Storage variable.
	@Slot (mandatory = true)
	public int getStorage() {
		return _storage;
	}
	
	public void setStorage(int newStorage) {
		this._storage = newStorage;
	}
}
