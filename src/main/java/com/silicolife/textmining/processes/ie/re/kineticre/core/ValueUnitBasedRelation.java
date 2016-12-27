package com.silicolife.textmining.processes.ie.re.kineticre.core;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;

public class ValueUnitBasedRelation {
	private IEntityAnnotation unit;
	private IEntityAnnotation value;
	private float score = 10;
	private String notes = "";
	private long startRelation;
	private long endRelation;
	
	public ValueUnitBasedRelation(IEntityAnnotation value, IEntityAnnotation unit) {
		super();
		this.unit = unit;
		this.value = value;
	}

	public IEntityAnnotation getUnit() {
		return unit;
	}

	public void setUnit(IEntityAnnotation unit) {
		this.unit = unit;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public IEntityAnnotation getValue() {
		return value;
	}

	public void setValue(IEntityAnnotation value) {
		this.value = value;
	}

	public long getStartRelation() {
		return startRelation;
	}

	public void setStartRelation(long startRelation) {
		this.startRelation = startRelation;
	}

	public long getEndRelation() {
		return endRelation;
	}

	public void setEndRelation(long endRelation) {
		this.endRelation = endRelation;
	}





	
	
	
	
}
