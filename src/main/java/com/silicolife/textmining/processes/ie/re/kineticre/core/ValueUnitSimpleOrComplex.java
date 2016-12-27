package com.silicolife.textmining.processes.ie.re.kineticre.core;

import java.util.ArrayList;
import java.util.List;

public class ValueUnitSimpleOrComplex {

	/**
	 * If size=1 -> Simple ValueUnitBasedRelation
	 * 
	 */
	private float score = 0;
	private long startRelation;
	private long endRelation;
	private List<ValueUnitBasedRelation>  valueUnitBasedRelationListSorted;
	
	public ValueUnitSimpleOrComplex(ValueUnitBasedRelation valueUnitBasedRelation) {
		this.valueUnitBasedRelationListSorted = new ArrayList<>(valueUnitBasedRelationListSorted);
		this.valueUnitBasedRelationListSorted.add(valueUnitBasedRelation);
	}
	
	public ValueUnitSimpleOrComplex(List<ValueUnitBasedRelation> valueUnitBasedRelationListSorted) {
		this.valueUnitBasedRelationListSorted = valueUnitBasedRelationListSorted;
	}
	
	public long getStartIndex() {
		return valueUnitBasedRelationListSorted.get(0).getValue().getStartOffset();
	}
	
	public long getEndIndex() {
		return valueUnitBasedRelationListSorted.get(valueUnitBasedRelationListSorted.size()-1).getUnit().getEndOffset();

	}
	
	public float getScore() {
		return score;
	}
	
	public float setScore(float score) {
		return score = valueUnitBasedRelationListSorted.size() * 10;
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
	
	public List<ValueUnitBasedRelation> getValueUnitBasedRelationListSorted() {
		return valueUnitBasedRelationListSorted;
	}

	public void setValueUnitBasedRelationListSorted(List<ValueUnitBasedRelation> valueUnitBasedRelationListSorted) {
		this.valueUnitBasedRelationListSorted = valueUnitBasedRelationListSorted;
	}

}
