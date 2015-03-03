package net.softwarealchemist.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.Stage;
import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;
import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.events.Event;

public class ServerToClientConnection implements ServerConnection {
	private TerrainConfig configuration;
	private Socket socket;
	private Stage stage;
	private ObjectOutputStream output;
	private AdriftServer server;
	private List<Event> eventsToSend;

	public ServerToClientConnection(Socket socket, Stage stage, TerrainConfig configuration, AdriftServer server) throws IOException {
		this.socket = socket;
		this.stage = stage;
		this.configuration = configuration;
		this.server = server;
		eventsToSend = new ArrayList<Event>();
		output = new ObjectOutputStream(socket.getOutputStream());
	}

	public void listen() {
		try {
			final ObjectInputStream clientInput = new ObjectInputStream(socket.getInputStream());
			
			output.writeObject(new ClientSetup(configuration, stage.getNextId()));
			
			while (true) {
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
			Hud.log("Client disconnected");
		}
	}
	
	public void send() {	
		try {
			ArrayList<Entity> updatedEntities = new ArrayList<Entity>(stage.entities.values());
			output.writeObject(new StateUpdate(updatedEntities, eventsToSend));
			output.reset();
			eventsToSend.clear();
		} catch (Exception e) {
			Hud.log("Communication problem. " + e.getClass() + " : " + e.getMessage());
		}
	}

	@Override
	public void addEvents(List<Event> events) {
		eventsToSend.addAll(events);
	}
}
