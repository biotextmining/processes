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


public class OPSPatentDescriptionHandler implements ResponseHandler<String>{

	@Override
	public String buildResponse(InputStream response,String responseMessage, Map<String, List<String>> headerFields,int status) throws ResponseHandlingException {
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList descriptionNode = doc.getElementsByTagName("description");
			for(int i=0;i<descriptionNode.getLength();i++)
			{
				Node description = descriptionNode.item(i);
				String lang = description.getAttributes().getNamedItem("lang").getNodeValue();
				if(lang.equalsIgnoreCase("en"))
				{
					return description.getTextContent();
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
