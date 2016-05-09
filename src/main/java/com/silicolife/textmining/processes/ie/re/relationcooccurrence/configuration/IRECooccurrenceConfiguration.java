package com.silicolife.textmining.processes.ie.re.relationcooccurrence.configuration;

import com.silicolife.textmining.core.interfaces.process.IE.re.IREConfiguration;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.models.RECooccurrenceModelEnum;

public interface IRECooccurrenceConfiguration extends IREConfiguration{
	
	public RECooccurrenceModelEnum getCooccurrenceModelEnum();

}
