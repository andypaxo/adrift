package net.softwarealchemist.adrift;

import com.badlogic.gdx.Gdx;

public class InputHandler {
	Entity player;
	
	public InputHandler(Entity player) {
		this.player = player;
	}

	public void handleInput() {
		doMouseLook();
	}

	private void doMouseLook() {
		int mouseX = Gdx.input.getDeltaX();
		int mouseY = Gdx.input.getDeltaY();
		
		player.rotation.y -= mouseX;
		player.rotation.x += mouseY;
	}

}
