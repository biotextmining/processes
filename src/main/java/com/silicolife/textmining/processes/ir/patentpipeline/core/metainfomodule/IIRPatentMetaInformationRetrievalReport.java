package com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule;

import java.util.Map;

import com.silicolife.textmining.core.interfaces.core.document.IPublication;

public interface IIRPatentMetaInformationRetrievalReport {
	
	public Map<String, IPublication> getMapPatentIDPublication();
	public Map<String, IPublication> getMapPatentIDPublicationExcluded();
	public void setMapPatentIDPublication(Map<String, IPublication> mapPatentIDPublication);
	public void setMapPatentIDPublicationExcluded(Map<String, IPublication> mapPatentIDPublicationExcluded);
	public void updateMapPatentIDPublication(Map<String,IPublication> newMapPatentIDPublication);

}
