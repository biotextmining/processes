package com.silicolife.textmining.processes.ir.springer.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.http.ResponseHandler;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.processes.ir.springer.SpringerSearchUtils;

public class SpringerSearchResultHandler implements ResponseHandler<Integer>{

	public SpringerSearchResultHandler()
	{
		
	}
	
	@Override
	public Integer buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status)
			throws ResponseHandlingException {
		int result = -1;
		try {
			Document doc = SpringerSearchUtils.createJDOMDocument(response);
			NodeList nodeList = doc.getElementsByTagName("total");
			Node node = nodeList.item(0);	
			String resString = node.getTextContent();
			result = Integer.valueOf(resString);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
