package net.softwarealchemist.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import net.softwarealchemist.adrift.Entity;
import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.Stage;
import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;
import net.softwarealchemist.adrift.dto.TerrainConfig;

import com.badlogic.gdx.Gdx;

public class AdriftServer {

	private TerrainConfig configuration;
	private ServerSocket serverSocket;
	private Stage stage;

	public void setConfiguration(TerrainConfig configuration) {
		this.configuration = configuration;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void start() {
		new Thread(() -> listen()).start();
	}

	private void listen() {
		try {
			serverSocket = new ServerSocket(10537);
			while (true) {
				final Socket socket = serverSocket.accept();
				new Thread(() -> listenTo(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}

	// One of these methods will be instantiated for each client. Maybe make it a class.
	private void listenTo(Socket socket) {
		try {
			final ObjectOutputStream clientOutput = new ObjectOutputStream(socket.getOutputStream());
			final ObjectInputStream clientInput = new ObjectInputStream(socket.getInputStream());
			
			clientOutput.writeObject(new ClientSetup(configuration, stage.getNextId()));
			
			while (true) {
				Object obj = clientInput.readObject();
				if (obj instanceof StateUpdate) {
					synchronized (stage) {
						for (Entity entity : ((StateUpdate) obj).getUpdatedEntities()) {
							Hud.log(String.format(entity.name + " position updated to " + entity.position));
							stage.updateEntity(entity);
						}	
					}
				}
			}
		} catch (Exception e) {
			Hud.log("Client disconnected");
		}
	}

	public void dispose() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
