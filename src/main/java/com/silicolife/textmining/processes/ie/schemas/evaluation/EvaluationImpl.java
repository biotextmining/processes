package com.silicolife.textmining.processes.ie.schemas.evaluation;

import com.silicolife.textmining.core.interfaces.process.utils.IEvaluation;

public class EvaluationImpl implements IEvaluation{
	
	private float precision;
	private float recall;
	private float fscore;

	
	public EvaluationImpl(float precision,float recall,float fscore)
	{
		this.precision = precision;
		this.recall = recall;
		this.fscore = fscore;
	}

	public float getPrecision() {
		return precision;
	}

	public float getRecall() {
		return recall;
	}

	public float getFscore() {
		return fscore;
	}

}
