package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class PUGRestPublicationIDSHandler implements ResponseHandler<Map<String,Set<String>>>{


	public PUGRestPublicationIDSHandler() {
	}


	@Override
	public Map<String,Set<String>> buildResponse(InputStream response, String responseMessage,
			Map<String, List<String>> headerFields, int status) throws ResponseHandlingException {
		Map<String,Set<String>> patentIDs = new HashMap<String, Set<String>>();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(response);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Information");
			for(int i=0;i<nList.getLength();i++)
			{
				Node item = nList.item(i);
				Element element = (Element) item;
				String cid = element.getElementsByTagName("CID").item(0).getTextContent();
				patentIDs.put(cid, new HashSet<String>());
				NodeList patentIDNodeList = element.getElementsByTagName("PubMedID");
				for(int j=0;j<patentIDNodeList.getLength();j++)
				{
					String patentID = patentIDNodeList.item(j).getTextContent().trim();
					patentIDs.get(cid).add(patentID);
				}
			}
			return patentIDs;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ResponseHandlingException(e);
		}


	}

}
