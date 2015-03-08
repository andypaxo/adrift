package net.softwarealchemist.adrift.events;

import java.io.Serializable;
import java.util.List;

import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.network.ClientListener;

public class MessageEvent implements Event, Serializable {

	private static final long serialVersionUID = -6701891783690653614L;

	private int playerId;
	private String message;

	public MessageEvent(int playerId, String message) {
		this.playerId = playerId;
		this.message = message;
	}

	@Override
	public void execute(ClientListener listener) {
		Entity player = listener.getEntityById(playerId);
		String playerName = player == listener.getPlayer() ? "Me" : player.getName();
		Hud.log(playerName + " : " + message);
	}

	@Override
	public List<Event> executeServer(ClientListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

}