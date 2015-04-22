package net.softwarealchemist.adrift.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.Monster;
import net.softwarealchemist.adrift.entities.Relic;
import net.softwarealchemist.adrift.entities.RelicItem;
import net.softwarealchemist.adrift.entities.RelicSlot;
import net.softwarealchemist.adrift.util.RayCaster;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.IntArray;

public class Zone {
	public HashMap<Integer, Entity> entities;
	public List<Entity> localEntities;
	private int highestId;
	
	public Terrain terrain;
	
	public int relicCount;
	public int slotCount;
	public int slotsActivated;
	
	public Zone() {
		entities = new HashMap<Integer, Entity>();
		localEntities = new ArrayList<Entity>();
	}

	// ----------- Entity management -----------


	public Entity getEntityById(int id) {
		return entities.get(new Integer(id));
	}
	
	public void addEntity(Entity entity) {
		if (entity.localOnly)
			localEntities.add(entity);
		else
			entities.put(entity.getKey(), entity);
		
		if (entity instanceof Relic)
			relicCount++;
		else if (entity instanceof RelicSlot)
			slotCount++;
	}
	
	public boolean updateEntity(Entity entity) {
		if (entities.containsKey(entity.getKey())) {
			Entity localEntity = getEntityById(entity.getKey());
			if (localEntity.isActive())
				localEntity.updateFrom(entity);
			return false;
		} else {
			addEntity(entity);
			return true;
		}
	}

	// ----------- Simulation -----------
	
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
			
			// Very bad. Should only apply to local player
			//boolean blockedByGround = GameState.InteractionMode == GameState.MODE_WALK && entity.position.y < entity.size.y * .5f;
			boolean blockedByGround = entity.position.y < entity.size.y * .5f;
			
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
	}

	private float resolve(float position, float velocity, float size) {
		return velocity > 0
			? (float) (Math.ceil(position - velocity) - size * .5002f)
			: (float) (Math.floor(position - velocity) + size * .5002f);
	}

	private boolean collisionWithTerrain(Entity entity) {
		if (terrain == null)
			throw new RuntimeException("Null terrain");
		if (entity.position == null)
			throw new RuntimeException("Entity has no position");
		if (entity.size == null)
			throw new RuntimeException("Entity has no size");
		
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
	
	// ----------- Server only! -----------

	public int getNextId() {
		return highestId++;
	}
	
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
		for (int relicN = 0; relicN < terrain.configuration.width * .1; relicN++) {
			RelicItem item = new RelicItem("r" + relicN);
			final Relic relic = new Relic(item);
			relic.id = getNextId();
			int location = validLocations.pop();
			relic.position.set(
				(location % terrain.configuration.width) + .5f,
				location / (terrain.configuration.width * terrain.configuration.depth) + .5f,
				((location / terrain.configuration.width) % terrain.configuration.depth) + .5f
			);
			addEntity(relic);
		}

		for (int monsterN = 0; monsterN < terrain.configuration.width * .1; monsterN++) {
			final Monster monster = new Monster();
			int location = validLocations.pop();
			monster.position.set(
					(location % terrain.configuration.width) + .5f,
					location / (terrain.configuration.width * terrain.configuration.depth) + .375f,
					((location / terrain.configuration.width) % terrain.configuration.depth) + .5f
				);
			monster.id = getNextId();
			addEntity(monster);
		}
		System.out.println(String.format("%d relics added in %.1f seconds", relicCount, (System.nanoTime() - startTime) / 1000000000.0));
	}

	public void pullEntitiesFromTerrain() {
		for (Entity entity : terrain.predefinedEntities) {
			entity.id = getNextId();
			addEntity(entity);
		}
	}

	public Entity findEntityInFrontOf(Entity observer) {
		Ray ray = new Ray(observer.position, observer.getFacing().nor());
		BoundingBox box = new BoundingBox();
		Entity closestCandiate = null;
		float distanceToClosestCandidate;

		Vector3 lookingAtVoxel = RayCaster.cast(observer.position, observer.getFacing(), 512, terrain);
		
		distanceToClosestCandidate = lookingAtVoxel != null
			? observer.position.dst2(lookingAtVoxel.add(.5f))
			: 512;
		
		float distanceToNextCandidate;
		final Vector3 min = new Vector3();
		final Vector3 max = new Vector3();

		for (Entity other : entities.values()) {
			distanceToNextCandidate = other.position.dst2(observer.position);
			if (other == observer || other.isInactive() || distanceToNextCandidate > distanceToClosestCandidate)
				continue;

			other.getApproximateBoundingMinimum(min);
			other.getApproximateBoundingMaximum(max);
			box.set(min, max);
			if (Intersector.intersectRayBoundsFast(ray, box)) {
				distanceToClosestCandidate = distanceToNextCandidate;
				closestCandiate = other;
			}
		}
		
		return closestCandiate;
	}
}
