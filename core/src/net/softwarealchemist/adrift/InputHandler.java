package net.softwarealchemist.adrift;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class InputHandler {
	Entity player;
	
	public InputHandler(Entity player) {
		this.player = player;
	}

	public void handleInput() {
		doCaptureOrRelease();
		doMouseLook();
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

}
