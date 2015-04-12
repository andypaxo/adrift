package net.softwarealchemist.adrift.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;
import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.events.Event;
import net.softwarealchemist.adrift.model.Zone;
import net.softwarealchemist.adrift.server.AdriftServer;

public class ServerToClientConnection implements ServerConnection {
	private TerrainConfig configuration;
	private Socket socket;
	private Zone stage;
	private ObjectOutputStream output;
	private AdriftServer server;
	private List<Event> eventsToSend;
	private boolean isDisposed;
	private ObjectInputStream clientInput;

	public ServerToClientConnection(Socket socket, Zone stage, TerrainConfig configuration, AdriftServer server) throws IOException {
		this.socket = socket;
		this.stage = stage;
		this.configuration = configuration;
		this.server = server;
		eventsToSend = new ArrayList<Event>();
		output = new ObjectOutputStream(socket.getOutputStream());
	}

	public void listen() {
		try {
			clientInput = new ObjectInputStream(socket.getInputStream());
			
			output.writeObject(new ClientSetup(configuration, stage.getNextId()));
			
			while (!isDisposed) {
				Object obj = clientInput.readObject();
				if (obj instanceof StateUpdate) {
					synchronized (stage) {
						StateUpdate update = (StateUpdate) obj;
						for (Entity entity : update.getUpdatedEntities()) {
							stage.updateEntity(entity);
						}
						server.relayEvents(update.getEvents(), this);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Client disconnected");
			dispose();
		}
	}
	
	public void send() {
		if (isDisposed)
			return;
		
		try {
			ArrayList<Entity> updatedEntities = new ArrayList<Entity>(stage.entities.values());
			output.writeObject(new StateUpdate(updatedEntities, eventsToSend));
			output.reset();
			eventsToSend.clear();
		} catch (Exception e) {
			System.out.println("Communication problem. " + e.getClass() + " : " + e.getMessage());
			dispose();
		}
	}

	@Override
	public void addEvents(List<Event> events) {
		eventsToSend.addAll(events);
	}
	
	public void dispose() {
		isDisposed = true;
		try {
			clientInput.close();
			output.close();
		} catch (IOException e) {
			// Well, can't do much now
		}
	}
}
