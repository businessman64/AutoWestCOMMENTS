package model;

public class BloodPressure {
	private int low;
	private int high;
	public BloodPressure(int low,int high) {
		this.low=low;
		this.high = high;
	}
	public int getLow() {
		return low;
	}
	public int getHigh() {
		return this.high;
	}
	public void setLow(int low) {
		this.low = low;
	}
	public void setHigh(int high) {
		this.high = high;
	}

}
