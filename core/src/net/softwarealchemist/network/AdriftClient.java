package net.softwarealchemist.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.softwarealchemist.adrift.Entity;
import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;

public class AdriftClient {

	private InetAddress server;
	private ClientListener listener;
	private boolean isDisposed;
	private ScheduledThreadPoolExecutor scheduler;
	private Socket socket;
	private ObjectOutputStream output;

	public AdriftClient(InetAddress server, ClientListener listener) {
		this.server = server;
		this.listener = listener;
	}

	public void start() {
		new Thread(() -> receive()).start();
		scheduler = new ScheduledThreadPoolExecutor(1);
		scheduler.scheduleAtFixedRate(() -> send(), 500, 500, TimeUnit.MILLISECONDS); 
	}
	
	private void receive() {		
		try {
			socket = new Socket(server, 10537);
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
			
			Object received;
			while (!isDisposed) {
				received = inputStream.readObject();
				if (received instanceof ClientSetup) {
					final ClientSetup clientSetup = (ClientSetup) received;
					listener.configurationReceived(clientSetup.terrainConfig);
					listener.setPlayerId(clientSetup.playerId);
				}
			}
			
			output.close();
			inputStream.close();
			socket.close();
		} catch (Exception e) {
			Hud.log("Host disconnected. You're on your own now!");
			scheduler.shutdown();
		}
	}
	
	private void send() {	
		try {
			Entity localPlayer = listener.getPlayer();
			ArrayList<Entity> updatedEntities = new ArrayList<Entity>();
			updatedEntities.add(localPlayer);
			Hud.log("Player position " + updatedEntities.get(0).position);
			output.writeObject(new StateUpdate(updatedEntities));
			output.reset();
		} catch (Exception e) {
			Hud.log("Communication problem : " + e.getMessage());
		}
	}

	public void dispose() {
		isDisposed = true;
		scheduler.shutdown();
	}

}
