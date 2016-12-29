package com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule;

import java.util.Set;

public interface IIRPatentRetrievalReport {
	
	public Set<String> getRetrievedPatents();
	
	public void addRetrievedPatents(String retrievedPatent);
	
	public Set<String> getNotRetrievedPatents();
	
	public void addNotRetrievedPatents(String notRetrievedPatent);
	
	public void setRetrievedPatents(Set<String> retrievedPatents);
	
	public void setNotRetrievedPatents(Set<String> notRetrievedPatents);

}
