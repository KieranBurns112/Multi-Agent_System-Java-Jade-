package supplyChain_ontology.elements;

import jade.content.Concept;

public class PartsList implements Concept {
	//All part quantities = 0 when first instantiated.
	private int _screen_5inch = 0;
	private int _screen_7inch = 0;
	private int _storage_64Gb = 0;
	private int _storage_256Gb = 0;
	private int _ram_4Gb = 0;
	private int _ram_8Gb = 0;
	private int _battery_2000mAh = 0;
	private int _battery_3000mAh = 0;
	
	public int getScreen_5inch() {
		return _screen_5inch;
	}
	
	public void setScreen_5inch(int newScreen_5inch) {
		this._screen_5inch = newScreen_5inch;
	}
	
	public int getScreen_7inch() {
		return _screen_7inch;
	}
	
	public void setScreen_7inch(int newScreen_7inch) {
		this._screen_7inch = newScreen_7inch;
	}
	
	public int getStorage_64Gb() {
		return _storage_64Gb;
	}
	
	public void setStorage_64Gb(int newStorage_64Gb) {
		this._storage_64Gb = newStorage_64Gb;
	}
	
	public int getStorage_256Gb() {
		return _storage_256Gb;
	}
	
	public void setStorage_256Gb(int newStorage_256Gb) {
		this._storage_256Gb = newStorage_256Gb;
	}
	
	public int getRAM_4Gb() {
		return _ram_4Gb;
	}
	
	public void setRAM_4Gb(int newRAM_4Gb) {
		this._ram_4Gb = newRAM_4Gb;
	}
	
	public int getRAM_8Gb() {
		return _ram_8Gb;
	}
	
	public void setRAM_8Gb(int newRAM_8Gb) {
		this._ram_8Gb = newRAM_8Gb;
	}
	
	public int getBattery_2000mAh() {
		return _battery_2000mAh;
	}
	
	public void setBattery_2000mAh(int newBattery_2000mAh) {
		this._battery_2000mAh = newBattery_2000mAh;
	}
	
	public int getBattery_3000mAh() {
		return _battery_3000mAh;
	}
	
	public void setBattery_3000mAh(int newBattery_3000mAh) {
		this._battery_3000mAh = newBattery_3000mAh;
	}
}
