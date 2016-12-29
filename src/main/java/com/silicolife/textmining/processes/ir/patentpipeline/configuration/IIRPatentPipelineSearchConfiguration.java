package com.silicolife.textmining.processes.ir.patentpipeline.configuration;

import java.util.List;

import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentRetrievalMetaInformation;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRecoverSource;

public interface IIRPatentPipelineSearchConfiguration {
	/**
	 * return a List of all configured sources of Patent IDs
	 * @return
	 */
	public List<IIRPatentIDRecoverSource> getIIRPatentIDRecoverSource();

	/**
	 * return a List of all configured sources of metainformation
	 * @return
	 */

	public List<IIRPatentRetrievalMetaInformation> getIIRPatentRetrievalMetaInformation();
	
	/**
	 * add a PatentIDRecoverSource configuration to list
	 * @param patentIDREcoverSource
	 */

	public void addIRPatentIDRecoverSource(IIRPatentIDRecoverSource patentIDREcoverSource);
	
	/**
	 * add a PatentRetrievalMetaInformation configuration to list
	 * @param patentRetrievalMetaInformation
	 */
	public void addIRPatentRetrievalMetaInformation(IIRPatentRetrievalMetaInformation patentRetrievalMetaInformation);

}
