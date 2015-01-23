package net.softwarealchemist.adrift;

import com.badlogic.gdx.math.Vector3;

public class Entity {
	Vector3 rotation;
	Vector3 position;
	Vector3 velocity;
	Vector3 size;

	public Entity() {
		rotation = new Vector3();
		position = new Vector3();
		velocity = new Vector3();
		size = new Vector3();
	}
	
}
