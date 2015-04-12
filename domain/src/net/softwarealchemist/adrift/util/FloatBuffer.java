package net.softwarealchemist.adrift.util;

public class FloatBuffer {
	public float[] buffer;
	public int length;

	public FloatBuffer(int capacity) {
		buffer = new float[capacity];
	}
	
	public void reset() {
		length = 0;
	}
	
	public FloatBuffer add(float... values) {
		for (float value : values)
			buffer[length++] = value;
		return this;
	}
	
}
