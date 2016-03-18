package com.silicolife.textmining.processes.resources.ontology.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.dataaccess.database.schema.TableResourcesElements;
import com.silicolife.textmining.core.datastructures.general.AnoteClass;
import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.report.resources.ResourceUpdateReportImpl;
import com.silicolife.textmining.core.datastructures.resources.ResourceElementImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.resources.ontology.loaders.OntologicalClass;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.ontologies.IOntologyLoader;
import com.silicolife.textmining.core.interfaces.resource.ontologies.configuration.IOntologyLoaderConfiguration;


public class OBOOntologyLoader extends DictionaryLoaderHelp implements IOntologyLoader{
	
	private BufferedReader br;
	private boolean cancel = false;
	
	public OBOOntologyLoader()
	{
		super(".obo");
	}
	
	private Map<String,OntologicalClass> loadFile(IOntologyLoaderConfiguration configuration) throws IOException
	{
		FileReader fr;
		BufferedReader br;
		fr = new FileReader(configuration.getFilePath());
		br = new BufferedReader(fr);		
		String lineread,id = new String(),name = new String(),def = new String();
		List<String> isA = new ArrayList<String>();
		List<String> syns = new ArrayList<String>();
		List<IExternalID> externalIds = new ArrayList<IExternalID>();
		OntologicalClass classe;
		List<String> block  = new ArrayList<String>();
		List<String> partof = new ArrayList<String>();
		Map<String,OntologicalClass> ontologicatermIDDetails = new HashMap<String, OntologicalClass>();;
		while((lineread = br.readLine())!=null && !cancel)
		{
			if(lineread.isEmpty() && !cancel)
			{
				// Treat Block Data
				if(!block.isEmpty() && block.get(0).contains("[Term]"))
				{
					for(String blockpiece:block)
					{
						if(blockpiece.startsWith("id: "))
						{
							id = blockpiece.substring(4);
						}
						else if(blockpiece.startsWith("name:"))
						{
							name = blockpiece.substring(6);
						}
						else if(blockpiece.startsWith("def:"))
						{
							def = blockpiece.substring(5);
						}
						else if(blockpiece.startsWith("is_a:"))
						{
							if(blockpiece.contains("!"))
								isA.add(blockpiece.substring(6, blockpiece.indexOf("!")-1));
							else
								isA.add(blockpiece.substring(6));
						}
						else if(blockpiece.startsWith("synonym:"))
						{
							String newSynonym = blockpiece.substring(blockpiece.indexOf('\"')+1,blockpiece.lastIndexOf('\"'));
							if(newSynonym.length()>TableResourcesElements.mimimumSynonymSize && newSynonym.length()<TableResourcesElements.synonymSize)
								syns.add(blockpiece.substring(blockpiece.indexOf('\"')+1,blockpiece.lastIndexOf('\"')));
						}
						else if(blockpiece.startsWith("is_obsolete"))
						{
							id = "";
						}
						else if(blockpiece.startsWith("relationship: part_of"))
						{
							if(blockpiece.contains("!"))
								partof.add(blockpiece.substring(22, blockpiece.indexOf("!")-1));
							else
								partof.add(blockpiece.substring(22));
						}
						else if(blockpiece.startsWith("xref:"))
						{
							String source;
							if(blockpiece.lastIndexOf('\"')!=-1)
							{
								if(blockpiece.lastIndexOf(':')!=-1)
								{
									source = blockpiece.substring(5,blockpiece.lastIndexOf(':'));
									if(blockpiece.indexOf(':')!=-1 && blockpiece.indexOf('\"')!=-1)
									{
										int secondTwoPoints = blockpiece.indexOf(':')+1;
										int lastAspas = blockpiece.indexOf('\"')-1;
										String externalID = blockpiece.substring(secondTwoPoints,lastAspas);
										externalIds.add(new ExternalIDImpl(externalID, new SourceImpl(source)));
									}
								}
							}
							else
							{
								if(blockpiece.lastIndexOf(':')!=-1)
								{
									source = blockpiece.substring(5,blockpiece.lastIndexOf(':'));
									String externalID = blockpiece.substring(blockpiece.lastIndexOf(':')+1);
									externalIds.add(new ExternalIDImpl(externalID,  new SourceImpl(source)));
								}
							}
						}
					}
					if(!id.equals("") && id.indexOf(':')!=-1 && id.lastIndexOf(':')!=-1)
					{
						String source = id.substring(0,id.indexOf(':'));
						int secondTwoPoints = id.lastIndexOf(':')+1;
						String externalID = id.substring(secondTwoPoints);
						externalIds.add(new ExternalIDImpl(externalID,  new SourceImpl(source)));	
						classe = new OntologicalClass(name, def, isA,partof, syns,externalIds);
						ontologicatermIDDetails.put(id, classe);
					}
					id = new String();
					name = new String();
					def = new String();
					isA = new ArrayList<String>();
					partof = new ArrayList<String>();
					syns = new ArrayList<String>();
					externalIds = new ArrayList<IExternalID>();
				}
				block = new ArrayList<String>();
			}
			else
			{
				block.add(lineread);
			}
			
		}
		br.close();
		return ontologicatermIDDetails;
	}

