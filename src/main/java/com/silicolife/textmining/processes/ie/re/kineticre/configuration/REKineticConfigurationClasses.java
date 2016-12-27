package com.silicolife.textmining.processes.ie.re.kineticre.configuration;

import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;

public class REKineticConfigurationClasses {
	
	private Set<IAnoteClass> units;
	private Set<IAnoteClass> values;
	private Set<IAnoteClass> kineticParameters;
	private Set<IAnoteClass> metabolites;
	private Set<IAnoteClass> enzymes;
	private Set<IAnoteClass> organism;

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
}
