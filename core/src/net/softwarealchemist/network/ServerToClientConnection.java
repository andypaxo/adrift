package net.softwarealchemist.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.Stage;
import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;
import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;

public class ServerToClientConnection {
	private TerrainConfig configuration;
	private Socket socket;
	private Stage stage;
	private ObjectOutputStream output;

	public ServerToClientConnection(Socket socket, Stage stage, TerrainConfig configuration) throws IOException {
		this.socket = socket;
		this.stage = stage;
		this.configuration = configuration;
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
						for (Entity entity : ((StateUpdate) obj).getUpdatedEntities()) {
							stage.updateEntity(entity);
						}	
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
			output.writeObject(new StateUpdate(updatedEntities));
			output.reset();
		} catch (Exception e) {
			Hud.log("Communication problem : " + e.getMessage());
		}
	}
}
