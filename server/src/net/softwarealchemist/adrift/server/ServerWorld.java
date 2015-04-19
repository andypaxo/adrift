package net.softwarealchemist.adrift.server;

import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.PlayerCharacter;
import net.softwarealchemist.adrift.model.ClientListener;
import net.softwarealchemist.adrift.model.Zone;

public class ServerWorld implements ClientListener {

	private Zone zone;
	
	public ServerWorld(Zone zone) {
		this.zone = zone;
	}

	@Override
	public void configurationReceived(TerrainConfig configuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlayerId(int playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PlayerCharacter getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performPickup(int playerId, int objectId) {
		Entity object = getEntityById(objectId);
		if (object == null)
			return;
		object.deactivate();
	}

	@Override
	public void activateRelicSlot(int relicId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateEntity(Entity entity) {
		zone.updateEntity(entity);
	}

	@Override
	public Entity getEntityById(int id) {
		return zone.getEntityById(id);
	}

	public void step(float delta) {
		zone.step(delta);
		for (Entity entity : zone.entities.values())
			entity.step(delta);
	}
}
