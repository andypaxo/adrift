package net.softwarealchemist.adrift.events;

import java.util.List;

import net.softwarealchemist.network.ClientListener;

public interface Event {
	public void execute(ClientListener listener);
	public List<Event> executeServer(ClientListener listener);
}
