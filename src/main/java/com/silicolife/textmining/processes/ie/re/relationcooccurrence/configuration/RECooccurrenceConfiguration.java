package com.silicolife.textmining.processes.ie.re.relationcooccurrence.configuration;

import java.util.HashMap;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.process.re.REConfigurationImpl;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.RECooccurrence;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.models.IRECooccurrenceSentenceModel;

public class RECooccurrenceConfiguration extends REConfigurationImpl implements IRECooccurrenceConfiguration{

	private IRECooccurrenceSentenceModel model;
	
	public RECooccurrenceConfiguration(ICorpus corpus, IIEProcess entityProcess,IRECooccurrenceSentenceModel model,boolean useManualCurationFromOtherProcess,
			IIEProcess manualCurationFromOtherProcess) {
		super(RECooccurrence.relationCooccurrence,corpus, entityProcess,useManualCurationFromOtherProcess,manualCurationFromOtherProcess);
		this.model = model;
	}

	public IRECooccurrenceSentenceModel getCooccurrenceModel() {
		return model;
	}

	@Override
	public Map<String, String> getREProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RECooccurrenceDefaultSettings.MODEL, model.getUID());
		return properties;
	}


}
