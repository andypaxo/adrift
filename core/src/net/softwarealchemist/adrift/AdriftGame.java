package net.softwarealchemist.adrift;

import com.badlogic.gdx.Game;

public class AdriftGame extends Game {

	@Override
	public void create() {
		setScreen(new MainMenuScreen(this));
	}

	public void startGame() {
		getScreen().dispose();
		setScreen(new GameScreen());
	}
	
	

}
