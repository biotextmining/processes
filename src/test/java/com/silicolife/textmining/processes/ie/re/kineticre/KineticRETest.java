package com.silicolife.textmining.processes.ie.re.kineticre;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.report.processes.IREProcessReport;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.process.IE.IREProcess;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.IREKineticAdvancedConfiguration;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.IREKineticREConfiguration;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.REKineticAdvancedConfigurationImpl;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.REKineticConfigurationClasses;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.REKineticConfigurationImpl;
import com.silicolife.textmining.processes.ie.re.kineticre.core.KineticRE;

public class KineticRETest {
		
	@Test //RE -> NER
	public void test1() throws ANoteException, InvalidDatabaseAccess, IOException, InvalidConfigurationException {
		DatabaseConnectionInit.init("localhost","3306","kineticre","root","admin");
		// number of NER process, that will be used in RE;
		
		// NER new search08_corpus Ki query_48 pdfs
		//long processID = new Long ("3522218364622074306");
		
		// NER new search08_corpus Ki query_48 pdfs_com29regrasConv
		//long processID = new Long ("4664802390538107611");
		
		// NER new search08_corpus Ki query_48 pdfs_com 33 regrasConv
		//long processID = new Long ("6278537010906148250");
		
		// NER new search08_corpus Ki query_48 PDFs_com 33 regrasConv_altsManual
		//long processID = new Long ("761395225317218037");
		
		//////////////////////////////////////////////
		//////////////////////////////////////////////

		// NER Id EXP Km 46 pdfs__Km query 08_366 pdfs
		//long processID = new Long ("8244767063668385953");
		
		// NER TESTE Km
		long processID = new Long ("4268651946828108937");

		
		
		repeatedInfoMapClasses(processID);
	}

	private void repeatedInfoMapClasses(long processID) throws ANoteException, InvalidConfigurationException {
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
		
		REKineticConfigurationClasses classes = new REKineticConfigurationClasses(units, values, kineticParameters, metabolites, enzymes, organism);
		IIEProcess entityProcess = InitConfiguration.getDataAccess().getProcessByID(processID );
		IREKineticAdvancedConfiguration advanced = new REKineticAdvancedConfigurationImpl();
		IREKineticREConfiguration configuration = new REKineticConfigurationImpl(entityProcess.getCorpus(), entityProcess, classes,advanced );
		IREProcess kineticRE = new KineticRE();
		System.out.println("Execute KineticRE!!!");
		IREProcessReport report = kineticRE.executeRE(configuration);
		System.out.println("Kinetic RE process FINISHED!!!");
	}
	


}
