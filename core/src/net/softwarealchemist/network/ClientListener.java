package net.softwarealchemist.network;

import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.PlayerCharacter;

public interface ClientListener {
	public void configurationReceived(TerrainConfig configuration);
	public void setPlayerId(int playerId);
	public PlayerCharacter getPlayer();
	public void updateEntity(Entity entity);
	public Entity getEntityById(int id);
	
	public void performPickup(int playerId, int objectId);
	public void activateRelicSlot(int relicId);
}
