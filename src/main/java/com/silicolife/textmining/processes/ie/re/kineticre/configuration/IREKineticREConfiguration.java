package com.silicolife.textmining.processes.ie.re.kineticre.configuration;

import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.process.IE.re.IREConfiguration;

public interface IREKineticREConfiguration extends IREConfiguration{

	public Set<IAnoteClass> getUnitsClasses();
	public Set<IAnoteClass> getValuesClasses();
	public Set<IAnoteClass> getKineticParametersClasses();
	public Set<IAnoteClass> getMetabolitesClasses();
	public Set<IAnoteClass> getEnzymesClasses();
	public Set<IAnoteClass> getOrganismClasses();
	public IREKineticAdvancedConfiguration getAdvancedConfiguration();
	
}
