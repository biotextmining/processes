package com.silicolife.textmining.processes.ie.re.kineticre.core;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;

public class KparamValueUnitBasedRelation {
	private IEntityAnnotation kparam;
	private ValueUnitBasedRelation pairs;
	private float score = 0;
	
	
	public KparamValueUnitBasedRelation(ValueUnitBasedRelation pairs, IEntityAnnotation kparam) {
		this.pairs = pairs;
		this.kparam = kparam;
	}

	public ValueUnitBasedRelation getPairs() {
		return pairs;
	}

	public void setPairs(ValueUnitBasedRelation pairs) {
		this.pairs = pairs;
	}

	public IEntityAnnotation getKparam() {
		return kparam;
	}

	public void setKparam(IEntityAnnotation kparam) {
		this.kparam = kparam;
	}

	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}

}
