package supplyChain;

public class Supplier2_info {
	
	private String[] items = {"4Gb RAM", "8Gb RAM", "64Gb Storage", "256Gb Storage"};
	private int[] prices = {20,35,15,40};
	private int deliveryDays = 4;
	
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