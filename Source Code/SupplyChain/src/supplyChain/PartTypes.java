package supplyChain;

import java.util.ArrayList;

//This class is used to translate the numerical forms the parts are stored as
// within the system into plain English.
public class PartTypes {
	// These are the text forms of the different available parts for
	//	each phone, they are stored here to be modified if needed.
	private String[] screens = new String[] {"5\" Screen", "7\" Screen"};
	private String[] batteries = new String[] {"2000mAh Battery", "3000mAh Battery"};
	private String[] ram = new String[] {"4Gb ram", "8Gb ram"};
	private String[] storages= new String[] {"64Gb Storage", "256Gb Storage"};
	
	//	Method to request the list of Screen types.
	public String[] ListScreens() {
		return screens;
	}
	
	//	Method to request the list of Battery types.
	public String[] ListBatteries() {
		return batteries;
	}
	
	//	Method to request the list of ram types.
	public String[] ListRAM() {
		return ram;
	}
	
	//	Method to request the list of Storage types.
	public String[] ListStorage() {
		return storages;
	}
}
