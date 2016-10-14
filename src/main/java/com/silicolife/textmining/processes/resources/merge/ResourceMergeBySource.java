package com.silicolife.textmining.processes.resources.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.report.resources.ResourceMergeReportImpl;
import com.silicolife.textmining.core.datastructures.resources.ResourceElementImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceMergeReport;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.IResourceElementSet;

public class ResourceMergeBySource {
	
	public IResourceMergeReport mergeResoures(IResource<IResourceElement> destiny,IResource<IResourceElement> toMerge,ISource source) throws ANoteException
	{
		IResourceMergeReport report = new ResourceMergeReportImpl("",destiny,destiny,toMerge);
		Set<String> alreadyUsedWords = new HashSet<>();
		boolean hasmore = true;
		int i=0;
		int step = 10000;
		Map<String,IResourceElement> sourceExternalIDResource = new HashMap<>();
		while(hasmore)
		{
			int finalStep = i+step;
			IResourceElementSet<IResourceElement> elems = InitConfiguration.getDataAccess().getResourceElementsInBatchWithLimit(destiny, i, finalStep);
			System.out.println(i + " ... "+finalStep);
			for(IResourceElement elem:elems.getResourceElements())
			{
				List<IExternalID> extendsalIDs = elem.getExtenalIDs();
				for(IExternalID extID:extendsalIDs)
				{
					if(extID.getSource().getSource().equals(source.getSource()))
					{
						sourceExternalIDResource.put(extID.getExternalID(), elem);
					}
				}
				alreadyUsedWords.add(elem.getTerm());
				alreadyUsedWords.addAll(elem.getSynonyms());
			}
			if(elems.size()==0)
				hasmore = false;
			i = i + step;
		}
		/// To merge
		hasmore = true;
		i=0;
		step = 1000;
		while(hasmore)
		{
			int finalStep = i+step;
			IResourceElementSet<IResourceElement> elems = InitConfiguration.getDataAccess().getResourceElementsInBatchWithLimit(toMerge, i, i+step);
			System.out.println(i + " ... "+finalStep);
			for(IResourceElement elem:elems.getResourceElements())
			{
				List<IExternalID> extendsalIDs = elem.getExtenalIDs();
				for(IExternalID extID:extendsalIDs)
				{
					if(extID.getSource().getSource().equals(source.getSource()))
					{
						if(sourceExternalIDResource.containsKey(extID.getExternalID()))
						{
							IResourceElement originalElem = sourceExternalIDResource.get(extID.getExternalID());
							List<String> synonyms = elem.getSynonyms();
							synonyms.add(elem.getTerm());
							Set<String> alreadyInEntry = new HashSet<>();
							alreadyInEntry.addAll(originalElem.getSynonyms());
							alreadyInEntry.add(originalElem.getTerm());
							List<String> synonymsToAdd = new ArrayList<>();
							for(String syn:synonyms)
							{
								if(!alreadyInEntry.contains(syn) && !alreadyUsedWords.contains(syn) && !syn.contains("cell"))
								{
									synonymsToAdd.add(syn);
								}
							}
							List<IExternalID> extermalIdsToAdd = new ArrayList<>();
							if(!synonymsToAdd.isEmpty())
							{
								System.out.println("["+extID.getExternalID()+":"+source.getSource()+"]");
								System.out.println(originalElem.getId() +" <- "+ elem.getId() );
								System.out.println("\tUpdate Synonyms :"+synonymsToAdd.toString());
								alreadyUsedWords.addAll(synonymsToAdd);
								InitConfiguration.getDataAccess().addResourceElementSynomynsWithoutValidation(destiny, originalElem, synonymsToAdd);
							}
							if(!extermalIdsToAdd.isEmpty())
							{
//								InitConfiguration.getDataAccess().addResourceElementExternalIds(destiny, originalElem, extermalIdsToAdd); 
							}
						}
						else
						{
//							System.out.println("["+extID.getExternalID()+":"+source.getSource()+"]");
//							List<IResourceElement> elements = new ArrayList<>();
//							List<String> synonyms = elem.getSynonyms();
//							Set<String> synonymsToAdd = new HashSet<>();
//							for(String syn:synonyms)
//							{
//								if(!alreadyUsedWords.contains(syn))
//								{
//									synonymsToAdd.add(syn);
//								}
//							}
//							String primaryTerm = null;
//							if(alreadyUsedWords.contains(elem.getTerm()) && synonymsToAdd.isEmpty())
//							{
//								
//							}
//							else if(alreadyUsedWords.contains(elem.getTerm()) )
//							{
//								primaryTerm = synonyms.get(0);
//							}
//							else
//							{
//								primaryTerm = elem.getTerm();
//							}
//							if(primaryTerm!=null)
//							{
//								IResourceElement elemAux = new ResourceElementImpl(primaryTerm, elem.getTermClass(), elem.getExtenalIDs(), new ArrayList<String>(synonymsToAdd), 0, true);
//								elements.add(elemAux );				
////								InitConfiguration.getDataAccess().addResourceElements(destiny, elements )
//								System.out.println("\tAdd New Entry :"+elem.getTerm() + " " +elem.getSynonyms().toString());
//							}
						}
					}
				}
			}
			if(elems.size()==0)
				hasmore = false;
			i = i + step;
		}
		return report;
	}
	
			

}
