package net.softwarealchemist.adrift.desktop;

import net.softwarealchemist.adrift.AdriftMain;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "adrift";
		new LwjglApplication(new AdriftMain(), config);
	}
}
