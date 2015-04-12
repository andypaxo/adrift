package net.softwarealchemist.adrift.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.events.Event;

public class StateUpdate implements Serializable {
	private static final long serialVersionUID = 8351637854801354279L;
	
	private List<Entity> updatedEntities;
	private List<Event> events;
	
	public StateUpdate(List<Entity> updatedEntities, List<Event> events) {
		this.updatedEntities = updatedEntities;
		this.events = new ArrayList<Event>(events);
	}

	public List<Entity> getUpdatedEntities() {
		return updatedEntities;
	}

	public List<Event> getEvents() {
		return events;
	}
}
