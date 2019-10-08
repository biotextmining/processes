package com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule;

import java.io.File;
import java.net.Proxy.Type;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public abstract class  AIRPatentRetrieval implements IIRPatentRetrieval{
	
	private IIRPatentRetrievalConfiguration configuration;
	protected boolean stop;
	
	/**
	 * Constructor that turns mandatory the IIRPatentRetrievalConfiguration
	 * 
	 * @param configuration
	 * @throws WrongIRPatentIDRecoverConfigurationException
	 */
	public AIRPatentRetrieval(IIRPatentRetrievalConfiguration configuration) throws WrongIRPatentRetrievalConfigurationException
	{
		validate(configuration);
		setProxy(configuration.getProxy());
		this.configuration=configuration;
		this.stop = false;
	}
	
	public IIRPatentRetrievalConfiguration getConfiguration()
	{
		return configuration;
	}
	
	public void setConfigutaion(IIRPatentRetrievalConfiguration  configuration) throws WrongIRPatentRetrievalConfigurationException
	{
		validate(configuration);
		setProxy(configuration.getProxy());
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
	public abstract void validate(IIRPatentRetrievalConfiguration configuration)throws WrongIRPatentRetrievalConfigurationException;
	
	public void stop()
	{
		stop = true;
	}
	
	private void setProxy(IProxy proxy)
	{
		if(proxy!=null && !proxy.type().equals(Type.DIRECT))
		{	
			String proxyHost = proxy.getProxyHost();
			if ((proxyHost != null) && !(proxyHost.equals(""))) {
				System.setProperty("http.proxyHost",proxyHost);
			}
			String proxyPort = proxy.getProxyPort();
			if ((proxyPort != null) && !(proxyPort.equals(""))) {
				System.setProperty("http.proxyPort",proxyPort);
			}
		}
		else
		{
//			System.setProperty("http.proxyHost",null);
//			System.setProperty("http.proxyPort",null);
		}
	}
	
	protected boolean verifyPDFAlreadyDownloaded(String filePathway){
		File file=new File(filePathway);
		if(file.exists() && file.isFile()){
			return true;
		}
		return false;

	}

}
