package net.softwarealchemist.adrift.entities;

public class Particle extends Entity {
	private static final long serialVersionUID = 8518171444016099030L;

	private float timeToLive;

	public Particle() {
		super();
		gravityMultiplier = 1;
		bounciness = 0.8f;
		timeToLive = 1 + (float) Math.random();
		localOnly = true;
	}



	@Override
	public void step(float delta) {
		timeToLive -= delta;
		if (timeToLive <= 0)
			flaggedForRemoval = true;
	}
}
