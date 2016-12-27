package com.silicolife.textmining.processes.ie.re.kineticre;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;

public class PublicationMetaInfoSearch {
	
	public static Set<IPublication> searchInformationOnDatabase(List<String> pmids, Map<String, Long> pmidDocID) throws ANoteException {
		Set<String> idsToremove = new HashSet<>();
		Set<IPublication> publicationINDatabase = new HashSet<>();
		for(String pmid: pmids)
		{
			if(pmidDocID.containsKey(pmid))
			{
				publicationINDatabase.add(InitConfiguration.getDataAccess().getPublication(pmidDocID.get(pmid)));
				idsToremove.add(pmid);
			}
		}
		for(String pmid:idsToremove)
		{
			pmids.remove(pmid);
		}
		return publicationINDatabase;
	}

}