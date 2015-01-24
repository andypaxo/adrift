package net.softwarealchemist.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

public class BroadcastListener {

	DatagramSocket rx;
	private InetAddress myAddress;
	private HashMap<String, InetAddress> knownHosts;
	private DiscoveryListener discoveryListener;
	boolean stopped;

	public BroadcastListener(DiscoveryListener discoveryListener) {
		try {
			rx = new DatagramSocket(10538, InetAddress.getByName("0.0.0.0"));
			myAddress = InetAddress.getLocalHost();
			knownHosts = new HashMap<String, InetAddress>();
			this.discoveryListener = discoveryListener;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void listen() {
		String hostname;
		InetAddress addr;
		try {
			byte[] buf = new byte[256];
			final DatagramPacket packet = new DatagramPacket(buf, buf.length);
			while (!stopped) {
				Arrays.fill(buf, (byte) 0);
				rx.receive(packet);
				
				// It makes sense that we should ignore packets from ourself, but for
				// debugging purposes, we'll allow it
//				if (packet.getAddress().equals(myAddress))
//					continue;

				System.out.println("Received");
				hostname = new String(packet.getData(), "UTF-8");
				addr = packet.getAddress();
				if (!knownHosts.containsKey(hostname)) {
					knownHosts.put(hostname, addr);
					discoveryListener.notifyDiscovered(hostname, addr);
				}
			}
			rx.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		new Thread(() -> listen()).start();
	}
	
	public void dispose()
	{
		stopped = true;
	}
}
