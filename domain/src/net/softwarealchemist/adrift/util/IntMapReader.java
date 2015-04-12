package net.softwarealchemist.adrift.util;

public class IntMapReader {
	int width, depth, height, defaultValue;
	int[] values;
	
	

	public IntMapReader(int width, int depth, int height, int defaultValue, int[] values) {
		this.width = width;
		this.depth = depth;
		this.height = height;
		this.defaultValue = defaultValue;
		this.values = values;
	}

	public int getInt(int x, int y, int z) {
		if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
			return defaultValue;
		return values[y * width * depth + z * width + x];
	}

	public void setInt(int x, int y, int z, int val) {
		values[y * width * depth + z * width + x] = val;
	}
}
