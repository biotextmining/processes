package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IRPatentMetaInformationRetrievalConfigurationImpl;

public class IROPSPatentMetaInformationRetrievalConfigurationImpl extends IRPatentMetaInformationRetrievalConfigurationImpl implements IIROPSPatentMetaInformationRetrievalConfiguration{
	
	private String accessToken;
	
	private boolean abstarctIncludeClaimsAndDescription;
	private boolean waitingTimeBetweenSteps;

	
	public IROPSPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy,String accessToken,boolean abstarctIncludeClaimsAndDescription,boolean waitingTimeBetweenSteps) {
		super(proxy);
		this.accessToken=accessToken;
		this.abstarctIncludeClaimsAndDescription = abstarctIncludeClaimsAndDescription;
		this.waitingTimeBetweenSteps = waitingTimeBetweenSteps;
	}
	
	public IROPSPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy,String accessToken,boolean abstarctIncludeClaimsAndDescription) {
		this(proxy, accessToken, abstarctIncludeClaimsAndDescription,true);
	}
	
	public IROPSPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy,String accessToken) {
		this(proxy, accessToken, false,true);
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	public boolean isAbstarctIncludeClaimsAndDescription() {
		return abstarctIncludeClaimsAndDescription;
	}

	@Override
	public boolean isWaitingTimeBetweenSteps() {
		return waitingTimeBetweenSteps;
	}

}
