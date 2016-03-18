package com.silicolife.textmining.processes.ie.schemas.evaluation.re;

import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.report.ReportImpl;
import com.silicolife.textmining.core.interfaces.core.report.processes.evaluation.IRESchemaEvaluationReport;
import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;
import com.silicolife.textmining.core.interfaces.process.IE.re.IRESchemaEvaluation;

public class RESchemasEvaluationReportImpl extends ReportImpl implements IRESchemaEvaluationReport{

	private IRESchemaEvaluation evaluation;
	private IRESchema goldenStandard;
	private IRESchema toCompare;

	
	
	public RESchemasEvaluationReportImpl(IRESchemaEvaluation evaluation,IRESchema goldenStandard,IRESchema toCompare) {
		super(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.re.evaluation.report"));
		this.evaluation = evaluation;
		this.goldenStandard = goldenStandard;
		this.toCompare = toCompare;
	}

	public IRESchemaEvaluation getEvaluation() {
		return evaluation;
	}

	public IRESchema getGoldenStandardRESchema() {
		return goldenStandard;
	}

	public IRESchema getRESchemaCompared() {
		return toCompare;
	}

}
