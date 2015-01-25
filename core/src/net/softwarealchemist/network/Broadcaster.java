package net.softwarealchemist.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Broadcaster {
	DatagramSocket tx;
	private String id;
	private ScheduledThreadPoolExecutor executor;

	public Broadcaster() {
		try {
			tx = new DatagramSocket(10539);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		id = getHostName();
		executor = new ScheduledThreadPoolExecutor(1);
		executor.scheduleAtFixedRate(() -> broadcast(), 0, 1, TimeUnit.SECONDS);
	}
	
	private void broadcast() {
		byte[] buf = id.getBytes(Charset.forName("UTF-8"));
		try {
			final InetAddress addr = InetAddress.getByName("255.255.255.255");
			final DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, 10538);
			tx.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String getHostName() {
		try {
			InetAddress myAddress = InetAddress.getLocalHost();
			return myAddress.getHostName();
		} catch (UnknownHostException ex) {
			return "Nigel";
		}
	}
	
	public void dispose() {
		executor.shutdown();
		tx.disconnect();
	}
}
