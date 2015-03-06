package net.softwarealchemist.adrift.events;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.network.ClientListener;

public class PickupEvent implements Event, Serializable {
	
	private static final long serialVersionUID = 8591479267786342762L;
	public int playerId, objectId;
	
	public PickupEvent(int playerId, int objectId) {
		this.playerId = playerId;
		this.objectId = objectId;
	}

	@Override
	public void execute(ClientListener listener) {
		listener.performPickup(playerId, objectId);
	}

	@Override
	public List<Event> executeServer(ClientListener listener) {
		Entity collectedEntity = listener.getEntityById(objectId);
		return Arrays.asList(new MessageEvent(playerId, "Collected " + collectedEntity.name));
	}
	
}
