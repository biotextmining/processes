package com.silicolife.textmining.processes.ie.re.kineticre.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.process.re.REConfigurationImpl;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.ie.re.kineticre.core.oldversions.KineticREtriples;

public class REKineticConfigurationImpl extends REConfigurationImpl implements IREKineticREConfiguration{

	private REKineticConfigurationClasses classes;
	private IREKineticAdvancedConfiguration advancedConfigurations;

	public REKineticConfigurationImpl(ICorpus corpus,IIEProcess entityProcess,REKineticConfigurationClasses  classes,IREKineticAdvancedConfiguration advancedConfigurations) {
		super(KineticREtriples.kineticREDescrition, corpus, entityProcess,false,null);
		this.classes=classes;
		this.advancedConfigurations=advancedConfigurations;
	}

	@Override
	public Map<String, String> getREProperties() {
		return new HashMap<String, String>();
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
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IREKineticAdvancedConfiguration getAdvancedConfiguration() {
		return advancedConfigurations;
	}
}
