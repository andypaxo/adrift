package net.softwarealchemist.network;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.Stage;
import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;
import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.events.Event;

public class ServerToLocalConnection implements ServerConnection {

	private AdriftClient client;
	private TerrainConfig configuration;
	private AdriftServer server;

	public ServerToLocalConnection(AdriftClient client, Stage stage, TerrainConfig configuration, AdriftServer server) {
		this.client = client;
		this.configuration = configuration;
		this.server = server;
	}
	
	@Override
	public void listen() {
		client.objectReceived(new ClientSetup(configuration, 0));
	}

	@Override
	public void send() {
		// TODO Auto-generated method stub

	}
	
	public void relayEvents(List<Event> events) {
		server.relayEvents(events, this);
	}
	
	@Override
	public void addEvents(List<Event> events) {
		if (events.size() > 0) {
			StateUpdate update = new StateUpdate(new ArrayList<Entity>(), events);
			client.objectReceived(update);
		}
	}

}
