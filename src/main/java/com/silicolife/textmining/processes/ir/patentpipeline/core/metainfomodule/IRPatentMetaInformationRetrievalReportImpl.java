package com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.document.IPublication;

public class IRPatentMetaInformationRetrievalReportImpl implements IIRPatentMetaInformationRetrievalReport{

	private Map<String, IPublication> mapPatentIDPublication;


	public IRPatentMetaInformationRetrievalReportImpl() {
		mapPatentIDPublication = new HashMap<String, IPublication>();
	}

	@Override
	public Map<String, IPublication> getMapPatentIDPublication() {
		return mapPatentIDPublication;
	}

	public void setMapPatentIDPublication(Map<String, IPublication> mapPatentIDPublication) {
		this.mapPatentIDPublication = mapPatentIDPublication;
	}

	@Override
	public void updateMapPatentIDPublication(Map<String, IPublication> newMapPatentIDPublication) {
		Set<String> patentIDs = newMapPatentIDPublication.keySet();
		for (int patIndex = 0; patIndex < patentIDs.size(); patIndex++) {
			String patentID = (String) patentIDs.toArray()[patIndex];
			if ((mapPatentIDPublication.get(patentID).getTitle()==null||mapPatentIDPublication.get(patentID).getTitle().isEmpty())||
//					((mapPatentIDPublication.get(patentID).getAbstractSection()==null||mapPatentIDPublication.get(patentID).getAbstractSection().isEmpty())&&
					(mapPatentIDPublication.get(patentID).getAuthors()==null||mapPatentIDPublication.get(patentID).getAuthors().isEmpty())||
					(mapPatentIDPublication.get(patentID).getYeardate()==null||mapPatentIDPublication.get(patentID).getYeardate().isEmpty())){//empty publication (or pub without title)

				mapPatentIDPublication.put(patentID, newMapPatentIDPublication.get(patentID));
			}
		}

	}

}
