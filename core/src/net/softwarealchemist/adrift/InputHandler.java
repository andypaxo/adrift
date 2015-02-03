package net.softwarealchemist.adrift;

import net.softwarealchemist.adrift.entities.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class InputHandler {
	Entity player;
	static final float mouseSensitivity = .5f;
	
	public InputHandler(Entity player) {
		this.player = player;
	}

	public void handleInput() {
		doStateChanges();
		doMouseLook();
		if (GameState.InteractionMode == GameState.MODE_WALK)
			doWalk();
		else
			doFly();
	}

	private void doStateChanges() {
		if (Gdx.input.justTouched())
			Gdx.input.setCursorCatched(true);
		
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			Gdx.input.setCursorCatched(false);
		
		if (Gdx.input.isKeyJustPressed(Keys.TAB))
			GameState.InteractionMode = (GameState.InteractionMode + 1) % 3;
	}

	private void doMouseLook() {
		if(!Gdx.input.isCursorCatched())
			return;
		
		float mouseX = Gdx.input.getDeltaX() * mouseSensitivity;
		float mouseY = Gdx.input.getDeltaY() * mouseSensitivity;
		
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
		if (Gdx.input.isKeyJustPressed(Keys.SPACE))
			vertical = 1;
		
		// TODO : Speeds should be set on player object
		float previousYMovement = player.velocity.y;
		player.velocity.set(slide, 0, forward); 
		player.velocity.rotate(Vector3.Y, player.rotation.y);
		player.velocity.nor().scl(10);
		if (vertical != 0 && previousYMovement <= 0f)
			player.velocity.y += vertical * 15;
		else
			player.velocity.y = previousYMovement;
		player.gravityMultiplier = 1;
	}

	private void doFly() {
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

		// TODO : Speeds should be set on player object
		player.velocity.set(slide, 0, forward); 
		player.velocity.rotate(Vector3.X, player.rotation.x); // For flying only
		player.velocity.rotate(Vector3.Y, player.rotation.y);
		player.velocity.y += vertical;
		player.velocity.nor().scl(20);
		player.gravityMultiplier = 0;
	}

}
