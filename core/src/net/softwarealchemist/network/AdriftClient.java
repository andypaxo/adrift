package net.softwarealchemist.network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.StateUpdate;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.events.Event;

public class AdriftClient {

	private ClientListener listener;
	private ScheduledThreadPoolExecutor scheduler;
	private ClientConnection connection;
	private List<Event> eventsToSend;

	public AdriftClient(ClientConnection connection, ClientListener listener) {
		this.connection = connection;
		this.listener = listener;
		eventsToSend = new ArrayList<Event>();
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
			StateUpdate update = (StateUpdate) received;
			for (Entity entity : update.getUpdatedEntities()) {
				if (entity.id != localPlayer.id)
					listener.updateEntity(entity);
			}
			for (Event event : update.getEvents()) {
				event.execute(listener);
			}
		}
	}
	
	private void send() {
		Entity localPlayer = listener.getPlayer();
		ArrayList<Entity> updatedEntities = new ArrayList<Entity>();
		updatedEntities.add(localPlayer);
		connection.send(new StateUpdate(updatedEntities, eventsToSend));
		eventsToSend.clear();
	}

	public void dispose() {
		connection.dispose();
		scheduler.shutdown();
	}

	public void stop() {
		dispose();
	}

	public void addEvent(Event event) {
		eventsToSend.add(event);
	}

}
