package com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule;

import java.util.HashSet;
import java.util.Set;

public class IRPatentRetrievalReport implements IIRPatentRetrievalReport{
	
	private Set<String> retrievedPatents;
	private Set<String> notRetrievedPatents;
	
	public IRPatentRetrievalReport()
	{
		this.retrievedPatents=new HashSet<>();
		this.notRetrievedPatents=new HashSet<>();
				
	}
	
	public Set<String> getRetrievedPatents() {
		return retrievedPatents;
	}
	public void addRetrievedPatents(String retrievedPatent) {
		this.retrievedPatents.add(retrievedPatent);
	}
	public Set<String> getNotRetrievedPatents() {
		return notRetrievedPatents;
	}
	public void addNotRetrievedPatents(String notRetrievedPatent) {
		this.notRetrievedPatents.add(notRetrievedPatent);
	}

	@Override
	public void setRetrievedPatents(Set<String> retrievedPatents) {
		this.retrievedPatents=retrievedPatents;
		
	}

	@Override
	public void setNotRetrievedPatents(Set<String> notRetrievedPatents) {
		this.notRetrievedPatents=notRetrievedPatents;
		
	}
	
	
}

