package net.softwarealchemist.adrift.events;

import java.io.Serializable;
import java.util.List;

import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.PlayerCharacter;
import net.softwarealchemist.network.ClientListener;

public class ItemGetEvent implements Event, Serializable {

	private static final long serialVersionUID = -7782441405430008088L;
	
	public int playerId, objectId;

	public ItemGetEvent(int playerId, int objectId) {
		this.playerId = playerId;
		this.objectId = objectId;
	}
	
	@Override
	public void execute(ClientListener listener) {
		PlayerCharacter player = listener.getPlayer();
		if (player.id == playerId) {
			Entity entity = listener.getEntityById(objectId);
			player.addToInventory(entity.dropItems());
			String inventoryDescription = player
				.getInventory().stream()
				.map((item) -> item.getName())
				.reduce("", (prev, current) -> prev + " " + current);
			Hud.setInfo("Inventory", inventoryDescription);
		}
	}

	@Override
	public List<Event> executeServer(ClientListener listener) {
		return null;
	}

}
