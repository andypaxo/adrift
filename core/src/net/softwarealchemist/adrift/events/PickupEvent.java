package net.softwarealchemist.adrift.events;

import java.io.Serializable;

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
	
	
}
