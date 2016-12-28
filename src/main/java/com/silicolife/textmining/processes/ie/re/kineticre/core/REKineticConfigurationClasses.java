package com.silicolife.textmining.processes.ie.re.kineticre.core;

import java.util.HashSet;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
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
	
	public static String convertIntoString(REKineticConfigurationClasses rEKineticConfigurationClasses)
	{
		String result = new String();
		String unit = generateString(rEKineticConfigurationClasses.getUnitsClasses());
		result = result + unit + "|";
		String values = generateString(rEKineticConfigurationClasses.getValuesClasses());
		result = result + values + "|";
		String kineticparameters = generateString(rEKineticConfigurationClasses.getKineticParametersClasses());
		result = result + kineticparameters + "|";
		String enzymes = generateString(rEKineticConfigurationClasses.getEnzymesClasses());
		result = result + enzymes + "|";
		String metabolites = generateString(rEKineticConfigurationClasses.getMetabolitesClasses());
		result = result + metabolites + "|";
		String organisms = generateString(rEKineticConfigurationClasses.getOrganismClasses());
		result = result + organisms;
		return result;
	}
	

	private static String generateString(Set<IAnoteClass> unitsClasses) {
		if(unitsClasses.isEmpty()) 
			return new String();
		String result = new String();
		for(IAnoteClass klass:unitsClasses)
		{
			result = result + klass.getId() + "+";
		}
		return result.substring(0,result.length()-1);
	}

	public static REKineticConfigurationClasses convertIntoREKineticConfigurationClasses(String propValue) {
		String[] splited = propValue.split("\\|");
		if(splited.length > 0)
		{
			Set<IAnoteClass> units = calculateClasses(splited[0]);
			Set<IAnoteClass> values = splited.length>1 ?  calculateClasses(splited[1]) : new HashSet<IAnoteClass>();
			Set<IAnoteClass> kineticParameters = splited.length>2 ? calculateClasses(splited[2]) : new HashSet<IAnoteClass>();
			Set<IAnoteClass> enzymes = splited.length>3 ? calculateClasses(splited[3]): new HashSet<IAnoteClass>();
			Set<IAnoteClass> metabolites = splited.length>4 ? calculateClasses(splited[4]): new HashSet<IAnoteClass>();
			Set<IAnoteClass> organism = splited.length>5 ? calculateClasses(splited[5]): new HashSet<IAnoteClass>();
			return new REKineticConfigurationClasses(units, values, kineticParameters, metabolites, enzymes, organism);
		}
		else
		{
			return new REKineticConfigurationClasses();
		}
	}

	private static Set<IAnoteClass> calculateClasses(String classes) {
		Set<IAnoteClass> classesResult = new HashSet<>();
		
		String[] classesSplited = classes.split("\\+");
		for(String classSplited:classesSplited)
		{
			try {
				IAnoteClass aklass = ClassPropertiesManagement.getClassGivenClassID(Long.valueOf(classSplited));
				if(aklass!=null)
					classesResult.add(aklass);
			} catch (NumberFormatException | ANoteException e) {
			}
		}
		return classesResult;
	}

}
