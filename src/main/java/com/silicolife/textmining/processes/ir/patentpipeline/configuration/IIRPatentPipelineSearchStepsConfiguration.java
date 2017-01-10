package com.silicolife.textmining.processes.ir.patentpipeline.configuration;

import java.util.List;

import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;

public interface IIRPatentPipelineSearchStepsConfiguration {
	/**
	 * return a List of all configured sources of Patent IDs
	 * @return
	 */
	public List<IIRPatentIDRetrievalSource> getIIRPatentIDRecoverSource();

	/**
	 * return a List of all configured sources of metainformation
	 * @return
	 */

	public List<IIRPatentMetainformationRetrievalSource> getIIRPatentRetrievalMetaInformation();
	
	/**
	 * add a PatentIDRecoverSource configuration to list
	 * @param patentIDREcoverSource
	 */

	public void addIRPatentIDRecoverSource(IIRPatentIDRetrievalSource patentIDREcoverSource);
	
	/**
	 * add a PatentRetrievalMetaInformation configuration to list
	 * @param patentRetrievalMetaInformation
	 */
	public void addIRPatentRetrievalMetaInformation(IIRPatentMetainformationRetrievalSource patentRetrievalMetaInformation);

}
