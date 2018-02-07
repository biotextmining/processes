package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionaryWebServiceLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public class KeggWebserviceDictionaryLoader extends DictionaryLoaderHelp implements IDictionaryWebServiceLoader{

	private boolean cancel;
	private String keggSource = "Kegg";


	public KeggWebserviceDictionaryLoader() {
		super("Kegg");
	}

	@Override
	public void stop() {
		this.cancel = true;
	}

	@Override
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		cancel = false;
		super.setResource(configuration.getDictionary());
		if(configuration instanceof IKeggWebserviceDictionaryLoaderConfiguration)
		{
			IKeggWebserviceDictionaryLoaderConfiguration keggWebserviceDictionaryLoaderConfiguration = (IKeggWebserviceDictionaryLoaderConfiguration) configuration;
			getReport().addClassesAdding(keggWebserviceDictionaryLoaderConfiguration.getKeggEntities().size());
			Map<KeggEntitiesEnum,List<String>> mapKeggEntityClassTextStrealList = getEntitiesStream(keggWebserviceDictionaryLoaderConfiguration.getKeggEntities());
			long start = GregorianCalendar.getInstance().getTimeInMillis();
			int total = calculateTotal(mapKeggEntityClassTextStrealList);
			int step = 0;		
			for(KeggEntitiesEnum keggEntity:mapKeggEntityClassTextStrealList.keySet())
			{
				List<String> entitiesStream = mapKeggEntityClassTextStrealList.get(keggEntity);
				for(String entityStream:entitiesStream)
				{
					String keggID = KeggWebserviceAPI.getEntityIDGivenEntityStream(keggEntity.getKeggShortIndentifier(), entityStream);
					List<String> names =  KeggWebserviceAPI.getEntityNames(entityStream);
					step ++;
					if(!names.isEmpty())
					{
						String term = names.get(0);
						List<IExternalID> externalIDs = new ArrayList<>();
						externalIDs.add(new ExternalIDImpl(keggID, new SourceImpl(keggSource)));
						names.remove(0);
						Set<String> synonyms = new HashSet<>(names);
						this.addElementToBatch(term, keggEntity.getClassEntity(), synonyms , externalIDs , 0);
						if ((step % 500) == 0) {
							memoryAndProgress(step, total);
						}
						if (!cancel && isBatchSizeLimitOvertaken()) {
							IResourceManagerReport reportBatchInserted = super.executeBatchWithoutValidation();
							super.updateReport(reportBatchInserted, getReport());
						}
					}
				}
			}
			if (!cancel) {
				IResourceManagerReport reportBatchInserted = super.executeBatchWithoutValidation();
				super.updateReport(reportBatchInserted, getReport());
			} else {
				getReport().setcancel();
			}
			long end = GregorianCalendar.getInstance().getTimeInMillis();
			getReport().setTime(end - start);
			return getReport();
		}
		else
			throw new ANoteException("KeggWebserviceDictionaryLoader: Configuration must be a IKeggWebserviceDictionaryLoaderConfiguration implementation");
	}

	private int calculateTotal(Map<KeggEntitiesEnum,List<String>> mapKeggEntityClassTextStrealList)
	{
		int out = 0;
		for(KeggEntitiesEnum entity :mapKeggEntityClassTextStrealList.keySet())
		{
			out = out + mapKeggEntityClassTextStrealList.get(entity).size();
		}
		return out;
	}

	private Map<KeggEntitiesEnum,List<String>> getEntitiesStream(List<KeggEntitiesEnum> entitiesToSearch) throws IOException
	{
		Map<KeggEntitiesEnum,List<String>> out = new HashMap<>();
		for(KeggEntitiesEnum entityToSearch:entitiesToSearch)
		{
			out.put(entityToSearch,KeggWebserviceAPI.getEntityStream(entityToSearch.getKeggShortIndentifier()));
		}
		return out;
	}

}
