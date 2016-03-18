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

import com.silicolife.http.ResponseHandler;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;

public class OPSPatentClaimsHandler implements ResponseHandler<String> {

	@Override
	public String buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status)
			throws ResponseHandlingException {
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList claimNodesNode = doc.getElementsByTagName("claims");
			for(int i=0;i<claimNodesNode.getLength();i++)
			{
				Node claimNode = claimNodesNode.item(i);
				String lang = claimNode.getAttributes().getNamedItem("lang").getNodeValue();
				if(lang.equalsIgnoreCase("en"))
				{
					return claimNode.getTextContent();
				}
			}		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
