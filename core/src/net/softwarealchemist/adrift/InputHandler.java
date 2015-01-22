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
		doStateChanges();
		doMouseLook();
		doWalk();
	}

	private void doStateChanges() {
		if (Gdx.input.justTouched())
			Gdx.input.setCursorCatched(true);
		
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			Gdx.input.setCursorCatched(false);
		
		if (Gdx.input.isKeyJustPressed(Keys.TAB))
			GameState.InteractionMode = (GameState.InteractionMode + 1) % 2;
	}

	private void doMouseLook() {
		if(!Gdx.input.isCursorCatched())
			return;
		
		int mouseX = Gdx.input.getDeltaX();
		int mouseY = Gdx.input.getDeltaY();
		
		player.rotation.y -= mouseX;
		player.rotation.x += mouseY;
		player.rotation.x = Math.min(88f, Math.max(-88f, player.rotation.x));
	}

	private void doWalk() {
		float forward = 0, slide = 0, vertical = 0;
		
		if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))
			forward = 1;
		if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))
			forward = -1;
		if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.RIGHT))
			slide = 1;
		if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.LEFT))
			slide = -1;
		if (Gdx.input.isKeyPressed(Keys.SPACE))
			vertical = 1;
		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
			vertical = -1;

		// TODO : A world should handle the actual position update
		// TODO : Speeds should be set on player object
		player.velocity.set(slide, vertical, forward).nor().scl(20); // Should preserve Y for walking. Clear for flying.
		player.velocity.rotate(Vector3.X, player.rotation.x); // For flying only
		player.velocity.rotate(Vector3.Y, player.rotation.y);
		Vector3 motion = new Vector3(player.velocity).scl(Gdx.graphics.getDeltaTime());
		player.position.add(motion);
	}

}
