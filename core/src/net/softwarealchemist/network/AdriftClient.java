package net.softwarealchemist.network;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;
import net.softwarealchemist.adrift.entities.Entity;

public class AdriftClient {

	private ClientListener listener;
	private ScheduledThreadPoolExecutor scheduler;
	private ClientConnection connection;

	public AdriftClient(ClientConnection connection, ClientListener listener) {
		this.connection = connection;
		this.listener = listener;
		connection.setClient(this);
	}

	public void start() {
		new Thread(() -> connection.open()).start();
		scheduler = new ScheduledThreadPoolExecutor(1);
		scheduler.scheduleAtFixedRate(() -> send(), 500, 500, TimeUnit.MILLISECONDS); 
	}
	
	public void objectReceived(Object received)
	{
		if (received instanceof ClientSetup) {
			final ClientSetup clientSetup = (ClientSetup) received;
			listener.configurationReceived(clientSetup.terrainConfig);
			listener.setPlayerId(clientSetup.playerId);
		} else if (received instanceof StateUpdate) {
			Entity localPlayer = listener.getPlayer();
			for (Entity entity : ((StateUpdate) received).getUpdatedEntities()) {
				if (entity.id != localPlayer.id)
					listener.updateEntity(entity);
			}
		}
	}
	
	private void send() {
		Entity localPlayer = listener.getPlayer();
		ArrayList<Entity> updatedEntities = new ArrayList<Entity>();
		updatedEntities.add(localPlayer);
		connection.send(new StateUpdate(updatedEntities));
	}

	public void dispose() {
		connection.dispose();
		scheduler.shutdown();
	}

	public void stop() {
		dispose();
	}

}
