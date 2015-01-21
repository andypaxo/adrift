package net.softwarealchemist.adrift;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class InputHandler {
	Entity player;
	
	public InputHandler(Entity player) {
		this.player = player;
	}

	public void handleInput() {
		doCaptureOrRelease();
		doMouseLook();
		doWalk();
	}

	private void doCaptureOrRelease() {
		if (Gdx.input.justTouched())
			Gdx.input.setCursorCatched(true);
		
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			Gdx.input.setCursorCatched(false);
	}

	private void doMouseLook() {
		if(!Gdx.input.isCursorCatched())
			return;
		
		int mouseX = Gdx.input.getDeltaX();
		int mouseY = Gdx.input.getDeltaY();
		
		player.rotation.y -= mouseX;
		player.rotation.x += mouseY;
	}

	private void doWalk() {
		float forward = 0, strafe = 0;
		
		// TODO : Speeds should be set on player object
		if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))
			forward = 10;
		if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))
			forward = -10;
		if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.RIGHT))
			strafe = 10;
		if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.LEFT))
			strafe = -10;

		// TODO : A world should handle the actual position update
		player.velocity.set(strafe, 0, forward); // Should preserve Y for walking. Clear for flying.
		player.velocity.rotate(Vector3.X, player.rotation.x); // For flying only
		player.velocity.rotate(Vector3.Y, player.rotation.y);
		Vector3 motion = new Vector3(player.velocity).scl(Gdx.graphics.getDeltaTime());
		player.position.add(motion);
	}

}
