package net.softwarealchemist.network;

import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

import net.softwarealchemist.adrift.TerrainConfig;

import com.badlogic.gdx.utils.DataOutput;

public class AdriftClient {

	private InetAddress server;
	private ClientListener listener;
	private boolean isDisposed;

	public AdriftClient(InetAddress server, ClientListener listener) {
		this.server = server;
		this.listener = listener;
	}

	public void start() {
		new Thread(() -> run()).start();
	}
	
	private void run() {		
		try {
			Socket socket = new Socket(server, 10537);
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			DataOutput output = new DataOutput(socket.getOutputStream());
			
			Object received;
			while (!isDisposed) {
				received = inputStream.readObject();
				if (received instanceof TerrainConfig)
					listener.ConfigurationReceived((TerrainConfig) received);
			}
			
			output.close();
			inputStream.close();
			socket.close();
		} catch (Exception e) {
			
			e.printStackTrace();
			System.exit(110);
		}
	}

	public void dispose() {
		isDisposed = true;
	}

}
