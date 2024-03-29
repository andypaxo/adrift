package net.softwarealchemist.adrift.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.events.Event;
import net.softwarealchemist.adrift.model.Zone;
import net.softwarealchemist.adrift.network.ServerConnection;
import net.softwarealchemist.adrift.network.ServerToClientConnection;

public class AdriftServer {

	private TerrainConfig configuration;
	private ServerSocket serverSocket;
	private ServerWorld world;
	private Zone stage;
	private ScheduledThreadPoolExecutor scheduler;
	private List<ServerConnection> connections;
	long lastTime;

	public void setConfiguration(TerrainConfig configuration) {
		this.configuration = configuration;
		connections = new ArrayList<ServerConnection>();
		scheduler = new ScheduledThreadPoolExecutor(4);
	}
	
	public void setStage(Zone stage) {
		this.stage = stage;
		world = new ServerWorld(stage);
	}

	public void start() {
		new Thread(() -> listen()).start();
		scheduler.scheduleAtFixedRate(() -> updateWorld(), 50, 50, TimeUnit.MILLISECONDS);
		lastTime = System.nanoTime();
		
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
			System.exit(1);
		}
	}

	public void addClient(ServerConnection connection) {
		new Thread(() -> connection.listen()).start();
		scheduler.scheduleAtFixedRate(() -> connection.send(), 100, 100, TimeUnit.MILLISECONDS);
		connections.add(connection);
	}
	
	public void relayEvents(List<Event> events, ServerConnection sender) {
		if (events.size() == 0)
			return;
		List<Event> knockOnEvents = processEvents(events);
		sender.addEvents(knockOnEvents);
		events.addAll(knockOnEvents);
		
		for (Event event : events)
			event.execute(world);
		
		for (ServerConnection connection : connections)
			if (connection != sender)
				connection.addEvents(events);
	}

	private List<Event> processEvents(List<Event> events) {
		List<Event> knockOnEvents = new ArrayList<Event>();
		
		// This is where arbitration / conflict resolution could occur
		for (Event event : events) {
			event.execute(world);
			List<Event> result = event.executeServer(stage);
			if (result != null)
				knockOnEvents.addAll(result);
		}
		
		return knockOnEvents;
	}

	public void dispose() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateWorld() {
		long now = System.nanoTime();
		world.step((float) ((now - lastTime) / 1E9));
		lastTime = now;
	}

}
