package net.softwarealchemist.adrift.util;

import java.util.ArrayList;

public class GraphNode {
	private ArrayList<GraphNode> neighbors = new ArrayList<GraphNode>();
	public int tag;
	
	public GraphNode(int tag) {
		this.tag = tag;
	}

	public ArrayList<GraphNode> getNeighbors() {
		return neighbors;
	}
	
	public void addNeighbor(GraphNode node) {
		neighbors.add(node);
	}
}
