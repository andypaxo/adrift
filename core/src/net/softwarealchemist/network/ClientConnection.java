package net.softwarealchemist.network;

import net.softwarealchemist.adrift.dto.StateUpdate;

public interface ClientConnection {
	void send(StateUpdate update);
	void setClient(AdriftClient client);
	void open();
	void dispose();
}
