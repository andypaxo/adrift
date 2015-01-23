package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector3;

public class Stage {
	Terrain terrain;
	List<Entity> entities;
	
	public Stage(Terrain terrain) {
		this.terrain = terrain;
		entities = new ArrayList<Entity>();
	}
	
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
	
	public void step(float timeStep) {
		Vector3 scratch = new Vector3();
		for (Entity entity : entities) {
			scratch.set(entity.velocity).scl(timeStep);
			// Very basic (tunneling prone) collision with terrain
			if (entity.velocity.x != 0) {
				entity.position.x += scratch.x;
				if (collisionWithTerrain(entity)) {
					entity.position.x = resolve(entity.position.x, scratch.x, entity.size.x); // Back out
					entity.velocity.x = 0;
				}
			}
			if (entity.velocity.y != 0) {
				entity.position.y += scratch.y;
				if (collisionWithTerrain(entity)) {
					entity.position.y = resolve(entity.position.y, scratch.y, entity.size.y); // Back out
					entity.velocity.y = 0;
				}
			}
			if (entity.velocity.z != 0) {
				entity.position.z += scratch.z;
				if (collisionWithTerrain(entity)) {
					entity.position.z = resolve(entity.position.z, scratch.z, entity.size.z); // Back out
					entity.velocity.z = 0;
				}
			}
		}
	}

	private float resolve(float position, float velocity, float size) {
		return velocity > 0
			? (float) (Math.ceil(position - velocity) - size * .51f)
			: (float) (Math.floor(position - velocity) + size * .51f);
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
}
