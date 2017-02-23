package com.silicolife.textmining.processes.ie.re.kineticre.configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessRunStatusConfigurationEnum;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.process.re.REConfigurationImpl;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.Properties;
import com.silicolife.textmining.processes.ie.re.kineticre.core.REKineticConfigurationClasses;
import com.silicolife.textmining.processes.ie.re.kineticre.core.oldversions.KineticREtriples;

public class REKineticConfigurationImpl extends REConfigurationImpl implements IREKineticREConfiguration{

	private REKineticConfigurationClasses classes;
	private IREKineticAdvancedConfiguration advancedConfigurations;

	public REKineticConfigurationImpl(ICorpus corpus,IIEProcess entityProcess,ProcessRunStatusConfigurationEnum processRunStatusConfigurationEnum,REKineticConfigurationClasses  classes,IREKineticAdvancedConfiguration advancedConfigurations) {
		super(KineticREtriples.kineticREDescrition, corpus,build(corpus),processRunStatusConfigurationEnum, entityProcess,false,null);
		this.classes=classes;
		this.advancedConfigurations=advancedConfigurations;
	}
	
	private static IIEProcess build(ICorpus corpus)
	{
		String name = KineticREtriples.kineticREDescrition+" "+Utils.SimpleDataFormat.format(new Date());
		String notes = new String();
		Properties properties = new Properties();
		IIEProcess reProcess = new IEProcessImpl(corpus,name,notes,ProcessTypeImpl.getREProcessType(), KineticREtriples.relationProcessType, properties);
		return reProcess;
	}

	@Override
	public Map<String, String> getREProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(KineticREDefaultSettings.CLASSESMAPPING, REKineticConfigurationClasses.convertIntoString(classes));
		return properties;
	}

	@Override
	public Set<IAnoteClass> getUnitsClasses() {
		return classes.getUnitsClasses();
	}

	@Override
	public Set<IAnoteClass> getValuesClasses() {
		return classes.getValuesClasses();
	}

	@Override
	public Set<IAnoteClass> getKineticParametersClasses() {
		return classes.getKineticParametersClasses();
	}

	@Override
	public Set<IAnoteClass> getMetabolitesClasses() {
		return classes.getMetabolitesClasses();
	}

	@Override
	public Set<IAnoteClass> getEnzymesClasses() {
		return classes.getEnzymesClasses();
	}

	@Override
	public Set<IAnoteClass> getOrganismClasses() {
		return classes.getOrganismClasses();
	}

	@Override
	public String getConfigurationUID() {
		return "kineticre";
	}


	@Override
	public IREKineticAdvancedConfiguration getAdvancedConfiguration() {
		return advancedConfigurations;
	}
}
