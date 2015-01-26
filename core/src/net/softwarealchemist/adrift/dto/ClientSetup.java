package net.softwarealchemist.adrift.dto;

import java.io.Serializable;

public class ClientSetup implements Serializable {
	private static final long serialVersionUID = 1602916704063907401L;
	
	public TerrainConfig terrainConfig;
	public int playerId;
	
	public ClientSetup(TerrainConfig terrainConfig, int playerId) {
		super();
		this.terrainConfig = terrainConfig;
		this.playerId = playerId;
	}
}
