package net.softwarealchemist.network;

import net.softwarealchemist.adrift.TerrainConfig;

public interface ClientListener {
	public void ConfigurationReceived(TerrainConfig configuration);
}
