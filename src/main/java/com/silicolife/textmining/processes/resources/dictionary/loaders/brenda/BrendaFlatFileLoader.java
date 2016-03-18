package com.silicolife.textmining.processes.resources.dictionary.loaders.brenda;

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
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalSources;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

/**
 * 
 * Testes : 629s
 * Termos : 4000
 * Syn    : 41000
 * @author Hugo
 * 
 * O externalID tem de ser uma string
 *
 */

public class BrendaFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader{
	
	private Pattern name = Pattern.compile("^RN\\s(.*)$");
	private Pattern syn1 = Pattern.compile("^SY\\s\\s(.*)$");
	private Pattern syn2 = Pattern.compile("^SY\\s#.*#\\s(.*)\\s<");
	private Pattern externalID = Pattern.compile("^ID\\s(.+)$");
	private Pattern externalID2 = Pattern.compile("^CR\\s(.+)$");
	private boolean cancel = false;
	public static String propertyorganism = "Organism";
	
	public final static String enzymes = "Enzyme";

	
	public BrendaFlatFileLoader()
	{
		super(GlobalSources.brenda);
	}
	
	public boolean checkFile(File file) {
		if(!file.isFile())
		{
			return false;
		}
		getReport().updateFile(file);
		FileReader fr;
		BufferedReader br;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			int i=0;
			String line;
			while((line = br.readLine())!=null&&i<200)
			{	
				if(line.contains("RECOMMENDED_NAME"))
				{
					br.close();
					return true;
				}
				i++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException 
	{
		super.setResource(configuration.getDictionary());
		Properties properties = configuration.getProperties();
		boolean loadExternalIDs = configuration.loadExternalIDs();
		cancel = false;
		File file = configuration.getFlatFile();
		String organism = "";
		if(properties.containsKey(propertyorganism))
		{
			organism  = properties.getProperty(propertyorganism);
		}
		String line = new String();
		String term = new String();
		Set<String> termSynomns = new HashSet<String>();
		FileReader fr;
		boolean isOrganism = false;
		BufferedReader br;
		getReport().updateFile(file);
		getReport().addClassesAdding(1);
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		int lineNumber=0;
		int getLines = FileHandling.getFileLines(file);
		List<IExternalID> externalIds = new ArrayList<IExternalID>();
		long startime = GregorianCalendar.getInstance().getTimeInMillis();
		while((line = br.readLine())!=null && !cancel)
		{	
			if(!cancel && line.startsWith("///"))
			{
				if(isOrganism)
				{
					super.addElementToBatch(term, enzymes, termSynomns, externalIds, 0);
				}
				termSynomns = new HashSet<String>();
				term=new String();
				externalIds= new ArrayList<IExternalID>();
				isOrganism = false;
			}
			else if(line.startsWith("PR"))
			{
				if(organism.isEmpty() || organismMatching(organism, line))
				{
					isOrganism=true;
				}
			}
			else if(line.startsWith("RN")&&!line.contains("#"))
			{
				term = findTerm(line);
			}
			else if(line.startsWith("SYN")||line.startsWith("SYS"))
			{

			}
			else if(line.startsWith("SY"))
			{
				findSyn(line, termSynomns);
			}
			else if(line.startsWith("ID") && loadExternalIDs)
			{
				Matcher m1 = this.externalID.matcher(line);
				if(m1.find())
				{
					ISource source = new SourceImpl(GlobalSources.brenda);
					IExternalID e = new ExternalIDImpl(m1.group(1),source);
					externalIds.add(e);	
				}
			}
			else if(line.startsWith("CR") && loadExternalIDs)
			{
				if(!line.contains("#"))
				{
					Matcher m1 = this.externalID2.matcher(line);
					if(m1.find())
					{
						ISource source = new SourceImpl(GlobalSources.cas);
						IExternalID e = new ExternalIDImpl(m1.group(1),source);
						externalIds.add(e);	
					}
				}
			}
			if(!cancel && isBatchSizeLimitOvertaken())
			{
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted,getReport());			
			}
			if((lineNumber%500)==0)
			{
				memoryAndProgress(lineNumber, getLines);
			}
			lineNumber++;
		}
		if(!cancel)
		{
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted,getReport());			
		}
		br.close();
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		getReport().setTime(endTime-startime);
		return getReport();
	}

	public Pattern getExternalID2() {
		return externalID2;
	}

	public Pattern getExternalID() {
		return externalID;
	}

	public void findSyn(String line,Set<String> termSynomns) throws IOException {
		Matcher m1;
		if(line.contains("#")){ m1 = syn2.matcher(line);}
		else
		{
			m1 = syn1.matcher(line);
		}
		if(m1.find())
			termSynomns.add(m1.group(1));

	}
	
	public String findTerm(String line) {
		String term;
		Matcher m1;
		m1 = name.matcher(line);
		m1.find();
		term=m1.group(1);
		return term;
	}
	
	public boolean organismMatching(String organism, String line) {
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

	public IDictionary getDictionary() {
		return (IDictionary) getResource();
	}

	@Override
	public void stop() {
		this.cancel = true;
	}

	public static boolean checkBrendaFile(File file) {
		if(!file.isFile())
		{
			return false;
		}
		FileReader fr;
		BufferedReader br;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			int i=0;
			String line;
			while((line = br.readLine())!=null&&i<200)
			{	
				if(line.contains("RECOMMENDED_NAME"))
				{
					br.close();
					return true;
				}
				i++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
