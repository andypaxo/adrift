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
	public float bounciness;
	public int id;
	public String name;
	public boolean canBeCollected;
	private boolean deactivated;
	public boolean localOnly;

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

	public boolean intersectsWith(Entity other) {

		float lx = Math.abs(this.position.x - other.position.x);
		float sumx = (this.size.x / 2f) + (other.size.x / 2f);

		float ly = Math.abs(this.position.y - other.position.y);
		float sumy = (this.size.y / 2f) + (other.size.y / 2f);

		float lz = Math.abs(this.position.z - other.position.z);
		float sumz = (this.size.z / 2f) + (other.size.z / 2f);

		return (lx <= sumx && ly <= sumy && lz <= sumz);
	}
	
	public void step(float delta) {
		
	}
	
	public boolean isActive() {
		return !deactivated;
	}

	public final boolean isInactive() {
		return deactivated;
	}

	public final void deactivate() {
		this.deactivated = true;
	}
	
	
}
