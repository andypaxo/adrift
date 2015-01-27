package net.softwarealchemist.adrift;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Hud {
	
	SpriteBatch spriteBatch;
	private BitmapFont font;
	private OrthographicCamera cam;
	LinkedList<String> messages;
	private static final int maxMessages = 8; 
	private static Hud currentInstance;

	public Hud() {
		spriteBatch = new SpriteBatch();
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		font = new BitmapFont(Gdx.files.internal("fonts/segoeui.fnt"), Gdx.files.internal("fonts/segoeui.png"), false);
		messages = new LinkedList<String>();
		currentInstance = this;
	}

	public void render(Collection<Label2d> labels) {
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();
		
		int y = 0;
		synchronized (this) {
			for (String message : messages)
				y += font.draw(spriteBatch,
					message,
					(int) (Gdx.graphics.getWidth() * -.5f),
					(int) (Gdx.graphics.getHeight() * .5f) - y)
					.height;
		}
		for (Label2d label : labels)
			font.draw(spriteBatch,
					label.text,
					label.position.x - (int) (Gdx.graphics.getWidth() * .5f),
					label.position.y - (int) (Gdx.graphics.getHeight() * .5f) + 32);
		spriteBatch.end();
	}
	
	public void resize(int width, int height) {
		cam = new OrthographicCamera(width, height);
		cam.update();
	}
	
	public static void log(String message) {
		if (currentInstance == null) {
			System.out.println(message);
		} else {
			synchronized (currentInstance) {
				if (currentInstance.messages.size() >= maxMessages)
					currentInstance.messages.removeFirst();
				currentInstance.messages.add(message);
			}
		}
	}
}
