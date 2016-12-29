package com.silicolife.textmining.processes.ir.patentpipeline.configuration;

import java.util.ArrayList;
import java.util.List;

import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentRetrievalMetaInformation;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRecoverSource;

public class IRPatentPipelineSearchConfigurationImpl implements IIRPatentPipelineSearchConfiguration{
	List<IIRPatentIDRecoverSource> patentIDRecoverSource;
	List<IIRPatentRetrievalMetaInformation> patentRetrievalMetainformationSource;
//	List<IIRPatentRetrieval> patentRetrievalSystem;

	
	public IRPatentPipelineSearchConfigurationImpl(List<IIRPatentIDRecoverSource> patentIDRecoverSource,
			List<IIRPatentRetrievalMetaInformation> patentRetrievalMetainformationSource,
			List<IIRPatentRetrieval> patentRetrievalSystem){
		this.patentIDRecoverSource=patentIDRecoverSource;
		this.patentRetrievalMetainformationSource=patentRetrievalMetainformationSource;

	}
	
	public IRPatentPipelineSearchConfigurationImpl(){
		patentIDRecoverSource=new ArrayList<>();
		patentRetrievalMetainformationSource=new ArrayList<>();
	}
	
	

	@Override
	public List<IIRPatentIDRecoverSource> getIIRPatentIDRecoverSource() {
		return patentIDRecoverSource;
	}

	@Override
	public List<IIRPatentRetrievalMetaInformation> getIIRPatentRetrievalMetaInformation() {
		return patentRetrievalMetainformationSource;
	}

	@Override
	public void addIRPatentIDRecoverSource(IIRPatentIDRecoverSource patentIDREcoverSource) {
		this.patentIDRecoverSource.add(patentIDREcoverSource);
		
	}

	@Override
	public void addIRPatentRetrievalMetaInformation(IIRPatentRetrievalMetaInformation patentRetrievalMetaInformation) {
		this.patentRetrievalMetainformationSource.add(patentRetrievalMetaInformation);
		
	}

}
