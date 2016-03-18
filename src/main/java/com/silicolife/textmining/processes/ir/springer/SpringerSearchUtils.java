package com.silicolife.textmining.processes.ir.springer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.silicolife.http.HTTPClient;
import com.silicolife.http.exceptions.ClientErrorException;
import com.silicolife.http.exceptions.ConnectionException;
import com.silicolife.http.exceptions.RedirectionException;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.http.exceptions.ServerErrorException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.springer.configuration.SpringerConfiguration;
import com.silicolife.textmining.processes.ir.springer.handler.SpringerSearchHandler;
import com.silicolife.textmining.processes.ir.springer.handler.SpringerSearchResultHandler;

public class SpringerSearchUtils {
	
	
	private static HTTPClient client = new HTTPClient();
	public static int numberOFRetries = 5;
	
	
	
	public static List<IPublication> getSearchRange(String autentication, String query,int step) throws ConnectionException, RedirectionException, ClientErrorException, ServerErrorException, ResponseHandlingException {
		Map<String, String> headers = new HashMap<String, String>();
		client = new HTTPClient();
		List<IPublication> pubs = client.get(SpringerConfiguration.springerstartLink + query+"&s="+step+"&p="+SpringerConfiguration.STEP+"&api_key="+autentication, headers, new SpringerSearchHandler());
		return pubs;
	}
	

	public static int getSearchResults(String query,String autentication) throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException {
		Map<String, String> headers = new HashMap<String, String>();
		Integer result = client.get(SpringerConfiguration.springerstartLink + query +"&api_key="+autentication, headers, new SpringerSearchResultHandler());
		return result;
	}
	
	public static Document createJDOMDocument(InputStream response)
			throws ParserConfigurationException, SAXException, IOException {
		String stream = IOUtils.toString(response, "UTF-8");
		stream = stream.replaceAll("\n", "");
		stream = stream.replaceAll("\\s{2,}", "");
		InputStream imputstream = new ByteArrayInputStream(stream.getBytes("UTF-8"));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(imputstream);
		return doc;
	}

}
