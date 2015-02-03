package net.softwarealchemist.adrift.entities;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector3;

public abstract class Entity implements Serializable {
	private static final long serialVersionUID = 8903359267394617157L;
	
	public Vector3 rotation;
	public Vector3 position;
	public Vector3 velocity;
	public Vector3 size;
	public float gravityMultiplier;
	public int id;
	public String name;

	public Entity() {
		rotation = new Vector3();
		position = new Vector3();
		velocity = new Vector3();
		size = new Vector3();
	}

	public Integer getKey() {
		return new Integer(id);
	}

	public void updateFrom(Entity entity) {
		rotation.set(entity.rotation);
		position.set(entity.position);
		velocity.set(entity.velocity);
		gravityMultiplier = entity.gravityMultiplier;
		size.set(entity.size);
		id = entity.id;
		name = entity.name;
	}
	
}
