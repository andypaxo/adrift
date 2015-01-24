package net.softwarealchemist.adrift;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Hud {
	
	SpriteBatch spriteBatch;
	private BitmapFont font;
	private OrthographicCamera cam;

	public Hud() {
		spriteBatch = new SpriteBatch();
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		font = new BitmapFont(Gdx.files.internal("fonts/segoeui.fnt"), Gdx.files.internal("fonts/segoeui.png"), false);
	}

	public void render() {
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();
		font.drawMultiLine(spriteBatch,
				"Adrift",
				(int) (Gdx.graphics.getWidth() * -.5f),
				(int) (Gdx.graphics.getHeight() * .5f));
		spriteBatch.end();
	}
	
	public void resize(int width, int height) {
		cam = new OrthographicCamera(width, height);
		cam.update();
	}

}
