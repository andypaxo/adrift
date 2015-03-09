package net.softwarealchemist.adrift.entities;

import java.io.Serializable;

import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.events.ActivateRelicSlotEvent;
import net.softwarealchemist.adrift.events.Event;

public class RelicSlot extends Entity implements Serializable {

	private static final long serialVersionUID = -7852585201079593454L;
	
	public boolean isActivated = false;

	@Override
	public Event[] onTouchPlayer(PlayerCharacter player) {
		if (player.getInventory().size() > 0) {
			player.removeFromInventory(player.getInventory().get(0));
			Hud.setInfo("Inventory", player.describeInventory());
			return new Event [] { new ActivateRelicSlotEvent(id) };
		}
		else {
			return super.onTouchPlayer(player);
		}
	}

}
