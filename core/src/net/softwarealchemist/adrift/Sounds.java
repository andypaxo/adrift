package net.softwarealchemist.adrift;

import net.softwarealchemist.adrift.entities.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Sounds {
	private static Sound bling;
	private static Music loop;
	private static Entity player;
	
	public static void startup(Entity playerCharacter) {
		bling = Gdx.audio.newSound(Gdx.files.internal("sounds/bling.wav"));
		loop = Gdx.audio.newMusic(Gdx.files.internal("sounds/relic-loop.mp3"));
		loop.setLooping(true);
		loop.play();
		player = playerCharacter; 
	}
	
	public static void itemGet(Entity item) {
		float volume = 1f / item.position.dst(player.position);
		bling.play(volume);
	}
	
	public static void setLoopDistance(float distance) {
		loop.setVolume(1f / distance);
	}
	
	public static void shutdown() {
		bling.dispose();
	}
}
