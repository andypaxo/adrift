package net.softwarealchemist.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.Stage;
import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.events.Event;

import com.badlogic.gdx.Gdx;

public class AdriftServer {

	private TerrainConfig configuration;
	private ServerSocket serverSocket;
	private Stage stage;
	private ScheduledThreadPoolExecutor scheduler;
	private List<ServerConnection> connections;

	public void setConfiguration(TerrainConfig configuration) {
		this.configuration = configuration;
		connections = new ArrayList<ServerConnection>();
		scheduler = new ScheduledThreadPoolExecutor(4);
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
				Socket socket = serverSocket.accept();
				// TODO These objects really need to be cleaned up on shutdown
				ServerConnection connection = new ServerToClientConnection(socket, stage, configuration, this);
				addClient(connection); 
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}

	public void addClient(ServerConnection connection) {
		new Thread(() -> connection.listen()).start();
		scheduler.scheduleAtFixedRate(() -> connection.send(), 500, 500, TimeUnit.MILLISECONDS);
		connections.add(connection);
	}
	
	public void relayEvents(List<Event> events, ServerConnection sender) {
		if (events.size() > 0)
			Hud.log("Received events");
		for (ServerConnection connection : connections)
			if (connection != sender)
				connection.addEvents(events);
	}

	public void dispose() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
