package net.softwarealchemist.adrift.dto;

import java.io.Serializable;
import java.util.List;

import net.softwarealchemist.adrift.entities.Entity;

public class StateUpdate implements Serializable {
	private static final long serialVersionUID = 8351637854801354279L;
	
	List<Entity> updatedEntities;
	
	public StateUpdate(List<Entity> updatedEntities) {
		this.updatedEntities = updatedEntities;
	}

	public List<Entity> getUpdatedEntities() {
		return updatedEntities;
	}
}
