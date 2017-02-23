package com.silicolife.textmining.processes.ie.re.relationcooccurrence.configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessRunStatusConfigurationEnum;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.process.re.REConfigurationImpl;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.Properties;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.RECooccurrence;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.models.RECooccurrenceModelEnum;

public class RECooccurrenceConfiguration extends REConfigurationImpl implements IRECooccurrenceConfiguration{

	public static String reRelationCooccurrenceUID = "re.relationcooccurrence";

	private RECooccurrenceModelEnum model;
	
	public RECooccurrenceConfiguration(ICorpus corpus,ProcessRunStatusConfigurationEnum processRunStatus, IIEProcess entityProcess,RECooccurrenceModelEnum model,boolean useManualCurationFromOtherProcess,
			IIEProcess manualCurationFromOtherProcess) {
		super(RECooccurrence.relationCooccurrence,corpus,build(corpus),processRunStatus, entityProcess,useManualCurationFromOtherProcess,manualCurationFromOtherProcess);
		this.model = model;
	}
	
	private static IIEProcess build(ICorpus corpus)
	{
		String description = RECooccurrence.relationCooccurrence + Utils.SimpleDataFormat.format(new Date());
		String notes = new String();
		IIEProcess reProcess = new IEProcessImpl(corpus, description,notes ,ProcessTypeImpl.getREProcessType(), RECooccurrence.relationCooccurrenceProcessType, new Properties());
		return reProcess;
	}

	public RECooccurrenceModelEnum getCooccurrenceModelEnum() {
		return model;
	}

	@Override
	public Map<String, String> getREProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RECooccurrenceDefaultSettings.MODEL, model.getRelationCooccurrenceModel().getUID());
		return properties;
	}

	@Override
	public String getConfigurationUID() {
		return RECooccurrenceConfiguration.reRelationCooccurrenceUID;
	}

	@Override
	public void setConfigurationUID(String uid) {
		RECooccurrenceConfiguration.reRelationCooccurrenceUID = uid;
		
	}


}
