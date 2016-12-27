package com.silicolife.textmining.processes.ie.re.kineticre.core;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;

public class KparamValueUnitSimpleOrComplex {
	private IEntityAnnotation kparam;
	private ValueUnitSimpleOrComplex simpleORcomplexPairs;
	
	public ValueUnitSimpleOrComplex getSimpleORcomplexPairs() {
		return simpleORcomplexPairs;
	}

	public void setSimpleORcomplexPairs(ValueUnitSimpleOrComplex simpleORcomplexPairs) {
		this.simpleORcomplexPairs = simpleORcomplexPairs;
	}

	private float score = 0;
	
	
	public KparamValueUnitSimpleOrComplex(ValueUnitSimpleOrComplex simpleORcomplexPairs, IEntityAnnotation kparam) {
		this.simpleORcomplexPairs = simpleORcomplexPairs;
		this.kparam = kparam;
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
