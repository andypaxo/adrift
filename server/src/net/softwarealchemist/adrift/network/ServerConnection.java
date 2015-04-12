package net.softwarealchemist.adrift.network;

import java.util.List;

import net.softwarealchemist.adrift.events.Event;

public interface ServerConnection {

	void listen();
	void send();
	void addEvents(List<Event> events);

}
