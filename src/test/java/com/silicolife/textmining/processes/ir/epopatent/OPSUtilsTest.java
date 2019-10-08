package com.silicolife.textmining.processes.ir.epopatent;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.junit.Test;

import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSUtilsTest {
	
	private static String accessTokenEPO = "accesstoken";
	
	@Test
	public void getAuthentication() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String token = getAccessToken();
		System.out.println(token);
	}
	
	@Test
	public void getSearch() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "PHBs";
		String token = getAccessToken();
		int step = 1;
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		for(IPublication item:result)
			System.out.println(item.toString());
	}
	
	@Test
	public void getPatentFamily() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "PHBs";
		String token = getAccessToken();
		int step = 1;
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		IPublication pub = result.get(0);
		System.out.println(pub.getPublicationExternalIDSource());
		String patentID = pub.getPublicationExternalIDSource().get(0).getSourceInternalId();
		OPSUtils.getPatentFamily(token, pub , patentID );
		System.out.println(pub.getPublicationExternalIDSource());
	}
	
	@Test
	public void updatePatentMetaInformation() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "PHBs";
		String token = getAccessToken();
		int step = 1;
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		IPublication pub = result.get(0);
		pub.setTitle("");
		pub.setAbstractSection("");
		pub.setAuthors("");
		System.out.println(pub.toString());
		String patentID = pub.getPublicationExternalIDSource().get(0).getSourceInternalId();
		OPSUtils.updatePatentMetaInformation(token, pub, patentID,true);
		System.out.println(pub.toString());
	}
	
	@Test
	public void getPatentOwners() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "PHBs";
		String token = getAccessToken();
		int step = 1;
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		for(IPublication item:result)
		{
			String patentID = item.getPublicationExternalIDSource().get(0).getSourceInternalId();
			String owners = OPSUtils.getPatentOwners(token, patentID);
			System.out.println(patentID + " : "+owners);
		}
	}
	
	@Test
	public void getSearchPatentIds() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "PHBs";
		String token = getAccessToken();
		int step = 1;
		Set<String> result = OPSUtils.getSearchPatentIds(token, query, step );
		System.out.println(result.toString());
	}
	
	@Test
	public void getSearchResults() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "PHBs";
		String token = getAccessToken();
		int result = OPSUtils.getSearchResults(token,query);
		System.out.println(result);
	}
	
	@Test
	public void updateAbstractwithDescritionandclaims() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "PHBs";
		String token = getAccessToken();
		int step = 1;
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		for(IPublication item:result)
		{
			System.out.println(item.toString());
			OPSUtils.updateAbstractwithDescritionandclaims(token, item);
			System.out.println(item.toString());
		}
	}
	
	@Test
	public void getPatentFullTextPDF() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, COSVisitorException, IOException, InterruptedException
	{
		String query = "PHBs";
		String path = "src/test/resources";
		String token = getAccessToken();
		int step = 1;
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		IPublication pub = result.get(0);
		OPSUtils.getPatentFullTextPDF(token, pub, path);
	}
	
	@Test
	public void getPatentFullTextPDFUsingPatentID() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, COSVisitorException, IOException, InterruptedException
	{
		String query = "PHBs";
		String path = "src/test/resources";
		String token = getAccessToken();
		int step = 1;
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		IPublication pub = result.get(0);
		String patentID = pub.getPublicationExternalIDSource().get(0).getSourceInternalId();
		OPSUtils.getPatentFullTextPDFUsingPatentID(token, patentID , path, pub.getId());
	}
	

	private String getAccessToken() throws ConnectionException, RedirectionException, ClientErrorException,
			ServerErrorException, ResponseHandlingException {
		String autentication = Utils.get64Base(accessTokenEPO);
		String token = OPSUtils.postAuth(autentication);
		return token;
	}

}
