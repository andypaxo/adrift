package net.softwarealchemist.network;

import net.softwarealchemist.adrift.dto.TerrainConfig;

public interface ClientListener {
	public void ConfigurationReceived(TerrainConfig configuration);
}
