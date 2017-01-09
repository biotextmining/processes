package com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule;

public abstract class AIRPatentIDRecoverSource implements IIRPatentIDRetrievalSource{
	
	private IIRPatentIDRetrievalModuleConfiguration configuration;
	protected boolean stop;
	
	/**
	 * Constructor that turns mandatory the IIRPatentIDRecoverConfiguration
	 * 
	 * @param configuration
	 * @throws WrongIRPatentIDRecoverConfigurationException
	 */
	public AIRPatentIDRecoverSource(IIRPatentIDRetrievalModuleConfiguration configuration) throws WrongIRPatentIDRecoverConfigurationException
	{
		validate(configuration);
		this.configuration=configuration;
		this.stop = false;
	}
	
	public IIRPatentIDRetrievalModuleConfiguration getConfiguration()
	{
		return configuration;
	}
	

	public void setConfigutaion(IIRPatentIDRetrievalModuleConfiguration  configuration) throws WrongIRPatentIDRecoverConfigurationException
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
	public abstract void validate(IIRPatentIDRetrievalModuleConfiguration configuration)throws WrongIRPatentIDRecoverConfigurationException;
	
	public void stop()
	{
		stop = true;
	}

}
