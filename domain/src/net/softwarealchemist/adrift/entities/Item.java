package net.softwarealchemist.adrift.entities;

import java.io.Serializable;

public abstract class Item implements Serializable {
	private static final long serialVersionUID = 9141270870499692034L;
	
	protected String name;

	public String getName() {
		return name;
	}
}
