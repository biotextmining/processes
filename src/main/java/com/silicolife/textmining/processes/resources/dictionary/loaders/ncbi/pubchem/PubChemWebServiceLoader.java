package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
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

public class PubChemWebServiceLoader  extends DictionaryLoaderHelp implements IDictionaryWebServiceLoader{

	
	private boolean cancel = false;

	public final static String compound = "Compound";
	public final static String source = "PubChem";

	
	public PubChemWebServiceLoader() {
		super("PubChem");
	}

	@Override
	public void stop() {
		cancel = true;		
	}

	@Override
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration)throws ANoteException, IOException {
		super.setResource(configuration.getDictionary());
		if(configuration instanceof IPubChemDictionaryLoaderConfiguration)
		{
			getReport().addClassesAdding(1);
			IPubChemDictionaryLoaderConfiguration pubchemConfiguration = (IPubChemDictionaryLoaderConfiguration) configuration;
			Set<String> pubchemTOImport = pubchemConfiguration.getPubChemIds();
			long start = GregorianCalendar.getInstance().getTimeInMillis();
			int total = pubchemTOImport.size();
			int step = 0;
			for(String pubchemId:pubchemTOImport)
			{
				try {
					processPubChemID(pubchemId);
				} catch (ANoteException e) {
				}
				step ++;
				if ((step % 500) == 0) {
					memoryAndProgress(step, total);
				}
				if (!cancel && isBatchSizeLimitOvertaken()) {
					IResourceManagerReport reportBatchInserted = super.executeBatch();
					super.updateReport(reportBatchInserted, getReport());
				}
			}

			if (!cancel) {
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted, getReport());
			} else {
				getReport().setcancel();
			}
			long end = GregorianCalendar.getInstance().getTimeInMillis();
			getReport().setTime(end - start);
			return getReport();
		}
		else
			throw new ANoteException("PubChemWebServiceLoader: Configuration must be a IPubChemDictionaryLoaderConfiguration implementation");
	}

	private void processPubChemID(String pubchemId) throws ANoteException {
		List<String> names = PubChemAPI.getPubChemNamesByCID(pubchemId);
		if(!names.isEmpty())
		{
			String term = names.get(0);		
			Set<String> synonyms = new HashSet<>(names);
			synonyms.remove(term);
			// For get all external id
//			List<IExternalID> externalIDs = PubChemAPI.getExternalIdsByPubchemID(pubchemId);
			// For add only Pubchem
			List<IExternalID> externalIDs = new ArrayList<>();
			externalIDs.add(new ExternalIDImpl(pubchemId, new SourceImpl(source)));
			super.addElementToBatch(term, compound, synonyms, externalIDs, 0);		
		}
	}

}
