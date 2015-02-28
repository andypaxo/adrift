package net.softwarealchemist.network;

import net.softwarealchemist.adrift.Stage;
import net.softwarealchemist.adrift.dto.ClientSetup;
import net.softwarealchemist.adrift.dto.TerrainConfig;

public class ServerToLocalConnection implements ServerConnection {

	private AdriftClient client;
	private Stage stage;
	private TerrainConfig configuration;

	public ServerToLocalConnection(AdriftClient client, Stage stage, TerrainConfig configuration) {
		this.client = client;
		this.stage = stage;
		this.configuration = configuration;
	}
	@Override
	public void listen() {
		client.objectReceived(new ClientSetup(configuration, 0));
	}

	@Override
	public void send() {
		// TODO Auto-generated method stub

	}

}
