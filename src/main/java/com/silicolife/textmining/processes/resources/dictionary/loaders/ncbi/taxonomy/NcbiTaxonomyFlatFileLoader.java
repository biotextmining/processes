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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalSources;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public class NcbiTaxonomyFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader {

	private Pattern idsentence = Pattern.compile("^(\\d+)\\s");
	private Pattern organismMacthing = Pattern.compile("^\\d+\\s\\|\\s(.*?)\\s\\|");
	private boolean cancel = false;
	// private Pattern firstline =
	// Pattern.compile("1\\s|\\sall\\s|\\s|\\ssynonym\\s|");
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
		String term = new String();
		Set<String> termSynomns = new HashSet<String>();
		Matcher m1, m2;
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
		while ((line = br.readLine()) != null && !cancel) {

			if (!line.startsWith(sentenceID)) {
				List<IExternalID> externalIDs = new ArrayList<IExternalID>();
				ISource source = new SourceImpl(GlobalSources.ncbitaxonomy);
				externalIDs.add(new ExternalIDImpl(String.valueOf(sentenceID), source));
				termSynomns = addSpecieAbbreviations(term, termSynomns);
				super.addElementToBatch(term, organism, termSynomns, externalIDs, 0);
				termSynomns = new HashSet<String>();
				term = new String();
				m1 = getIdsentence().matcher(line);
				m1.find();
				sentenceID = m1.group(1);
				m2 = getOrganismMacthing().matcher(line);
				if (m2.find()) {
					term = m2.group(1);
				}

			} else {
				if (line.contains("authority")) {

				} else {
					m2 = getOrganismMacthing().matcher(line);
					m2.find();
					termSynomns.add(m2.group(1));
				}
			}
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

	private Set<String> addSpecieAbbreviations(String term, Set<String> termSynomns) {
		Set<String> abbreviations = new HashSet<>();
		for(String synonim : termSynomns){
			String abbreviation = convertStringToAbbreviation(synonim);
			if(!abbreviation.isEmpty()){
				abbreviations.add(abbreviation);
			}
		}
		String abb = convertStringToAbbreviation(term);
		if(!abb.isEmpty()){
			abbreviations.add(abb);
		}
		termSynomns.addAll(abbreviations);
		return termSynomns;
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

	public Pattern getIdsentence() {
		return idsentence;
	}

	public void setIdsentence(Pattern idsentence) {
		this.idsentence = idsentence;
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

}
