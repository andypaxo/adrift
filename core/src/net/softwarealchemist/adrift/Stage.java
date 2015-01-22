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
			entity.position.add(scratch);
		}
	}
}
