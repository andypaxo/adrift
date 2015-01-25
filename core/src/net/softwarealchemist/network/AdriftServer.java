package net.softwarealchemist.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import net.softwarealchemist.adrift.TerrainConfig;

import com.badlogic.gdx.Gdx;

public class AdriftServer {

	private TerrainConfig configuration;
	private ServerSocket serverSocket;

	public void setConfiguration(TerrainConfig configuration) {
		this.configuration = configuration;
	}

	public void start() {
		new Thread(() -> listen()).start();
	}

	private void listen() {
		try {
			serverSocket = new ServerSocket(10537);
			while (true) {
				final Socket socket = serverSocket.accept();
				new Thread(() -> listenTo(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}

	private void listenTo(Socket socket) {
		try {
			final ObjectOutputStream clientOutput = new ObjectOutputStream(socket.getOutputStream());
			final BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			clientOutput.writeObject(configuration);
			
			while (true) {
				final String command = clientInput.readLine();
				System.out.println("Got command : " + command);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}

	public void dispose() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