	public boolean validateFile(File file) throws IOException {
		FileReader fr;
			fr = new FileReader(file);
			br = new BufferedReader(fr);		
			String line;
			if((line = br.readLine())!=null)
			{
				if(line.contains("format-version"))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		return false;
	}
	
	public IResourceUpdateReport processOntologyFile(IOntologyLoaderConfiguration configuration) throws ANoteException, IOException
	{
		this.setResource(configuration.getOntology());
		Map<String, OntologicalClass> fileData = loadFile(configuration);
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		IResourceUpdateReport report = new ResourceUpdateReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.resources.ontology.update.report.title"), configuration.getOntology(), null, "");
		report.updateFile(new File(configuration.getFilePath()));
		Map<String,IResourceElement> ontologyIDDatabaseIndex = new HashMap<String, IResourceElement>();
		int total = 2*fileData.size();
		int point = 0;
		String onto = "ontology";
		if(getResource().getName()!=null && !getResource().getName().equals(""))
		{
			onto = getResource().getName();
		}
		IAnoteClass klass = new AnoteClass(onto);
		IResourceElement root = new ResourceElementImpl("root",klass,new ArrayList<IExternalID>(),new ArrayList<String>(),0,true);
		getReport().addClassesAdding(1);
		this.addElementToBatch(root);
		Iterator<String> itOnto = fileData.keySet().iterator();;
		while(itOnto.hasNext() && !cancel)
		{
			String cl = itOnto.next();
			OntologicalClass clO = fileData.get(cl);
			if(!ontologyIDDatabaseIndex.containsKey(cl))
			{
				List<String> synoyms = clO.getSynonyms();
				Set<String> nonrepeatedSynoyms = new HashSet<>(synoyms);
				IResourceElement elem = new ResourceElementImpl(clO.getName(),klass,clO.getExternalIDs(),new ArrayList<>(nonrepeatedSynoyms),0,true);
				ontologyIDDatabaseIndex.put(cl, elem);
				this.addElementToBatch(elem);
			}
			if(point % 100 == 0 )
			{
				memoryAndProgress(startTime,point,total);
			}
			if(!cancel && isBatchSizeLimitOvertaken())
			{
				IResourceManagerReport reportBatchInserted = super.executeBatchWithoutValidation();
				super.updateReport(reportBatchInserted,report);			
			}
			point ++;
		}
		if(!cancel)
		{
			IResourceManagerReport reportBatchInserted = super.executeBatchWithoutValidation();
			super.updateReport(reportBatchInserted,report);			
		}
		itOnto = fileData.keySet().iterator();
		while(itOnto.hasNext()  && !cancel)
		{
			porcessRelations(itOnto,fileData, ontologyIDDatabaseIndex, root,"is_a");
			if(point % 100 == 0 )
			{
				memoryAndProgress(startTime,point,total);
			}
			point ++;
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;

	}

	private void porcessRelations(Iterator<String> itOnto,Map<String, OntologicalClass> fileData,Map<String, IResourceElement> ontologyIDDatabaseIndex, IResourceElement root,String string) throws ANoteException {
		String cl = itOnto.next();
		OntologicalClass clO = fileData.get(cl);
		IResourceElement sun = ontologyIDDatabaseIndex.get(cl);
		if(clO.getIs_a().isEmpty() && clO.getPartof().isEmpty())
		{
			InitConfiguration.getDataAccess().addResourceElementsRelation(root, sun, string);
		}
		else
		{
			processIsA(ontologyIDDatabaseIndex, string,cl, clO);
		}
	}

	private void processIsA(Map<String, IResourceElement> ontologyIDDatabaseIndex,String relationType, String externalLink,OntologicalClass clO) throws ANoteException {
		IResourceElement sun = ontologyIDDatabaseIndex.get(externalLink);
		for(String isA:clO.getIs_a())
		{
			IResourceElement father = ontologyIDDatabaseIndex.get(isA);
			InitConfiguration.getDataAccess().addResourceElementsRelation(father, sun, relationType);
		}
	}

	@Override
	public void stop() {
		this.cancel = true;
	}
}
