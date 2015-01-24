package net.softwarealchemist.network;

import java.net.InetAddress;

public interface DiscoveryListener {

	void notifyDiscovered(String hostname, InetAddress addr);

}
