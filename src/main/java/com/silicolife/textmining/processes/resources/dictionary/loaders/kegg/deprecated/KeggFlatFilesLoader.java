package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalSources;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;
import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.classes.KeggCompound;
import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.classes.KeggEnzymes;
import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.classes.KeggGenes;
import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.classes.KeggReactions;

@Deprecated
public class KeggFlatFilesLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader {

	private Pattern findEntity = Pattern.compile("\\s+(.*)$");
	private Pattern externalID = Pattern.compile("ENTRY\\s+(.+)\\s+?");
	private boolean cancel = false;
	public static final String propertyorganism="Organism";
	private Map<String, AKeggClassLoader> classClassLoader;

	public KeggFlatFilesLoader() {
		super(GlobalSources.kegg);
		classClassLoader = new HashMap<String, AKeggClassLoader>();
		classClassLoader.put(KeggCompound.klass, new KeggCompound());
		classClassLoader.put(KeggEnzymes.klass, new KeggEnzymes());
		classClassLoader.put(KeggGenes.klass, new KeggGenes());
		classClassLoader.put(KeggReactions.klass, new KeggReactions());
	}


	@Override
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException,IOException {
		super.setResource(configuration.getDictionary());
		Properties properties = configuration.getProperties();
		boolean loadExternalIDs = configuration.loadExternalIDs();
		String organism = "";
		if(properties.containsKey(propertyorganism))
		{
			organism = properties.getProperty(propertyorganism);
		}
		cancel = false;
		File file = configuration.getFlatFile();
		if(file.isFile())
		{
			for(AKeggClassLoader klass:classClassLoader.values())
			{
				if(klass.checkFile(file))
				{
					genericLoadFile(file, organism,!organism.equals(""),loadExternalIDs,klass.getClassLoader());
					return getReport();
				}
			}
		}
		return null;
	}


	@Override
	public boolean checkFile(File file) {
		if(!file.isFile())
		{
			for(File fileList : file.listFiles())
			{
				for(AKeggClassLoader klass:classClassLoader.values())
				{
					if(klass.checkFile(fileList))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private void genericLoadFile(File file, String organism,boolean loadExternalIDs, boolean useOrganismFilter, String klass) throws ANoteException, IOException {
		int total = getTotalEntries(file);
		int step = 0;
		String line = new String();
		String term = new String();
		Set<String> termSynomns = new HashSet<String>();
		FileReader fr;
		boolean isOrganism = false;
		boolean inorganism = false;
		BufferedReader br;
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		List<IExternalID> externalIDs = new ArrayList<>();
		while ((line = br.readLine()) != null && !cancel) {
			if (line.startsWith("///")) {
				if (useOrganismFilter && (isOrganism || !inorganism)) {
					super.addElementToBatch(term, klass, termSynomns, externalIDs, 0);
				}
				termSynomns = new HashSet<String>();
				term = new String();
				isOrganism = false;
				inorganism = false;
				externalIDs = new ArrayList<IExternalID>();
				if (step % 100 == 0) {
					memoryAndProgress(step, total);
				}
				step++;
			} else if (useOrganismFilter && line.startsWith("  ORGANISM")) {
				isOrganism = organismMatching(organism, line);
				inorganism = true;
			} else if (line.startsWith("NAME ")) {
				term = finTerm(line);
				findSyn(termSynomns, br);
			} else if (loadExternalIDs && line.startsWith("ENTRY")) {

				Matcher m = this.externalID.matcher(line);
				if (m.find()) {
					String externalID = m.group(1);
					String[] externals = externalID.split("  ");
					externalID = externals[0];
					ISource source = new SourceImpl(GlobalSources.kegg);
					externalIDs.add(new ExternalIDImpl(externalID, source));
				}
			}
			if (!cancel && isBatchSizeLimitOvertaken()) {
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted, getReport());
			}
		}
		if (!cancel && isBatchSizeLimitOvertaken()) {
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted, getReport());
		}
	}

	protected int getTotalEntries(File file) throws IOException {
		BufferedReader br;
		FileReader fr = new FileReader(file);
		br = new BufferedReader(fr);
		String line;
		int result = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("///")) {
				result++;
			}
		}
		br.close();
		return result;

	}


	public Pattern getExternalID() {
		return externalID;
	}

	public String finTerm(String line) {
		String term;
		Matcher m1;
		m1 = findEntity.matcher(line);
		m1.find();

		term = m1.group(1);
		if (term.endsWith(";")) {
			term = term.substring(0, term.length() - 1);
		}
		return term;
	}

	public void findSyn(Set<String> termSynomns, BufferedReader br) throws IOException {
		boolean namesZone = true;
		String line;
		Matcher m1;
		while ((line = br.readLine()) != null && namesZone) {
			if (!line.startsWith("  ")) {
				namesZone = false;
			} else {
				m1 = findEntity.matcher(line);
				m1.find();
				String syn = m1.group(1);
				if (syn.endsWith(";")) {
					syn = syn.substring(0, syn.length() - 1);
				}
				termSynomns.add(syn);
			}
		}
	}

	public boolean organismMatching(String organism, String line) {
		boolean isOrganism;
		if (line.contains(organism) || organism.equals("")) {
			isOrganism = true;
		} else {
			isOrganism = false;
		}
		return isOrganism;
	}

	public Pattern getFindEntity() {
		return findEntity;
	}

	public IDictionary getDictionary() {
		return (IDictionary) getResource();
	}

	public void stop() {
		this.cancel = true;
	}


	public static boolean checkKeggFile(File file, AKeggClassLoader klassChhoser) {
		return klassChhoser.checkFile(file);
	}


}
