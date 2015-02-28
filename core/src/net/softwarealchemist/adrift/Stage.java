package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.Particle;
import net.softwarealchemist.adrift.entities.PlayerCharacter;
import net.softwarealchemist.adrift.entities.Relic;
import net.softwarealchemist.network.AdriftClient;
import net.softwarealchemist.network.AdriftServer;
import net.softwarealchemist.network.Broadcaster;
import net.softwarealchemist.network.ClientListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

public class Stage implements ClientListener {
	Terrain terrain;
	public HashMap<Integer, Entity> entities;
	private GameScreen gameScreen;
	private int highestId;
	private Entity player;
	private int relicCount;
	
	private AdriftClient client;
	private AdriftServer server;
	private Broadcaster broadcaster;
	
	// Wrong, wrong, wrong
	private Sound bling;
	
	public Stage(Terrain terrain, GameScreen gameScreen) {
		this.terrain = terrain;
		this.gameScreen = gameScreen;
		entities = new HashMap<Integer, Entity>();
		bling = Gdx.audio.newSound(Gdx.files.internal("sounds/bling.wav"));
	}


	public void startWithLocalServer() {
		terrain.configureRandom();
		server = new AdriftServer();
		server.setConfiguration(terrain.getConfiguration());
		server.setStage(this);
		server.start();
		broadcaster = new Broadcaster();
		broadcaster.start();
	}
	
	public void startWithRemoteServer() {
		client = new AdriftClient(GameState.server, this);
		client.start();
	}
	
	// TODO : This should only be called by server
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
			final Relic relic = new Relic();
			relic.id = getNextId();
			int location = validLocations.get(relicCount);
			relic.size.set(.75f, .75f, .75f);
			relic.position.set(
				(location % terrain.configuration.width) + .5f,
				location / (terrain.configuration.width * terrain.configuration.depth) + relic.size.y / 2f,
				((location / terrain.configuration.width) % terrain.configuration.depth) + .5f
			);
			relic.name = "Relic " + relicCount;
			addEntity(relic);
		}

		System.out.println(String.format("%d relics added in %.1f seconds", relicCount, (System.nanoTime() - startTime) / 1000000000.0));
	}
	
	public void addEntity(Entity entity) {
		entities.put(entity.getKey(), entity);
	}
	
	public void step(float timeStep) {
		Vector3 scratch = new Vector3();
		for (Entity entity : entities.values()) {
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
				if (entity.position.y < entity.size.y * .5f || collisionWithTerrain(entity)) {
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

		int playerRegion = terrain.regions.getInt((int) player.position.x, (int) player.position.y, (int) player.position.z);
		Hud.setInfo("Region", "" + playerRegion);
	}
	
	public void doEvents() {
		ArrayList<Entity> entitiesToAdd = new ArrayList<Entity>();
		for (Entity entity : entities.values()) {
			// This could get really tangled. 
			// Might be a good idea for entities to have a way of hooking in their own event code
			
			if (entity instanceof PlayerCharacter) {
				for (Entity other : entities.values())
					if (other.canBeCollected && !other.flaggedForRemoval && entity.intersectsWith(other)) {
						other.flaggedForRemoval = true;
						relicCount--;
						Hud.log("Item collected : " + other.name);
						for (int i = 0; i < 15; i++)
							entitiesToAdd.add(makeParticle(other.position));
						bling.play();
					}
			}
		}
		
		for (Entity entity : entitiesToAdd)
			addEntity(entity);

		Hud.setInfo("Relics remaining", ""+relicCount);
		
		Iterator<Entity> entityIterator = entities.values().iterator();
		while (entityIterator.hasNext()) {
			if(entityIterator.next().flaggedForRemoval)
				entityIterator.remove();
		}
	}

	private Entity makeParticle(Vector3 position) {
		Particle result = new Particle();
		result.id = getNextId();
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

	public void setPlayer(Entity player) {
		addEntity(player);
		this.player = player;
	}

	@Override
	public Entity getPlayer() {
		return player;
	}
	
	public void updateEntity(Entity entity) {
		if (entities.containsKey(entity.getKey())) {
			entities.get(entity.getKey()).updateFrom(entity);
		} else {
			addEntity(entity);
			Hud.log(entity.name + " has joined the party");
		}
	}

	public void dispose() {
		if (server != null)
			server.dispose();
		if (client != null)
			client.dispose();
		broadcaster.dispose();
		bling.dispose();
	}
}
