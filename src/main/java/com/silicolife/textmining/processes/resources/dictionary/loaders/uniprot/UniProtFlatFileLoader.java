package com.silicolife.textmining.processes.resources.dictionary.loaders.uniprot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

/**
 * @note2
 * 
 * @author Hugo Costa
 *
 */

public class UniProtFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader{
	
	private static Pattern full = Pattern.compile("Full=(.+?)\\s*;");
	private static Pattern shortName = Pattern.compile("Short=(.+?)\\s*;");
	private static Pattern name = Pattern.compile("Name=(.+?)\\s*;");
	private static Pattern synomys = Pattern.compile("Synonyms=(.+?)\\s*;");
	private static Pattern externalID = Pattern.compile("ID\\s(.+?)\\s\\s");
	private static Pattern checkFormat = Pattern.compile("ID\\s(.+?)\\s\\s");
	public final static String uniprot = "UniProt";
	private boolean cancel = false;
	public final static String protein = "Protein";
	
	public static final String propertyOrganism = "organism";

	public UniProtFlatFileLoader()
	{
		super(uniprot);
	}
	
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		super.setResource(configuration.getDictionary());
		File file = configuration.getFlatFile();	
		Properties properties = configuration.getProperties();
		boolean loadExternalIDs = configuration.loadExternalIDs();
		cancel = false;
		getReport().addClassesAdding(1);
		getReport().updateFile(file);
		String line = new String();
		String term = new String();
		Set<String> termSynomns = new HashSet<String>();
		FileReader fr;
		boolean isOrganism = false;
		BufferedReader br;
		String organism="";
		ExternalIDImpl ext;
		if(properties.containsKey(propertyOrganism))
		{
			organism = properties.getProperty(propertyOrganism);
		}		
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		List<IExternalID> externalIDs = new ArrayList<IExternalID>();
		int total = FileHandling.getFileLines(file);
		int lineNumber=0;
		while((line = br.readLine())!=null && !cancel)
		{	
			if(!cancel && line.startsWith("//"))
			{
				if(!cancel && isOrganism)
				{
					super.addElementToBatch(term,protein, termSynomns,externalIDs,null);
				}
				termSynomns = new HashSet<String>();
				term=new String();
				externalIDs = new ArrayList<IExternalID>();
				isOrganism = true;
			}
			else if(!cancel && line.startsWith("OS"))
			{
				isOrganism = organismMatching(organism, line);
			}
			else if(!cancel && line.startsWith("GN"))
			{
				term = findTErmAndSyn(line, term, termSynomns);
			}
			else if(!cancel && line.startsWith("DE"))
			{
				findSyn(line, termSynomns);
			}
			else if(loadExternalIDs && !cancel && line.startsWith("ID"))
			{
				Matcher m = externalID.matcher(line);
				if(!cancel && m.find())
				{
					ext = new ExternalIDImpl(m.group(1).trim(),  new SourceImpl(uniprot));
					externalIDs.add(ext);
				}
			}
			else if(loadExternalIDs && !cancel && line.startsWith("DR"))
			{
				String[] linediv = line.split("\\s");
				if(linediv.length>4)
				{
					ext = new ExternalIDImpl(linediv[4].replace(";", ""),  new SourceImpl(linediv[3].replace(";", "")));
					externalIDs.add(ext);
				}
			}
			if(!cancel && isBatchSizeLimitOvertaken())
			{
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted,getReport());			
			}
			if(!cancel && (lineNumber%500)==0)
			{
				memoryAndProgress(lineNumber, total);
			}	
			lineNumber++;	
		}
		if(!cancel)
		{
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted,getReport());
		}
		else
		{
			getReport().setcancel();
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		getReport().setTime(endTime-startTime);
		return getReport();
	}
	
	private void findSyn(String line, Set<String> termSynomns) {
		Matcher m1;
		Matcher m2;
		m1 = full.matcher(line);
		while(m1.find())
		{
			String prot = m1.group(1);
//			if(line.contains("Uncharacterized")){}
//			else
			{
				termSynomns.add(prot);
			}
		}
		
		m2 = shortName.matcher(line);
		while(m2.find())
		{
			String prot = m2.group(1);
//			if(line.contains("Uncharacterized")){}
//			else
			{
				termSynomns.add(prot);
			}
		}
	}

	private String findTErmAndSyn(String line, String term,
			Set<String> termSynomns) {
		String auxTerm;
		Matcher m1;
		Matcher m2;
		m1 = name.matcher(line);
		if(m1.find())
		{
			auxTerm = m1.group(1);
			term = auxTerm;
		}
		m2 = synomys.matcher(line);
		while(m2.find())
		{
			String synsT = m2.group(1);
			String[] str = synsT.split(", ");
			for(String syn:str)
			{
				termSynomns.add(syn);
			}
		}
		return term;
	}
	
	private boolean organismMatching(String organism, String line) {
		boolean isOrganism;
		if(line.contains(organism))
		{
			isOrganism=true;	
		}
		else
		{
			isOrganism=false;
		}
		return isOrganism;
	}
	
	public boolean checkFile(File file) {
		return checkUniprotFile(file);
	}
	
	public static boolean checkUniprotFile(File file)
	{
		if(!file.isFile())
		{
			return false;
		}
		FileReader fr;
		BufferedReader br;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			String line;
			int i=0;
			Matcher m;
			while((line = br.readLine())!=null&&i<100)
			{
				m = checkFormat.matcher(line);
				if(m.find())
				{
					return true;
				}
				i++;
			}
			
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return false;
	}
	
	public IDictionary getDictionary() {
		return (IDictionary) getResource();
	}

	@Override
	public void stop() {
		this.cancel = true;
	}

}
