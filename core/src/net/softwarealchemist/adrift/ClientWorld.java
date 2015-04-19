package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.Particle;
import net.softwarealchemist.adrift.entities.PlayerCharacter;
import net.softwarealchemist.adrift.entities.RelicSlot;
import net.softwarealchemist.adrift.events.Event;
import net.softwarealchemist.adrift.model.ClientListener;
import net.softwarealchemist.adrift.model.Terrain;
import net.softwarealchemist.adrift.model.Zone;
import net.softwarealchemist.network.AdriftClient;
import net.softwarealchemist.network.ClientToSocketConnection;

import com.badlogic.gdx.math.Vector3;

public class ClientWorld implements ClientListener {

	public Zone zone;
	private Terrain terrain;
	
	private GameScreen gameScreen;
	private PlayerCharacter player;
		
	private AdriftClient client;
	private ArrayList<Entity> entitiesToAdd;
	
	public ClientWorld(Terrain terrain, GameScreen gameScreen) {
		this.terrain = terrain;
		
		zone = new Zone();
		zone.terrain = terrain;
		
		this.gameScreen = gameScreen;
		zone.entities = new HashMap<Integer, Entity>();
		entitiesToAdd = new ArrayList<Entity>();
	}
	
	public void startAndConnectToServer() {
		final ClientToSocketConnection clientConnection = new ClientToSocketConnection(GameState.server);
		client = new AdriftClient(clientConnection, this);
		client.start();
	}
	
	public void doEvents(float delta) {
		for (Entity entity : zone.localEntities)
			entity.step(delta);
		
		float nearestCollectibleDistance = Float.MAX_VALUE;
		List<Entity> activeEntities = zone.entities.values().stream().filter((entity) -> entity.isActive()).collect(Collectors.toList());
		for (Entity entity : activeEntities) {
				
			
			if (entity.canBeCollected) {
				float distance = player.position.dst(entity.position);
				nearestCollectibleDistance = Math.min(distance, nearestCollectibleDistance);
			}
			
			if (entity.intersectsWith(player)) {
				Event[] events = entity.onTouchPlayer(player);
				for (Event event : events)
				{
					emitEvent(event);
					event.execute(this);
				}
			}
		}
		
		Sounds.setLoopDistance(nearestCollectibleDistance);
		
		for (Entity entity : entitiesToAdd)
			zone.addEntity(entity);
		entitiesToAdd.clear();

		Hud.setInfo("Relics remaining", ""+zone.relicCount);

		removeFlaggedEntities(zone.localEntities.iterator());
	}


	private void emitEvent(Event event) {
		client.addEvent(event);
	}


	private void removeFlaggedEntities(Iterator<Entity> entityIterator) {
		while (entityIterator.hasNext()) {
			if(entityIterator.next().isInactive())
				entityIterator.remove();
		}
	}

	private Entity makeParticle(Vector3 position) {
		Particle result = new Particle();
		result.position.set(position);
		result.velocity.set(
				(float)(Math.random() * 4 - 2),
				(float)(Math.random() * 15),
				(float)(Math.random() * 4 - 2));
		return result;
	}

	@Override
	public void configurationReceived(TerrainConfig configuration) {
		terrain.configure(configuration);
		gameScreen.startTerrainGeneration();
	}

	@Override
	public void setPlayerId(int playerId) {
		zone.entities.remove(player.getKey());
		player.id = playerId;
		zone.addEntity(player);
	}

	public void setPlayer(PlayerCharacter player) {
		zone.addEntity(player);
		this.player = player;
		Sounds.startup(player);
	}

	@Override
	public PlayerCharacter getPlayer() {
		return player;
	}

	public void dispose() {
		if (client != null)
			client.dispose();
		Sounds.shutdown();
	}

	@Override
	public void performPickup(int playerId, int objectId) {
		Entity object = getEntityById(objectId);
		if (object == null)
			return;
		object.deactivate();
		zone.relicCount--;
		for (int i = 0; i < 15; i++)
			entitiesToAdd.add(makeParticle(object.position));
		Sounds.itemGet(object);
	}

	@Override
	public Entity getEntityById(int id) {
		return zone.getEntityById(id);
	}
	
	@Override
	public void activateRelicSlot(int relicId) {
		RelicSlot relicSlot = (RelicSlot) getEntityById(relicId);
		relicSlot.isActivated = true;
		Sounds.slotActivated(relicSlot);
		zone.slotsActivated++;
		Hud.setInfo("Keys activated", String.format("%d/%d", zone.slotsActivated, zone.slotCount));
	}

	@Override
	public void updateEntity(Entity entity) {
		boolean isNew = zone.updateEntity(entity);		
		if (isNew && entity instanceof PlayerCharacter)
			Hud.log(entity.getName() + " has joined the party");
	}
}
