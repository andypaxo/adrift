package net.softwarealchemist.adrift.entities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import net.softwarealchemist.adrift.events.Event;
import net.softwarealchemist.adrift.events.PickupEvent;

public class Relic extends Entity implements Serializable {
	private static final long serialVersionUID = -4110042349438733422L;
	private RelicItem item;

	public Relic(RelicItem item) {
		super();
		canBeCollected = true;
		this.item = item;
	}

	@Override
	public String getName() {
		return item.name;
	}

	@Override
	public List<Item> dropItems() {
		return Arrays.asList(item);
	}

	@Override
	public Event[] onTouchPlayer(PlayerCharacter player) {
		return new Event[] { new PickupEvent(player.id, id) };
	}
	
}
