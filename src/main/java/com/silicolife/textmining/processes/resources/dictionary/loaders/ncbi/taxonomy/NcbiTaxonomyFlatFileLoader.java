package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.taxonomy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.general.AnoteClass;
import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.ResourceElementImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalSources;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public class NcbiTaxonomyFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader {

	private Pattern organismMacthing = Pattern.compile("^\\d+\\s\\|\\s(.*?)\\s\\|");
	private boolean cancel = false;
	private ISource source = new SourceImpl(GlobalSources.ncbitaxonomy);

	public final static String organism = "Organism";

	public NcbiTaxonomyFlatFileLoader() {
		super(GlobalSources.ncbitaxonomy);
	}

	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		cancel = false;
		super.setResource(configuration.getDictionary());
		File file = configuration.getFlatFile();
		getReport().addClassesAdding(1);
		getReport().updateFile(file);
		String line = new String();
		FileReader fr;
		BufferedReader br;
		getReport().addClassesAdding(1);
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		// the first two lines are irrelevant
		line = br.readLine();
		line = br.readLine();
		String sentenceID = "1";
		long start = GregorianCalendar.getInstance().getTimeInMillis();
		int total = FileHandling.getFileLines(file);
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		int lines = 0;
		IResourceElement resourceElementToAdd = new ResourceElementImpl(new String(), new AnoteClass(organism), new ArrayList<IExternalID>(), new ArrayList<String>(), 0, true);
		while ((line = br.readLine()) != null && !cancel) {
			String[] linePieces = line.split("\\|");
			String organismID = linePieces[0].trim();
			if (!organismID.equals(sentenceID)) {	
				resourceElementToAdd.getExternalIDsInMemory().add(new ExternalIDImpl(sentenceID, source));
				Set<String> newSynonyms = addSpecieAbbreviations(resourceElementToAdd.getTerm(), resourceElementToAdd.getSynonyms());
				resourceElementToAdd.getSynonyms().addAll(newSynonyms);
				super.addElementToBatch(resourceElementToAdd);
				sentenceID = organismID;
				resourceElementToAdd = new ResourceElementImpl(new String(), new AnoteClass(organism), new ArrayList<IExternalID>(), new ArrayList<String>(), 0, true);
			}				
			updateResourceElement(linePieces, resourceElementToAdd);
			if (!cancel && isBatchSizeLimitOvertaken()) {
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted, getReport());
			}
			if ((lines % 500) == 0) {
				memoryAndProgress(lines, total);
			}
			lines++;
		}
		if (!cancel) {
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted, getReport());
		} else {
			getReport().setcancel();
		}
		long end = GregorianCalendar.getInstance().getTimeInMillis();
		getReport().setTime(end - start);
		br.close();
		return getReport();

	}
	
	private void updateResourceElement(String[] linePieces,IResourceElement resourceElementToAdd)
	{
		String name = linePieces[1].trim();
		String meaning = linePieces[3].trim();
		switch (meaning) {
			case "scientific name" :
				resourceElementToAdd.setTerm(name);
				break;
			case "common name" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "synonym" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "equivalent name" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "blast name" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "genbank common name" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "genbank synonym" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "genbank anamorph" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "genbank acronym" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "misspelling" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "acronym" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "teleomorph" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			case "anamorph" :
				if(!resourceElementToAdd.getSynonyms().contains(name))
					resourceElementToAdd.getSynonyms().add(name);
				break;
			default :
				break;
		}

	}

	private Set<String> addSpecieAbbreviations(String term, List<String> termSynomns) {
		Set<String> abbreviations = new HashSet<>();
		for(String synonim : termSynomns){
			String abbreviation = convertStringToAbbreviation(synonim);
			if(!abbreviation.isEmpty() && abbreviation.length() > 5){
				abbreviations.add(abbreviation);
			}
		}
		String abb = convertStringToAbbreviation(term);
		if(!abb.isEmpty() && abb.length() > 5){
			abbreviations.add(abb);
		}
		abbreviations.removeAll(termSynomns);
		return abbreviations;
	}

	private String  convertStringToAbbreviation(String specieName) {
		String result = new String();
		String[] partsTerm = specieName.trim().split("\\s");
		if(partsTerm.length==2){
			String firstChar = partsTerm[0].substring(0, 1);
			firstChar = firstChar.toUpperCase();
			result = firstChar + ". "+partsTerm[1];
		}
		return result;
	}

	public boolean checkFile(File file) {

		if (!file.isFile()) {
			return false;
		}
		getReport().updateFile(file);
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}
		String line = null;

		try (BufferedReader br = new BufferedReader(fr)) {
			line = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (line == null) {
			return false;
		} else {
			if (!line.contains("1"))
				return false;
			if (!line.contains("all"))
				return false;
			if (!line.contains("synonym")) {
				return false;
			} else {
				return true;
			}
		}
	}

	public Pattern getOrganismMacthing() {
		return organismMacthing;
	}

	@Override
	public IDictionary getDictionary() {
		// TODO Auto-generated method stub
		return (IDictionary) getResource();
	}

	@Override
	public void stop() {
		this.cancel = true;
	}

	public static boolean checkNCBITaxonomyFile(File file) {
		if (!file.isFile()) {
			return false;
		}
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}
		String line = null;

		try (BufferedReader br = new BufferedReader(fr)) {
			line = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (line == null) {
			return false;
		} else {
			if (!line.contains("1"))
				return false;
			if (!line.contains("all"))
				return false;
			if (!line.contains("synonym")) {
				return false;
			} else {
				return true;
			}
		}
	}
	
//	public static void main(String[] args) throws IOException {
//		File file = new File("S://Projectos//ANote2//Resources//Resources//dictionaries//ncbi taxonomy//names.dmp");
//		FileReader fr = new FileReader(file);
//		BufferedReader br = new BufferedReader(fr);
//		String line;
//		Set<String> meanings = new HashSet<>();
//		while ((line = br.readLine()) != null) {
//			String[] linePieces = line.split("\\|");
//			String meaning = linePieces[3].trim();
//			meanings.add(meaning);
//		}
//		br.close();
//		for(String mean:meanings)
//			System.out.println(mean);
//	}
}
