package com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule;

public abstract class AIRPatentIDRecoverSource implements IIRPatentIDRecoverSource{
	
	private IIRPatentIDRecoverConfiguration configuration;
	protected boolean stop;
	
	/**
	 * Constructor that turns mandatory the IIRPatentIDRecoverConfiguration
	 * 
	 * @param configuration
	 * @throws WrongIRPatentIDRecoverConfigurationException
	 */
	public AIRPatentIDRecoverSource(IIRPatentIDRecoverConfiguration configuration) throws WrongIRPatentIDRecoverConfigurationException
	{
		validate(configuration);
		this.configuration=configuration;
		this.stop = false;
	}
	
	public IIRPatentIDRecoverConfiguration getConfiguration()
	{
		return configuration;
	}
	

	public void setConfigutaion(IIRPatentIDRecoverConfiguration  configuration) throws WrongIRPatentIDRecoverConfigurationException
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
	public abstract void validate(IIRPatentIDRecoverConfiguration configuration)throws WrongIRPatentIDRecoverConfigurationException;
	
	public void stop()
	{
		stop = true;
	}

}
