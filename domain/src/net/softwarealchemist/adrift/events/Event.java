package net.softwarealchemist.adrift.events;

import java.util.List;

import net.softwarealchemist.adrift.model.ClientListener;
import net.softwarealchemist.adrift.model.Zone;

public interface Event {
	public void execute(ClientListener listener);
	public List<Event> executeServer(Zone listener);
}
