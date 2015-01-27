package net.softwarealchemist.adrift;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class AdriftGame extends Game {

	private boolean isStarted;

	@Override
	public void create() {
		verifyWaterShader();
		setScreen(new MainMenuScreen(this));
	}

	private void verifyWaterShader() {
		ShaderProgram waterShader = new ShaderProgram(Gdx.files.internal("shaders/waterVertex.glsl"), Gdx.files.internal("shaders/waterFragment.glsl"));
		if (!waterShader.isCompiled()) {
			System.out.println(waterShader.getLog());
			Gdx.app.exit();
		}
		waterShader.dispose();
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
