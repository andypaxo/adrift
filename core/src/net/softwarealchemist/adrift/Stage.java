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
import net.softwarealchemist.adrift.entities.Relic;
import net.softwarealchemist.adrift.entities.RelicItem;
import net.softwarealchemist.adrift.entities.RelicSlot;
import net.softwarealchemist.adrift.events.PickupEvent;
import net.softwarealchemist.network.AdriftClient;
import net.softwarealchemist.network.AdriftServer;
import net.softwarealchemist.network.Broadcaster;
import net.softwarealchemist.network.ClientListener;
import net.softwarealchemist.network.ClientToLocalConnection;
import net.softwarealchemist.network.ClientToSocketConnection;
import net.softwarealchemist.network.ServerToLocalConnection;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

public class Stage implements ClientListener {
	Terrain terrain;
	public HashMap<Integer, Entity> entities;
	public List<Entity> localEntities;
	private GameScreen gameScreen;
	private int highestId;
	private PlayerCharacter player;
	private int relicCount;
	
	private AdriftClient client;
	private AdriftServer server;
	private Broadcaster broadcaster;
	private ArrayList<Entity> entitiesToAdd;
	
	public Stage(Terrain terrain, GameScreen gameScreen) {
		this.terrain = terrain;
		this.gameScreen = gameScreen;
		entities = new HashMap<Integer, Entity>();
		entitiesToAdd = new ArrayList<Entity>();
		localEntities = new ArrayList<Entity>();
	}


	public void startWithLocalServer() {
		terrain.configureRandom();
		server = new AdriftServer();
		server.setConfiguration(terrain.getConfiguration());
		server.setStage(this);
		server.start();
		
		final ClientToLocalConnection clientConnection = new ClientToLocalConnection();
		client = new AdriftClient(clientConnection, this);
		
		final ServerToLocalConnection serverConnection = new ServerToLocalConnection(client, this, terrain.getConfiguration(), server);
		server.addClient(serverConnection);
		clientConnection.setServerConnection(serverConnection);
		client.start();
				
		broadcaster = new Broadcaster();
		broadcaster.start();
	}
	
	public void startWithRemoteServer() {
		final ClientToSocketConnection clientConnection = new ClientToSocketConnection(GameState.server);
		client = new AdriftClient(clientConnection, this);
		client.start();
	}
	
	// Should be part of server?
	public void generateRelics () {
		long startTime = System.nanoTime();

		final IntArray validLocations = new IntArray();

		for (int x = 0; x < terrain.configuration.width; x++)
			for (int z = 0; z < terrain.configuration.depth; z++)
				for (int y = 2; y < terrain.configuration.height; y++)
					if ((x+y+z) % 13 == 0 && terrain.get(x, y, z) == 0 && terrain.get(x, y - 1, z) > 0)
						validLocations.add(y * terrain.configuration.width * terrain.configuration.depth + z * terrain.configuration.width + x);
		
		System.out.println(String.format("Found %d valid relic locations", validLocations.size));
		validLocations.shuffle();
		for (relicCount = 0; relicCount < terrain.configuration.width * .1; relicCount++) {
			RelicItem item = new RelicItem("r" + relicCount);
			final Relic relic = new Relic(item);
			relic.id = getNextId();
			int location = validLocations.get(relicCount);
			relic.size.set(.75f, .75f, .75f);
			relic.position.set(
				(location % terrain.configuration.width) + .5f,
				location / (terrain.configuration.width * terrain.configuration.depth) + relic.size.y / 2f,
				((location / terrain.configuration.width) % terrain.configuration.depth) + .5f
			);
			addEntity(relic);
		}

		System.out.println(String.format("%d relics added in %.1f seconds", relicCount, (System.nanoTime() - startTime) / 1000000000.0));
	}


	public void pullEntitiesFromTerrain() {
		for (Entity entity : terrain.predefinedEntities) {
			entity.id = getNextId();
			addEntity(entity);
		}
	}
	
	public void addEntity(Entity entity) {
		if (entity.localOnly)
			localEntities.add(entity);
		else
			entities.put(entity.getKey(), entity);
	}
	
	public void step(float timeStep) {
		Vector3 scratch = new Vector3();
		for (Entity entity : entities.values())
			stepEntity(timeStep, scratch, entity);
		for (Entity entity : localEntities)
			stepEntity(timeStep, scratch, entity);
	}


	private void stepEntity(float timeStep, Vector3 scratch, Entity entity) {
		entity.velocity.y -= 40 * timeStep * entity.gravityMultiplier;
		
		scratch.set(entity.velocity).scl(timeStep);
		
		// Very basic (tunneling prone) collision with terrain
		if (entity.velocity.x != 0) {
			entity.position.x += scratch.x;
			if (collisionWithTerrain(entity)) {
				entity.position.x = resolve(entity.position.x, scratch.x, entity.size.x); // Back out
				entity.velocity.x = entity.velocity.x * (-entity.bounciness);
			}
		}
		if (entity.velocity.y != 0) {
			entity.position.y += scratch.y;
			boolean blockedByGround = GameState.InteractionMode == GameState.MODE_WALK && entity.position.y < entity.size.y * .5f;
			if (blockedByGround || collisionWithTerrain(entity)) {
				entity.position.y = resolve(entity.position.y, scratch.y, entity.size.y); // Back out
				entity.velocity.y = entity.velocity.y * (-entity.bounciness);
			}
		}
		if (entity.velocity.z != 0) {	
			entity.position.z += scratch.z;
			if (collisionWithTerrain(entity)) {
				entity.position.z = resolve(entity.position.z, scratch.z, entity.size.z); // Back out
				entity.velocity.z = entity.velocity.z * (-entity.bounciness);
			}
		}

		entity.step(timeStep);
	}
	
