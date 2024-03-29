package net.softwarealchemist.adrift.dto;

import java.io.Serializable;

public class TerrainConfig implements Serializable {
	private static final long serialVersionUID = -2510983042273570673L;
	
	public int width, depth, height;
	public double seed;
	public double noiseScale;
	public double caveScale;
	public double caveStretch;

	public TerrainConfig() {
	}
}