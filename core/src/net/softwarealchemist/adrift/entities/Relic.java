package net.softwarealchemist.adrift.entities;

import java.util.Arrays;
import java.util.List;

public class Relic extends Entity {
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
	
}
