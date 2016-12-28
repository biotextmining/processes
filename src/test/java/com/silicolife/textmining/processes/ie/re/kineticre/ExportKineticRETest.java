package com.silicolife.textmining.processes.ie.re.kineticre;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.schemas.RESchemaImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.ie.re.kineticre.core.REKineticConfigurationClasses;
import com.silicolife.textmining.processes.ie.re.kineticre.io.ExportKineticResultsTOCSV;

public class ExportKineticRETest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","kineticre","root","admin");

		long processID = 9030444517125912921L;
		String fileToExport = processID + ".tsv";

		IRESchema reSchema = new RESchemaImpl(InitConfiguration.getDataAccess().getProcessByID(processID ));
		
		// Mapeamento feito pelo utilizador: entre as classes usadas no NER e as que precisa pra o RE
		Set<IAnoteClass> units = new HashSet<IAnoteClass>();
		units.add(ClassPropertiesManagement.getClassIDClassName("units"));
		units.add(ClassPropertiesManagement.getClassIDClassName("unit_rule"));
		Set<IAnoteClass> values = new HashSet<IAnoteClass>();
		values.add(ClassPropertiesManagement.getClassIDClassName("value_rule"));
		Set<IAnoteClass> kineticParameters = new HashSet<IAnoteClass>();
		kineticParameters.add(ClassPropertiesManagement.getClassIDClassName("Kparameters"));
		Set<IAnoteClass> metabolites = new HashSet<IAnoteClass>();
		metabolites.add(ClassPropertiesManagement.getClassIDClassName("metabolite"));
		Set<IAnoteClass> enzymes = new HashSet<IAnoteClass>();
		enzymes.add(ClassPropertiesManagement.getClassIDClassName("Enzyme"));
		enzymes.add(ClassPropertiesManagement.getClassIDClassName("enzyme_rule"));
		Set<IAnoteClass> organism = new HashSet<IAnoteClass>();
		organism.add(ClassPropertiesManagement.getClassIDClassName("Organism"));
		REKineticConfigurationClasses classConfiguration = new REKineticConfigurationClasses(units, values, kineticParameters, metabolites, enzymes, organism);
		ExportKineticResultsTOCSV export = new ExportKineticResultsTOCSV();
		export.export(fileToExport, reSchema, classConfiguration);
	}

}
