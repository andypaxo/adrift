package net.softwarealchemist.adrift.desktop;

import net.softwarealchemist.adrift.AdriftGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 600;
		config.title = "adrift";
		new LwjglApplication(new AdriftGame(), config);
	}
}
