package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class OPSSearchResultHandler implements ResponseHandler<Integer>{

	public OPSSearchResultHandler()
	{
		
	}
	
	@Override
	public Integer buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status)
			throws ResponseHandlingException {
		int result = -1;
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList nodeList = doc.getElementsByTagName("ops:biblio-search");
			Node node = nodeList.item(0);	
//			NodeList childnodeList = node.getChildNodes();
//			System.out.println("CQL :" + childnodeList.item(0).getTextContent());
			String resString = node.getAttributes().getNamedItem("total-result-count").getTextContent();
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
