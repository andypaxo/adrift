package net.softwarealchemist.adrift.entities;

import com.badlogic.gdx.math.Vector3;

public class Monster extends Entity {
	private static final long serialVersionUID = 7068135930797189981L;
	private int speed = 4;
	
	public Monster() {
		gravityMultiplier = 1;
	}

	@Override
	public void step(float delta) {
		super.step(delta);
		
		// All this only needs to happen on server!
		
		if (Math.random() < .1)
			rotation.y = (float) (Math.random() * 360.0);
		
		if (Math.random() < .05)
			velocity.y = 8;
		
		float previousYMovement = velocity.y;
		velocity.set(Vector3.Z); 
		velocity.rotate(Vector3.Y, rotation.y);
		velocity.nor().scl(speed);
		velocity.y = previousYMovement;
	}

}
