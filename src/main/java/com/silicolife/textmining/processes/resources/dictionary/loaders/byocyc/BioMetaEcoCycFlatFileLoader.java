package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.HTMLCodes;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalSources;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes.BioMetaEcoCycCompound;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes.BioMetaEcoCycEnzyme;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes.BioMetaEcoCycGene;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes.BioMetaEcoCycPathways;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes.BioMetaEcoCycProtein;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes.BioMetaEcoCycReaction;

public class BioMetaEcoCycFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader {
	
	static Logger logger = Logger.getLogger(BioMetaEcoCycFlatFileLoader.class.getName());

	
	private Pattern name = Pattern.compile("^COMMON-NAME - (.*)");
	private Pattern syn = Pattern.compile("^SYNONYMS - (.*)");
	private Pattern externalID = Pattern.compile("^UNIQUE-ID - (.*)");
	private Pattern externalID2 = Pattern.compile("^DBLINKS - \\((.*) \"(.*)\"");
	protected String cleanTag = "<.*?>";
	protected String classe;
	private boolean cancel = false;
	private Set<ABioCycMetaCycFileImportAvailable> classFilesLoader;
	
	private HTMLCodes htmlCodes;

	public static final String propertyChangeToGreekLetters = "Change Greek characters";
	
	public BioMetaEcoCycFlatFileLoader()
	{
		super(GlobalSources.biocyc);		
		this.htmlCodes = new HTMLCodes();
		this.classFilesLoader = new HashSet<>();
		this.classFilesLoader.add(new BioMetaEcoCycCompound());
		this.classFilesLoader.add(new BioMetaEcoCycEnzyme());
		this.classFilesLoader.add(new BioMetaEcoCycGene());
		this.classFilesLoader.add(new BioMetaEcoCycPathways());
		this.classFilesLoader.add(new BioMetaEcoCycProtein());
		this.classFilesLoader.add(new BioMetaEcoCycReaction());
	}
	
	
	@Override
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException,IOException {
		super.setResource(configuration.getDictionary());
		Properties properties = configuration.getProperties();
		boolean loadExternalIDs = configuration.loadExternalIDs();
		boolean changeGreekcharacters = false;
		if(properties.containsKey(propertyChangeToGreekLetters))
		{
			changeGreekcharacters = Boolean.valueOf(properties.getProperty(propertyChangeToGreekLetters));
		}
		cancel = false;
		File file = configuration.getFlatFile();
		Map<ABioCycMetaCycFileImportAvailable, File> klassFile = new java.util.HashMap<>();
		if(file.isFile())
		{
			checkFileLoader(file, klassFile);
		}
		else
		{
			checkDirectory(file, klassFile);
		}		
		int totalNumberOFLines = calculateNumperOflines(klassFile.values());
		long startime = GregorianCalendar.getInstance().getTimeInMillis();
		int point = 0;
		for(ABioCycMetaCycFileImportAvailable klasssLOader:klassFile.keySet())
		{
			File fileupload = klassFile.get(klasssLOader);
			genericLoadFile(getReport(),klasssLOader.getClassLoader(),fileupload,loadExternalIDs,changeGreekcharacters,point,totalNumberOFLines);
			point = point +FileHandling.getFileLines(fileupload);
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		getReport().setTime(endTime-startime);
		return getReport();	
	}


	private int calculateNumperOflines(Collection<File> values) throws IOException {
		int result = 0;
		for(File file:values)
		{
			result = result +FileHandling.getFileLines(file);
		}
		return result;
	}


	private void checkDirectory(File file,
			Map<ABioCycMetaCycFileImportAvailable, File> klassFile) {
		for(File fileList : file.listFiles())
		{
			for(ABioCycMetaCycFileImportAvailable klass:classFilesLoader)
			{
				if(klass.checkFile(fileList))
				{
					klassFile.put(klass, fileList);
				}
			}
		}
	}


	private void checkFileLoader(File file,
			Map<ABioCycMetaCycFileImportAvailable, File> klassFile) {
		for(ABioCycMetaCycFileImportAvailable klass:classFilesLoader)
		{
			if(klass.checkFile(file))
			{
				klassFile.put(klass, file);
			}
		}
	}

	@Override
	public boolean checkFile(File file) {
		if(file.isFile())
		{
			for(ABioCycMetaCycFileImportAvailable klass:classFilesLoader)
			{
				if(klass.checkFile(file))
				{
					return true;
				}
			}
		}
		else
		{
			for(File fileList : file.listFiles())
			{
				for(ABioCycMetaCycFileImportAvailable klass:classFilesLoader)
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

	
	public static boolean cheackGenericFile(File file,String type)
	{
		if(!file.isFile())
		{
			return false;
		}
		FileReader fr;
		BufferedReader br;
		String line;
		int i=0;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			while(((line = br.readLine())!=null) && i<200)
			{	
				if(line.equals(type))
				{
					br.close();
					fr.close();
					return true;
				}
				i++;
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	

	protected void updateSynonyms(String line, Set<String> termSynomns, boolean changegreekcaracters) {
		String synonym;
		Matcher m = syn.matcher(line);
		m.find();
		synonym = m.group(1).replaceAll(cleanTag, "");
		if(changegreekcaracters)
			synonym = htmlCodes.cleanString(synonym);
		termSynomns.add(synonym);
	}

	protected String findTerm(String line, boolean changegreekcaracters) {
		String term;
		Matcher m = name.matcher(line);
		m.find();
		term = m.group(1).replaceAll(cleanTag, "");
		if(changegreekcaracters)
			term = htmlCodes.cleanString(term);
		return term;
	}
	
	




	public Pattern getExternalID() {
		return externalID;
	}

	public IDictionary getDictionary() {
		return (IDictionary) getResource();
	}
	
	
	public Pattern getExternalID2() {
		return externalID2;
	}
	
	public void stop() {
		this.cancel = true;
	}
	
	public void genericLoadFile(IResourceUpdateReport report,String klass,File file,boolean imporatExternalIDs,boolean changeGreekcharacters,
			int point,int total) throws ANoteException, IOException
	{
		report.addClassesAdding(1);
		String line = new String();
		String term = new String();
		Set<String> termSynomns = new HashSet<String>();
		FileReader fr;
		BufferedReader br;
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		List<IExternalID> externalIDs = new ArrayList<IExternalID>();
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		int lineNumber=point;	
		int totalLines = total;
		while((line = br.readLine())!=null && !cancel)
		{	
			if(line.startsWith("//"))
			{
				super.addElementToBatch(term, klass, termSynomns, externalIDs, 0);
				termSynomns = new HashSet<String>();
				externalIDs = new ArrayList<IExternalID>();
				term=new String();

			}
			else if(line.startsWith("COMMON-NAME"))
			{
				term = findTerm(line,changeGreekcharacters);
			}
			else if(line.startsWith("SYNONYMS"))
			{
				updateSynonyms(line, termSynomns,changeGreekcharacters);

			}
			else if(imporatExternalIDs && line.startsWith("UNIQUE-ID"))
			{	
				Matcher m = getExternalID().matcher(line);
				if(m.find())
				{
					ISource source = new SourceImpl(GlobalSources.biocyc);
					IExternalID e = new ExternalIDImpl(m.group(1),source);
					externalIDs.add(e);
				}
			}
			else if(imporatExternalIDs && line.startsWith("DBLINKS"))
			{	
				Matcher m = getExternalID2().matcher(line);
				if(m.find())
				{
					ISource source = new SourceImpl(m.group(1));

					IExternalID e = new ExternalIDImpl(m.group(2),source);
					externalIDs.add(e);
				}
			}
			if((lineNumber%500)==0)
			{
				memoryAndProgress(lineNumber, totalLines);
			}
			if(!cancel && isBatchSizeLimitOvertaken())
			{
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted,report);			
			}
			lineNumber++;
		}
		if(!cancel)
		{
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted,report);			
		}
		else
		{
			report.setcancel();
		}
		br.close();
	}


	public static boolean checkBioMetaCycFile(File file, ABioCycMetaCycFileImportAvailable klassChhoser) {
		return klassChhoser.checkFile(file);
	}
}

	
