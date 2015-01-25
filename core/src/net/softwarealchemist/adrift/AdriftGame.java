package net.softwarealchemist.adrift;

import com.badlogic.gdx.Game;

public class AdriftGame extends Game {

	private boolean isStarted;

	@Override
	public void create() {
		setScreen(new MainMenuScreen(this));
	}

	public void startGame() {
		getScreen().dispose();
		setScreen(new GameScreen());
		isStarted = true;
		System.out.println("The game is started");
	}

	public boolean isStarted() {
		return isStarted;
	}
	
	

}
