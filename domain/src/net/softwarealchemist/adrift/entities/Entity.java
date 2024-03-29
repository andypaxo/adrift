package net.softwarealchemist.adrift.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.events.Event;

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
	protected String name;
	public boolean canBeCollected;
	private boolean deactivated;
	public boolean localOnly;

	public Entity() {
		rotation = new Vector3();
		position = new Vector3();
		velocity = new Vector3();
		size = new Vector3(.99f, .99f, .99f);
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
	
	public Vector3 getFacing() {
		return new Vector3(Vector3.Z)
			.rotate(rotation.x, 1, 0, 0)
			.rotate(rotation.y, 0, 1, 0);
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
	
	public String getName() {
		return name;
	}
	
	public List<Item> dropItems() {
		return new ArrayList<Item>();
	}

	public Event[] onTouchPlayer(PlayerCharacter player) {
		return new Event[0];
	}

	// TODO : All the functions in this class should work like these,
	//        rather than returning new values
	
	public void getApproximateBoundingMinimum(Vector3 min) {
		min.set(position).sub(size.x / 2, size.y / 2, size.z / 2);
	}
	
	public void getApproximateBoundingMaximum(Vector3 max) {
		max.set(position).add(size.x / 2, size.y / 2, size.z / 2);
	}
}
