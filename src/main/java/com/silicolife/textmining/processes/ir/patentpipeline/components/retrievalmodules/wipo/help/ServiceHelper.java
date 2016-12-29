package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.help;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

import org.wipo.pctis.ps.client.PatentScope;
import org.wipo.pctis.ps.client.PatentScopeService;

import com.sun.xml.ws.developer.JAXWSProperties;

public class ServiceHelper {
	
	/**
	 * Locator used by the ServiceHelper
	 */
	private PatentScopeService locator;
	
	/**
	 * Stub used by the service Helper
	 */
	private PatentScope stub;
	
	
	/**
	 * password property
	 */
	private static final String patentscopeServiceWsdlLocationProperty="http://www.wipo.int/patentscope-webservice/servicesPatentScope?wsdl";
	
	public ServiceHelper(String userName,String password) throws MalformedURLException{
		//Initialization of the Authenticator object
    	//to send the authentication parameters
    	//when calling a Web Service
    	initAuthenticator(userName,password);
		URL baseUrl = org.wipo.pctis.ps.client.PatentScopeService.class.getResource(".");
		URL url = new URL(baseUrl, patentscopeServiceWsdlLocationProperty);
		locator = new PatentScopeService(url, new QName("http://www.wipo.org/wsdl/ps", "PatentScopeService"));
		stub = locator.getPatentScope(new MTOMFeature());
        Map<String, Object> ctxt = ((BindingProvider)stub).getRequestContext();
        ctxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192); 
	}
	

	
	/**
	 * Method to initialize the Authenticator object
	 * @throws InvalidPropertiesFormatException
	 * @throws IOException
	 */
	private void initAuthenticator(String userName,String password){
		AuthenticatorPatentScopeWs authenticator = new AuthenticatorPatentScopeWs();
		authenticator.setPasswordAuthentication(userName, password);
		Authenticator.setDefault(authenticator);
	}
	
	/**
     * Set a service stub's endpoint URL to send requests to tcpmon, for testing purpose only.
     * @param serviceStub a JAX-RPC service stub 
     */
    public void setEndPointAddress(String endPointAddress) {
    	((javax.xml.ws.BindingProvider)stub).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointAddress);
    }

	public PatentScope getStub() {
		return stub;
	}

	public void setStub(PatentScope stub) {
		this.stub = stub;
	}
	
}
