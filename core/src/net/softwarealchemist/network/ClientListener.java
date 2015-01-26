package net.softwarealchemist.network;

import net.softwarealchemist.adrift.Entity;
import net.softwarealchemist.adrift.dto.TerrainConfig;

public interface ClientListener {
	public void configurationReceived(TerrainConfig configuration);
	public void setPlayerId(int playerId);
	public Entity getPlayer();
	public void updateEntity(Entity entity);
}
