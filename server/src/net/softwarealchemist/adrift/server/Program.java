package net.softwarealchemist.adrift.server;

import net.softwarealchemist.adrift.model.Terrain;
import net.softwarealchemist.adrift.model.Zone;
import net.softwarealchemist.network.Broadcaster;

public class Program {

	public static void main(String[] args) {
		System.out.println("Server starting...");

		AdriftServer server = new AdriftServer();
		
		Zone zone = new Zone();
		zone.terrain = new Terrain();
		zone.terrain.configureRandom();
		zone.terrain.generate();
		zone.pullEntitiesFromTerrain();
		zone.generateRelics();
		
		server.setConfiguration(zone.terrain.getConfiguration());
		server.setStage(zone);
		server.start();
		
		Broadcaster broadcaster = new Broadcaster();
		broadcaster.start();
	}

}
