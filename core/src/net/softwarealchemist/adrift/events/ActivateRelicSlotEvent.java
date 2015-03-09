package net.softwarealchemist.adrift.events;

import java.io.Serializable;
import java.util.List;

import net.softwarealchemist.network.ClientListener;

public class ActivateRelicSlotEvent implements Event, Serializable {

	private static final long serialVersionUID = 3268908742643133094L;

	int relicId;
	
	public ActivateRelicSlotEvent(int relicId) {
		this.relicId = relicId;
	}

	@Override
	public void execute(ClientListener listener) {
		listener.activateRelicSlot(relicId);
	}

	@Override
	public List<Event> executeServer(ClientListener listener) {
		return null;
	}

}
