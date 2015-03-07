package net.softwarealchemist.network;

import net.softwarealchemist.adrift.dto.StateUpdate;

public class ClientToLocalConnection implements ClientConnection {
	
	ServerToLocalConnection serverConnection;

	public void setServerConnection(ServerToLocalConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	@Override
	public void send(StateUpdate update) {
		if (update.getEvents().size() > 0)
			serverConnection.relayEvents(update.getEvents());
	}

	@Override
	public void setClient(AdriftClient client) {
		// TODO Auto-generated method stub

	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
