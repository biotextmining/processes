package com.silicolife.textmining.processes.ie.re.kineticre.core;

import java.util.HashSet;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.KineticREClassesEnum;

public class REKineticConfigurationClasses {
	
	private Set<IAnoteClass> units;
	private Set<IAnoteClass> values;
	private Set<IAnoteClass> kineticParameters;
	private Set<IAnoteClass> metabolites;
	private Set<IAnoteClass> enzymes;
	private Set<IAnoteClass> organism;
	
	public REKineticConfigurationClasses() {
		this.units = new HashSet<>();
		this.values = new HashSet<>();
		this.kineticParameters= new HashSet<>();
		this.metabolites= new HashSet<>();
		this.enzymes= new HashSet<>();
		this.organism= new HashSet<>();
	}

	public REKineticConfigurationClasses(Set<IAnoteClass> units,Set<IAnoteClass> values,Set<IAnoteClass> kineticParameters,
			Set<IAnoteClass> metabolites,Set<IAnoteClass> enzymes,Set<IAnoteClass> organism) {
		this.units = units;
		this.values = values;
		this.kineticParameters=kineticParameters;
		this.metabolites=metabolites;
		this.enzymes=enzymes;
		this.organism=organism;
	}

	public Set<IAnoteClass> getUnitsClasses() {
		return units;
	}

	public Set<IAnoteClass> getValuesClasses() {
		return values;
	}

	public Set<IAnoteClass> getKineticParametersClasses() {
		return kineticParameters;
	}

	public Set<IAnoteClass> getMetabolitesClasses() {
		return metabolites;
	}

	public Set<IAnoteClass> getEnzymesClasses() {
		return enzymes;
	}

	public Set<IAnoteClass> getOrganismClasses() {
		return organism;
	}
	
	public static KineticREClassesEnum getKineticREClassesEnumGivenAnoteClass(REKineticConfigurationClasses rEKineticConfigurationClasses,IAnoteClass klass)
	{
		if(rEKineticConfigurationClasses.getUnitsClasses().contains(klass))
		{
			return KineticREClassesEnum.Units;
		}
		else if(rEKineticConfigurationClasses.getValuesClasses().contains(klass))
		{
			return KineticREClassesEnum.Values;
		}
		else if(rEKineticConfigurationClasses.getKineticParametersClasses().contains(klass))
		{
			return KineticREClassesEnum.KineticParameters;
		}
		else if(rEKineticConfigurationClasses.getEnzymesClasses().contains(klass))
		{
			return KineticREClassesEnum.Enzymes;
		}
		else if(rEKineticConfigurationClasses.getMetabolitesClasses().contains(klass))
		{
			return KineticREClassesEnum.Metabolites;
		}
		else if(rEKineticConfigurationClasses.getOrganismClasses().contains(klass))
		{
			return KineticREClassesEnum.Organism;
		}
		return KineticREClassesEnum.None;
	}
}