	public void doEvents() {
		float nearestCollectibleDistance = Float.MAX_VALUE;
		List<Entity> activeEntities = entities.values().stream().filter((entity) -> entity.isActive()).collect(Collectors.toList());
		for (Entity entity : activeEntities) {
				
			
			if (entity.canBeCollected) {
				float distance = player.position.dst(entity.position);
				nearestCollectibleDistance = Math.min(distance, nearestCollectibleDistance);
			}
			
			// This could get really tangled. 
			// Might be a good idea for entities to have a way of hooking in their own event code
			if (entity.intersectsWith(player)) {
				if (entity.canBeCollected) {
					PickupEvent event = new PickupEvent(player.id, entity.id);
					emitEvent(event);
					event.execute(this);
				}

				// Awful. Fix this.
				if (entity instanceof RelicSlot) {
					// Use event so this propagates
					if (player.getInventory().size() > 0) {
						player.removeFromInventory(player.getInventory().get(0));
						Hud.setInfo("Inventory", player.describeInventory());
						((RelicSlot) entity).isActivated = true;
					}
				}
			}
		}
		
		Sounds.setLoopDistance(nearestCollectibleDistance);
		
		for (Entity entity : entitiesToAdd)
			addEntity(entity);
		entitiesToAdd.clear();

		Hud.setInfo("Relics remaining", ""+relicCount);

		removeFlaggedEntities(localEntities.iterator());
	}


	private void emitEvent(PickupEvent event) {
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

	private float resolve(float position, float velocity, float size) {
		return velocity > 0
			? (float) (Math.ceil(position - velocity) - size * .5002f)
			: (float) (Math.floor(position - velocity) + size * .5002f);
	}

	private boolean collisionWithTerrain(Entity entity) {
		Vector3 pos = entity.position, size = new Vector3(entity.size).scl(.5f);
		return
			terrain.get((int)(pos.x + size.x), (int)(pos.y + size.y), (int)(pos.z + size.z)) > 0 ||
			terrain.get((int)(pos.x + size.x), (int)(pos.y + size.y), (int)(pos.z - size.z)) > 0 ||
			terrain.get((int)(pos.x + size.x), (int)(pos.y - size.y), (int)(pos.z + size.z)) > 0 ||
			terrain.get((int)(pos.x + size.x), (int)(pos.y - size.y), (int)(pos.z - size.z)) > 0 ||
			terrain.get((int)(pos.x - size.x), (int)(pos.y + size.y), (int)(pos.z + size.z)) > 0 ||
			terrain.get((int)(pos.x - size.x), (int)(pos.y + size.y), (int)(pos.z - size.z)) > 0 ||
			terrain.get((int)(pos.x - size.x), (int)(pos.y - size.y), (int)(pos.z + size.z)) > 0 ||
			terrain.get((int)(pos.x - size.x), (int)(pos.y - size.y), (int)(pos.z - size.z)) > 0;
	}

	@Override
	public void configurationReceived(TerrainConfig configuration) {
		terrain.configure(configuration);
		gameScreen.startTerrainGeneration();
	}

	public int getNextId() {
		return highestId++;
	}

	@Override
	public void setPlayerId(int playerId) {
		entities.remove(player.getKey());
		player.id = playerId;
		addEntity(player);
	}

	public void setPlayer(PlayerCharacter player) {
		addEntity(player);
		this.player = player;
		Sounds.startup(player);
	}

	@Override
	public PlayerCharacter getPlayer() {
		return player;
	}
	
	public void updateEntity(Entity entity) {
		if (entities.containsKey(entity.getKey())) {
			Entity localEntity = getEntityById(entity.getKey());
			if (localEntity.isActive())
				localEntity.updateFrom(entity);
		} else {
			addEntity(entity);
			if (entity instanceof PlayerCharacter)
				Hud.log(entity.getName() + " has joined the party");
		}
	}

	public void dispose() {
		if (server != null)
			server.dispose();
		if (client != null)
			client.dispose();
		broadcaster.dispose();
		Sounds.shutdown();
	}


	@Override
	public void performPickup(int playerId, int objectId) {
		Entity object = getEntityById(objectId);
		if (object == null)
			return;
		object.deactivate();
		relicCount--;
		for (int i = 0; i < 15; i++)
			entitiesToAdd.add(makeParticle(object.position));
		Sounds.itemGet(object);
	}


	@Override
	public Entity getEntityById(int id) {
		return entities.get(new Integer(id));
	}
}
