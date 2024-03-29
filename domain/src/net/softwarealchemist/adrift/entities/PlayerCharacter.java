package net.softwarealchemist.adrift.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerCharacter extends Entity {
	private static final long serialVersionUID = 3619377619510809667L;
	
	private List<Item> inventory;

	public PlayerCharacter(String name) {
		this.name = name;
		inventory = new ArrayList<Item>();
	}

	public void addToInventory(List<Item> items) {
		inventory.addAll(items);
	}
	
	public List<Item> getInventory() {
		return inventory.stream().collect(Collectors.toList());
	}

	public void removeFromInventory(Item item) {
		inventory.remove(item);
		System.out.println(String.format("Removed %s from %s", item.getName(), name));
		System.out.println(String.format("%s inventory : %s", name, describeInventory()));
	}

	public String describeInventory() {
		return inventory.stream()
			.map((item) -> item.getName())
			.reduce("", (prev, current) -> prev + " " + current);
	}
}
