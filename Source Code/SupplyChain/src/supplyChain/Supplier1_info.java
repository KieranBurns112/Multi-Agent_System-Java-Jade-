package supplyChain;

public class Supplier1_info {
	
	private String[] items = {"5\" Screen", "7\" Screen", "2000mAh Battery", "3000mAh Battery", 
			"4Gb RAM", "8Gb RAM", "64Gb Storage", "256Gb Storage"};
	private int[] prices = {100,150,70,100,30,60,25,50};
	private int deliveryDays = 1;
	
	public String[] Items() {
		return items;
	}
	
	public int[] Prices() {
		return prices;
	}
	
	public int DeliveryDays() {
		return deliveryDays;
	}
}
