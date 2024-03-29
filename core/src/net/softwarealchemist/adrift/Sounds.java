package net.softwarealchemist.adrift;

import net.softwarealchemist.adrift.entities.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Sounds {
	private static Sound bling;
	private static Sound slotActivated;
	private static Music loop;
	private static Entity player;
	private static float loopVolume = 0;
	
	public static void startup(Entity playerCharacter) {
		bling = Gdx.audio.newSound(Gdx.files.internal("sounds/bling.wav"));
		slotActivated = Gdx.audio.newSound(Gdx.files.internal("sounds/activate-slot.wav"));
		loop = Gdx.audio.newMusic(Gdx.files.internal("sounds/relic-loop.mp3"));
		loop.setLooping(true);
		loop.setVolume(loopVolume);
		loop.play();
		player = playerCharacter; 
	}
	
	public static void itemGet(Entity item) {
		bling.play(getDistanceBasedVolume(item));
	}

	public static void slotActivated(Entity relicSlot) {
		slotActivated.play(getDistanceBasedVolume(relicSlot));
	}
	
	private static float getDistanceBasedVolume(Entity item) {
		float volume = 1f / item.position.dst(player.position);
		return volume;
	}
	
	public static void setLoopDistance(float distance) {
		float targetVolume = distance < 32 ? 1f / distance : 0;
		loopVolume = (targetVolume + loopVolume * 19f) / 20f;
		loop.setVolume(loopVolume);
	}
	
	public static void shutdown() {
		bling.dispose();
		slotActivated.dispose();
		loop.dispose();
	}
}
