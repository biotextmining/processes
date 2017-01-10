package com.silicolife.textmining.processes.ir.patentpipeline.configuration;

import java.util.ArrayList;
import java.util.List;

import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;

public class IRPatentPipelineSearchStepsConfigurationImpl implements IIRPatentPipelineSearchStepsConfiguration{
	List<IIRPatentIDRetrievalSource> patentIDRecoverSource;
	List<IIRPatentMetainformationRetrievalSource> patentRetrievalMetainformationSource;
//	List<IIRPatentRetrieval> patentRetrievalSystem;

	
	public IRPatentPipelineSearchStepsConfigurationImpl(List<IIRPatentIDRetrievalSource> patentIDRecoverSource,
			List<IIRPatentMetainformationRetrievalSource> patentRetrievalMetainformationSource,
			List<IIRPatentRetrieval> patentRetrievalSystem){
		this.patentIDRecoverSource=patentIDRecoverSource;
		this.patentRetrievalMetainformationSource=patentRetrievalMetainformationSource;

	}
	
	public IRPatentPipelineSearchStepsConfigurationImpl(){
		patentIDRecoverSource=new ArrayList<>();
		patentRetrievalMetainformationSource=new ArrayList<>();
	}
	
	

	@Override
	public List<IIRPatentIDRetrievalSource> getIIRPatentIDRecoverSource() {
		return patentIDRecoverSource;
	}

	@Override
	public List<IIRPatentMetainformationRetrievalSource> getIIRPatentRetrievalMetaInformation() {
		return patentRetrievalMetainformationSource;
	}

	@Override
	public void addIRPatentIDRecoverSource(IIRPatentIDRetrievalSource patentIDREcoverSource) {
		this.patentIDRecoverSource.add(patentIDREcoverSource);
		
	}

	@Override
	public void addIRPatentRetrievalMetaInformation(IIRPatentMetainformationRetrievalSource patentRetrievalMetaInformation) {
		this.patentRetrievalMetainformationSource.add(patentRetrievalMetaInformation);
		
	}

}
