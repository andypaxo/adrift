package net.softwarealchemist.network;

import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

import net.softwarealchemist.adrift.TerrainConfig;

import com.badlogic.gdx.utils.DataOutput;

public class AdriftClient {

	private InetAddress server;

	public AdriftClient(InetAddress server) {
		this.server = server;
	}

	// Synchronously download configuration
	public TerrainConfig getConfiguration() {
		TerrainConfig result = null;
		try {
			final Socket socket = new Socket(server, 10537);
			final ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			final DataOutput output = new DataOutput(socket.getOutputStream());
			
			output.writeBytes("configuration\n");
			result = (TerrainConfig) inputStream.readObject();
			
			output.close();
			inputStream.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(110);
		}
		return result;
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
