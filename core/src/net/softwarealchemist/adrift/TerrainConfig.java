package net.softwarealchemist.adrift;

import java.io.Serializable;

public class TerrainConfig implements Serializable {
	private static final long serialVersionUID = -2510983042273570673L;
	
	public int height;
	public double seed;
	public double noiseScale;
	public double caveScale;
	public double caveStretch;

	public TerrainConfig() {
	}
}