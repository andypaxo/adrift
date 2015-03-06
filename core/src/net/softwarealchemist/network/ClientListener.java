package net.softwarealchemist.network;

import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;

public interface ClientListener {
	public void configurationReceived(TerrainConfig configuration);
	public void setPlayerId(int playerId);
	public Entity getPlayer();
	public void updateEntity(Entity entity);
	public Entity getEntityById(int id);
	
	public void performPickup(int playerId, int objectId);
}
