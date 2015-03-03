package net.softwarealchemist.adrift.events;

import net.softwarealchemist.network.ClientListener;

public interface Event {
	public void execute(ClientListener listener);
}
