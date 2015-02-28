package net.softwarealchemist.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import net.softwarealchemist.adrift.Hud;
import net.softwarealchemist.adrift.dto.StateUpdate;

public class ClientToSocketConnection implements ClientConnection {

	private InetAddress serverAddress;
	private Socket socket;
	private ObjectOutputStream output;
	private boolean isDisposed;
	private AdriftClient client;
	
	public ClientToSocketConnection(InetAddress serverAddress) {
		super();
		this.serverAddress = serverAddress;
	}

	@Override
	public void send(StateUpdate update) {
		try {
			output.writeObject(update);
			output.reset();
		} catch (Exception e) {
			Hud.log("Communication problem : " + e.getMessage());
		}
	}

	@Override
	public void setClient(AdriftClient client) {
		this.client = client;
	}

	@Override
	public void open() {		
		try {
			socket = new Socket(serverAddress, 10537);
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
			
			Object received;
			while (!isDisposed) {
				received = inputStream.readObject();
				client.objectReceived(received);
			}
			
			output.close();
			inputStream.close();
			socket.close();
		} catch (Exception e) {
			Hud.log("Host disconnected. You're on your own now!");
			client.stop();
		}
	}

	@Override
	public void dispose() {
		isDisposed = true;
	}

}
