package com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public abstract class AIRPatentMetaInformationRetrieval implements IIRPatentMetainformationRetrievalSource{
	
	private IIRPatentMetaInformationRetrievalConfiguration configuration;
	protected boolean stop;
	
	/**
	 * Constructor that turns mandatory the IIRPatentIDRecoverConfiguration
	 * 
	 * @param configuration
	 * @throws WrongIRPatentIDRecoverConfigurationException
	 */
	public AIRPatentMetaInformationRetrieval(IIRPatentMetaInformationRetrievalConfiguration configuration) throws WrongIRPatentMetaInformationRetrievalConfigurationException
	{
		validate(configuration);
		this.configuration=configuration;
		this.stop = false;
	}
	
	public IIRPatentMetaInformationRetrievalConfiguration getConfiguration()
	{
		return configuration;
	}
	

	public void setConfigutaion(IIRPatentMetaInformationRetrievalConfiguration  configuration) throws WrongIRPatentMetaInformationRetrievalConfigurationException
	{
		validate(configuration);
		this.configuration=configuration;
		this.stop = false;
	}

	/**
	 * Abstract method to validate configuration
	 * 		If configurations are wrong return a WrongIRPatentIDRecoverException exception
	 * 
	 * @param configuration
	 * @throws WrongIRPatentIDRecoverConfigurationException
	 */
	public abstract void validate(IIRPatentMetaInformationRetrievalConfiguration configuration)throws WrongIRPatentMetaInformationRetrievalConfigurationException;
	
	public void stop()
	{
		stop = true;
	}

}
