package net.softwarealchemist.adrift;
import java.net.InetAddress;
import java.util.ArrayList;

import net.softwarealchemist.network.BroadcastListener;
import net.softwarealchemist.network.DiscoveryListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class MainMenuScreen implements Screen, DiscoveryListener {
	SpriteBatch spriteBatch;
	private BitmapFont font;
	private OrthographicCamera cam;
	private ArrayList<HostConfig> menuItems;
	private int selectedMenuItem;
	private AdriftGame game;
	private boolean shouldStartGame;
	private BroadcastListener broadcastListener;
	
	public MainMenuScreen(AdriftGame game) {
		this.game = game;
		spriteBatch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("fonts/segoeui.fnt"), Gdx.files.internal("fonts/segoeui.png"), false);
		menuItems = new ArrayList<MainMenuScreen.HostConfig>();
		menuItems.add(new HostConfig("Host game", null));
		broadcastListener = new BroadcastListener(this);
		broadcastListener.start();
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {
		if (game.isStarted())
			return;
		
		if (shouldStartGame) {
			GameState.server = menuItems.get(selectedMenuItem).address;
			Gdx.input.setCursorCatched(true);
			game.startGame();
			return;
		}
		

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();

		shouldStartGame |= Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER);
		if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S))
			selectedMenuItem = (selectedMenuItem + 1) % menuItems.size();
		if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W))
			selectedMenuItem = (selectedMenuItem - 1) % menuItems.size();
		
		if (shouldStartGame) {
			font.draw(spriteBatch,
					"Drifting to an island...",
					(int) (Gdx.graphics.getWidth() * -.5f),
					(int) (Gdx.graphics.getHeight() * .5f));
		} else {
			int y = 0;
			int itemN = 0;
			for (HostConfig config : menuItems) {
				if (itemN++ == selectedMenuItem)
					font.setColor(Color.WHITE);
				else
					font.setColor(Color.BLACK);
				y += font.draw(spriteBatch,
					config.address == null ? config.hostname : "Join " + config.hostname,
					(int) (Gdx.graphics.getWidth() * -.5f),
					(int) (Gdx.graphics.getHeight() * .5f) - y)
					.height;
			}
		}
		spriteBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		cam = new OrthographicCamera(width, height);
		cam.update();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		spriteBatch.dispose();
		font.dispose();
	}

	@Override
	public void dispose() {
		broadcastListener.dispose();
	}

	@Override
	public void notifyDiscovered(String hostname, InetAddress addr) {
		menuItems.add(new HostConfig(hostname, addr));
	}
	
	private class HostConfig {
		public String hostname;
		public InetAddress address;
		
		public HostConfig(String hostname, InetAddress address) {
			this.hostname = hostname;
			this.address = address;
		}
	}

}
