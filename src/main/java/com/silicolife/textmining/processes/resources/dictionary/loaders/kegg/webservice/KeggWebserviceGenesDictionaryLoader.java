package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionaryWebServiceLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public class KeggWebserviceGenesDictionaryLoader extends DictionaryLoaderHelp implements IDictionaryWebServiceLoader{

	private boolean cancel;
	private static String keggSource = "Kegg";
	private static String klass = "Gene";


	public KeggWebserviceGenesDictionaryLoader() {
		super(keggSource);
	}

	@Override
	public void stop() {
		this.cancel = true;
	}

	@Override
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		cancel = false;
		super.setResource(configuration.getDictionary());
		if(configuration instanceof IKeggWebserviceGenesDictionaryLoaderConfiguration)
		{
			IKeggWebserviceGenesDictionaryLoaderConfiguration keggWebserviceGenesDictionaryLoaderConfiguration = (IKeggWebserviceGenesDictionaryLoaderConfiguration) configuration;
			getReport().addClassesAdding(1);
			String keggOrganism = keggWebserviceGenesDictionaryLoaderConfiguration.gerOrganism();
			List<String> genesTextStream = KeggWebserviceAPI.getGenesByOrganismStream(keggOrganism);
			int total = genesTextStream.size();
			long start = GregorianCalendar.getInstance().getTimeInMillis();
			int step = 0;
			for(String geneTextStream:genesTextStream)
			{
				String keggGeneID = KeggWebserviceAPI.getEntityIDGivenEntityStream(keggOrganism, geneTextStream);
				List<String> synonyms = KeggWebserviceAPI.getEntityNames(geneTextStream);
				String term = "";
				if(!synonyms.isEmpty())
				{
					term = synonyms.get(0);
					synonyms.remove(0);
				}
				// Add bnumber only in ecoli
				if(keggOrganism.equals("eco"))
					synonyms.add(keggGeneID);
				step++;
				List<IExternalID> externalIDs = new ArrayList<>();
				externalIDs.add(new ExternalIDImpl(keggGeneID, new SourceImpl(keggSource)));	
				this.addElementToBatch(term, klass, new HashSet<>(synonyms) , externalIDs  , 0);
				if ((step % 500) == 0) {
					memoryAndProgress(step, total);
				}
				if (!cancel && isBatchSizeLimitOvertaken()) {
					IResourceManagerReport reportBatchInserted = super.executeBatchWithoutValidation();
					super.updateReport(reportBatchInserted, getReport());
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
			throw new ANoteException("KeggWebserviceDictionaryLoader: Configuration must be a IKeggWebserviceGenesDictionaryLoaderConfiguration implementation");

	}

}
