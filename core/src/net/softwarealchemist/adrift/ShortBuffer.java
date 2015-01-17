package net.softwarealchemist.adrift;

public class ShortBuffer {
	public short[] buffer;
	public int length;

	public ShortBuffer(int capacity) {
		buffer = new short[capacity];
	}
	
	public void reset() {
		length = 0;
	}
	
	public ShortBuffer add(short... values) {
		for (short value : values)
			buffer[length++] = value;
		return this;
	}
	
}
